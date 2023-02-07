package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.PrivacyNoticeApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PrivacyNoticeVersionResponseDto;
import it.pagopa.pn.external.registries.services.PrivacyNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PrivacyNoticeController implements PrivacyNoticeApi {

    private final PrivacyNoticeService privacyNoticeService;

    @Override
    public Mono<ResponseEntity<PrivacyNoticeVersionResponseDto>> getPrivacyNoticeVersion(String consentsType, String portalType,
                                                                                         String xPagopaPnCxId, final ServerWebExchange exchange) {
        return privacyNoticeService.getPrivacyNoticeVersion(consentsType, portalType)
                .map(ResponseEntity::ok);
    }

}
