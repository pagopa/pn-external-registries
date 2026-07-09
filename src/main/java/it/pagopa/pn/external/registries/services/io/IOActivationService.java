package it.pagopa.pn.external.registries.services.io;

import it.pagopa.pn.external.registries.generated.openapi.msclient.userattributes.v1.dto.ConsentType;
import it.pagopa.pn.external.registries.generated.openapi.msclient.userattributes.v1.dto.CxTypeAuthFleet;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.ActivationDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.ActivationPayloadDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.ActivationStatusDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.FiscalCodePayloadDto;
import it.pagopa.pn.external.registries.mapper.ActivationToActivationDtoMapper;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOCourtesyMessageClient;
import it.pagopa.pn.external.registries.middleware.msclient.userattributes.UserAttributesConsentClient;
import it.pagopa.pn.external.registries.services.PrivacyNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class IOActivationService {

    private static final String PRIVACY_NOTICE_PORTAL_TYPE_PF = "PF";
    private static final String CONSENT_TYPE_TOS = "TOS";
    private static final String CONSENT_TYPE_DATAPRIVACY = "DATAPRIVACY";

    private final IOCourtesyMessageClient client;
    private final PrivacyNoticeService privacyNoticeService;
    private final UserAttributesConsentClient userAttributesConsentClient;


    public IOActivationService(IOCourtesyMessageClient client, PrivacyNoticeService privacyNoticeService, UserAttributesConsentClient userAttributesConsentClient) {
        this.client = client;
        this.privacyNoticeService = privacyNoticeService;
        this.userAttributesConsentClient = userAttributesConsentClient;
    }

    public Mono<ActivationDto> getServiceActivation(Mono<FiscalCodePayloadDto> fiscalCodePayloadDto) {
        return fiscalCodePayloadDto.flatMap(x -> client.getServiceActivation(x.getFiscalCode())
                .map(ActivationToActivationDtoMapper::toDto));
    }

    public Mono<ActivationDto> upsertServiceActivation(Mono<ActivationPayloadDto> activationPayloadDto,
                                                        String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId) {
        CxTypeAuthFleet cxType = CxTypeAuthFleet.fromValue(xPagopaPnCxType.getValue());
        return activationPayloadDto
                .flatMap(x -> {
                    boolean isActive = x.getStatus().equals(ActivationStatusDto.ACTIVE);
                    Mono<Void> consents = isActive
                            ? acceptConsent(xPagopaPnUid, xPagopaPnCxId, cxType, CONSENT_TYPE_TOS)
                                    .then(acceptConsent(xPagopaPnUid, xPagopaPnCxId, cxType, CONSENT_TYPE_DATAPRIVACY))
                            : Mono.empty();
                    return consents.then(client.upsertServiceActivation(x.getFiscalCode(), isActive));
                })
                .map(ActivationToActivationDtoMapper::toDto);
    }

    private Mono<Void> acceptConsent(String uid, String cxId, CxTypeAuthFleet cxType, String consentsType) {
        return privacyNoticeService.findPrivacyNoticeVersion(consentsType, PRIVACY_NOTICE_PORTAL_TYPE_PF)
                .flatMap(privacyNotice -> userAttributesConsentClient.acceptConsent(uid, cxId, cxType, ConsentType.fromValue(consentsType), privacyNotice.getVersion()));
    }
}
