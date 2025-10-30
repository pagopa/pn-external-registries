package it.pagopa.pn.external.registries.middleware.msclient.onetrust;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.ONE_TRUST;

@Component
@CustomLog
public class OneTrustClient extends CommonBaseClient {

    //protected static final String PRIVACY_NOTICES_URL = "/api/privacynotice/v2/privacynotices/{privacyNoticeId}";

    protected static final String PRIVACY_NOTICES_URL = "/api/enterprise-policy/v1/privacynotices/{privacyNoticeId}/published-version";

    private WebClient webClient;

    private final String oneTrustBaseUrl;

    private final String oneTrustToken;

    public OneTrustClient(PnExternalRegistriesConfig config) {
        this.oneTrustBaseUrl = config.getOnetrustBaseUrl();
        this.oneTrustToken = config.getOnetrustToken();
    }


    @PostConstruct
    public void init() {
        this.webClient = super.enrichBuilder(WebClient.builder().baseUrl(oneTrustBaseUrl))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + oneTrustToken)
                .build();
    }

    /**
     * Restituisce un privacyNotice con status ACTIVE da un privacyNoticeId.
     * Il query parameter date (required), passato a One Trust, specifica a quale data (versione) di pubblicazione
     * del Privacy Notice si vuole recuperare. Passando il giorno dopo, viene presa sempre l'ultima versione.
     *
     * @param privacyNoticeId identificativo del PrivacyNotice attivo da ricercare
     * @return il Privacy Notice se trovato, altrimenti One Trust restituisce 500
     */
    public Mono<PrivacyNoticeOneTrustResult> getPrivacyNoticeVersionByPrivacyNoticeId(String privacyNoticeId) {
        log.logInvokingExternalDownstreamService(ONE_TRUST, "getPrivacyNoticeVersionByPrivacyNoticeId");
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PRIVACY_NOTICES_URL)
                        .queryParam("date", LocalDate.now().plusDays(1)) //yyyy.MM.dd
                        .build(privacyNoticeId))
                .retrieve()
                .bodyToMono(PrivacyNoticeOneTrustResponseInt.class)
                .doOnSuccess(response -> log.info("Response from OneTrust: {}", response))
                .map(this::mapToPrivacyNoticeResult)
                .doOnError(throwable -> {
                    log.logInvokationResultDownstreamFailed(ONE_TRUST, elabExceptionMessage(throwable));
                    log.error(String.format("Error from OnTrust with privacyNoticeId: %s", privacyNoticeId), throwable);
                });
    }

    @Autowired
    @Override
    public void setRetryMaxAttempts(@Value("${pn.external-registry.onetrust-retry-max-attempts}") int retryMaxAttempts) {
        super.setRetryMaxAttempts(retryMaxAttempts);
    }

    @Autowired
    @Override
    public void setReadTimeoutMillis(@Value("${pn.external-registry.onetrust-read-timeout-millis}") int readTimeoutMillis) {
        super.setReadTimeoutMillis(readTimeoutMillis);
    }

    private PrivacyNoticeOneTrustResult mapToPrivacyNoticeResult(PrivacyNoticeOneTrustResponseInt response){
        //Null Check
        // response.versions().get(0);
        //response.orgGroup()
        //response.approvers().get(0)

        return new PrivacyNoticeOneTrustResult(
                response.versions().get(0).createdDate(),
                response.guid(),
                response.versions().get(0).publishedDate(),
                response.orgGroup().id(),
                response.approvers().get(0).id(),
                new PrivacyNoticeOneTrustResult.Version(
                        response.versions().get(0).id(),
                        response.name(),
                        response.versions().get(0).publishedDate(),
                        response.versions().get(0).versionStatus(),
                        response.versions().get(0).majorVersion()
                )
        );
    }

}
