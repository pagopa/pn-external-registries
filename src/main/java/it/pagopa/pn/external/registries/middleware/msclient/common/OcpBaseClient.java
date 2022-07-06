package it.pagopa.pn.external.registries.middleware.msclient.common;

import org.springframework.web.reactive.function.client.WebClient;

public abstract class OcpBaseClient extends BaseClient {
    private static final String HEADER_API_KEY = "Ocp-Apim-Subscription-Key";

    protected WebClient.Builder initWebClient(WebClient.Builder builder, String apiKey){

        return super.initWebClient(builder)
                .defaultHeader(HEADER_API_KEY, apiKey);
    }


}
