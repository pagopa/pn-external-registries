package it.pagopa.pn.external.registries.middleware.msclient.common;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public abstract class OcpBaseClient extends CommonBaseClient {
    private static final String HEADER_API_KEY = "Ocp-Apim-Subscription-Key";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    
    protected WebClient.Builder initWebClient(WebClient.Builder builder, String apiKey){

        return super.enrichBuilder(builder)
                .defaultHeader(HEADER_API_KEY, apiKey);
    }

    protected WebClient.Builder initWebClient(WebClient.Builder builder, String apiKey, String requestId){

        return super.enrichBuilder(builder)
                .defaultHeader(HEADER_API_KEY, apiKey)
                .defaultHeader(HEADER_REQUEST_ID, requestId);
    }
}
