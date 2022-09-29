package it.pagopa.pn.external.registries.middleware.msclient.io;

import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.ApiClient;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.CreatedMessage;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.FiscalCodePayload;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.NewMessage;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Slf4j
class IOClient extends OcpBaseClient {

    protected DefaultApi ioApi;
    private final String apiKey;
    private final PnExternalRegistriesConfig config;

    public IOClient(PnExternalRegistriesConfig config, String apiKey) {
        this.config = config;
        this.apiKey = apiKey;
    }

    @PostConstruct
    public void init() {

        ApiClient apiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder(), apiKey).build());
        apiClient.setBasePath( config.getIoBaseUrl() );

        this.ioApi = new DefaultApi( apiClient );
    }

    public Mono<CreatedMessage> submitMessageforUserWithFiscalCodeInBody(NewMessage message) {
        log.info("[enter] submitMessageforUserWithFiscalCodeInBody taxId={}", LogUtils.maskTaxId(message.getFiscalCode()));

        if (!checkWhitelist(message.getFiscalCode()))
        {
            log.warn("submitMessageforUserWithFiscalCodeInBody taxId is not in whitelist, mocking IO response");
            CreatedMessage res = new CreatedMessage();
            res.setId(UUID.randomUUID().toString());
            return Mono.just(res);
        }

        return ioApi.submitMessageforUserWithFiscalCodeInBody( message ).onErrorResume(throwable -> {
            log.error("error submitMessageforUserWithFiscalCodeInBody message={}", elabExceptionMessage(throwable), throwable);
            return Mono.error(throwable);
        });
    }


    public Mono<LimitedProfile> getProfileByPOST(FiscalCodePayload payload) {
        log.info("[enter] getProfileByPOST taxId={}", LogUtils.maskTaxId(payload.getFiscalCode()));

        if (!checkWhitelist(payload.getFiscalCode()))
        {
            log.warn("getProfileByPOST taxId is not in whitelist, mocking IO response");
            WebClientResponseException res = WebClientResponseException.create(404, "not found (mocked)", HttpHeaders.EMPTY, new byte[0], null);
            return Mono.error(res);
        }

        return ioApi.getProfileByPOST( payload );
    }

    protected boolean checkWhitelist(String taxId)
    {
        return  (CollectionUtils.isEmpty(config.getIoWhitelist()) || config.getIoWhitelist().get(0).equals("*") || config.getIoWhitelist().contains(taxId));
    }
}
