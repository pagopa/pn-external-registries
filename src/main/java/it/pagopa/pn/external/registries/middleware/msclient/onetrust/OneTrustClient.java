package it.pagopa.pn.external.registries.middleware.msclient.onetrust;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@Component
@Slf4j
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

    public Mono<PrivacyNoticeOneTrustResponse> getPrivacyNoticeVersionByPrivacyNoticeId(String privacyNoticeId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PRIVACY_NOTICES_URL)
                        .queryParam("date", LocalDate.now()) //yyyy.MM.dd
                        .build(privacyNoticeId))
                .retrieve()
                .bodyToMono(PrivacyNoticeOneTrustResponse.class)
                .doOnSuccess(response -> log.info("Response from OneTrust: {}", response))
                .doOnError(throwable -> log.error("Error from OnTrust", throwable));
    }

}
