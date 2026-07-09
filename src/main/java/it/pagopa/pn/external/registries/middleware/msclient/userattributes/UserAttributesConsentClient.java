package it.pagopa.pn.external.registries.middleware.msclient.userattributes;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.external.registries.generated.openapi.msclient.userattributes.v1.api.ConsentsApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.userattributes.v1.dto.ConsentAction;
import it.pagopa.pn.external.registries.generated.openapi.msclient.userattributes.v1.dto.ConsentType;
import it.pagopa.pn.external.registries.generated.openapi.msclient.userattributes.v1.dto.CxTypeAuthFleet;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.PN_USER_ATTRIBUTES;

@CustomLog
@Component
@RequiredArgsConstructor
public class UserAttributesConsentClient {

    private final ConsentsApi consentsApi;

    public Mono<Void> acceptConsent(String uid, String cxId, CxTypeAuthFleet cxType, ConsentType consentType, Integer version) {
        log.logInvokingExternalDownstreamService(PN_USER_ATTRIBUTES, "consentAction");

        ConsentAction consentAction = new ConsentAction()
                .action(ConsentAction.ActionEnum.ACCEPT)
                .channel(ConsentAction.ChannelEnum.IO);

        return consentsApi.consentAction(uid, cxId, cxType, consentType, String.valueOf(version), consentAction)
                .doOnError(WebClientResponseException.class, throwable ->
                        log.logInvokationResultDownstreamFailed(PN_USER_ATTRIBUTES, CommonBaseClient.elabExceptionMessage(throwable), throwable));
    }
}
