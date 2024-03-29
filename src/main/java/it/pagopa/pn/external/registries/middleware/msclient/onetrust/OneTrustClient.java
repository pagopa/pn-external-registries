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

    protected static final String PRIVACY_NOTICES_URL = "/api/privacynotice/v2/privacynotices/{privacyNoticeId}";

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
    public Mono<PrivacyNoticeOneTrustResponse> getPrivacyNoticeVersionByPrivacyNoticeId(String privacyNoticeId) {
        log.logInvokingExternalDownstreamService(ONE_TRUST, "getPrivacyNoticeVersionByPrivacyNoticeId");
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PRIVACY_NOTICES_URL)
                        .queryParam("date", LocalDate.now().plusDays(1)) //yyyy.MM.dd
                        .build(privacyNoticeId))
                .retrieve()
                .bodyToMono(PrivacyNoticeOneTrustResponse.class)
                .doOnSuccess(response -> log.info("Response from OneTrust: {}", response))
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

}
