package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.external.registries.config.aws.PrivacyNoticeDTO;
import it.pagopa.pn.external.registries.exceptions.PnParameterStoreNotFound;
import it.pagopa.pn.external.registries.exceptions.PnPrivacyNoticeNotFound;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PrivacyNoticeVersionResponseDto;
import it.pagopa.pn.external.registries.middleware.msclient.onetrust.OneTrustClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivacyNoticeService {

    private static final String PRIVACY_NOTICE_PARAMETER_STORE = "MapPrivacyNotice";

    private final ParameterConsumer parameterConsumer;

    private final OneTrustClient oneTrustClient;

    private final ConcurrentHashMap<String, Integer> privacyNoticeCache = new ConcurrentHashMap<>();


    public Mono<PrivacyNoticeVersionResponseDto> findPrivacyNoticeVersion(String consentsType, String portalType) {
        log.info("Checking Privacy Notice with consentsType: {} and portalType: {}", consentsType, portalType);
        Optional<PrivacyNoticeDTO[]> mapPrivacyNotices = parameterConsumer.getParameterValue(PRIVACY_NOTICE_PARAMETER_STORE, PrivacyNoticeDTO[].class);
        if (mapPrivacyNotices.isPresent()) {
            Optional<PrivacyNoticeDTO> privacyNotice = getPrivacyNoticeFromParameterStore(mapPrivacyNotices.get(), consentsType, portalType);

            return privacyNotice
                    .map(privacyNoticeDTO -> oneTrustClient
                            .getPrivacyNoticeVersionByPrivacyNoticeId(privacyNoticeDTO.privacyNoticeId())
                            .doOnNext(oneTrustResponse -> log.debug("Privacy Notice retrieved from OneTrust with consentsType: {}, portalType: {}, privacyNoticeId: {}",
                                    consentsType, portalType, privacyNoticeDTO.privacyNoticeId()))
                            .doOnNext(oneTrustResponse -> privacyNoticeCache.put(privacyNoticeDTO.privacyNoticeId(), oneTrustResponse.version().version()))
                            .map(response -> new PrivacyNoticeVersionResponseDto().version(response.version().version()))
                            .onErrorResume(throwable -> getVersionFromCache(consentsType, portalType, privacyNoticeDTO.privacyNoticeId(), privacyNoticeDTO.defaultVersion()))
                    )
                    .orElseGet(() -> Mono.error(() -> new PnPrivacyNoticeNotFound(
                            String.format("Privacy Notice not found in PS, with consentsType: %s portalType: %s", consentsType, portalType))));
        } else {
            return Mono.error(() -> new PnParameterStoreNotFound(String.format("Parameter store not found: %s", PRIVACY_NOTICE_PARAMETER_STORE)));
        }
    }

    private Optional<PrivacyNoticeDTO> getPrivacyNoticeFromParameterStore(PrivacyNoticeDTO[] privacyNotices, String consentsType, String portalType) {
        return Arrays.stream(privacyNotices)
                .filter(privacyNoticeDTO -> privacyNoticeDTO.consentsType().equals(consentsType) &&
                        privacyNoticeDTO.portalType().equals(portalType))
                .findFirst();
    }

    private Mono<PrivacyNoticeVersionResponseDto> getVersionFromCache(String consentsType, String portalType,
                                                                      String privacyNoticeId, Integer defaultVersion) {
        log.debug("Retrieve privacyNotice from cache with privacyNoticeId: {}", privacyNoticeId);
        Integer versionInCache = privacyNoticeCache.get(privacyNoticeId);
        if(versionInCache != null) {
            log.info("Privacy Notice found in cache with consentsType: {}, portalType: {}, privacyNoticeId: {}",
                    consentsType, portalType, privacyNoticeId);
            return Mono.just(new PrivacyNoticeVersionResponseDto().version(versionInCache));
        }
        else {
            log.info("Privacy Notice not found in cache use default version: {}", defaultVersion);
            return Mono.just( new PrivacyNoticeVersionResponseDto().version( defaultVersion ) );
        }
    }

    // for testing
    protected ConcurrentHashMap<String, Integer> getPrivacyNoticeCache() {
        return privacyNoticeCache;
    }

}
