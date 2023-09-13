package it.pagopa.pn.external.registries.middleware.msclient.io;

import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.CreatedMessage;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.FiscalCodePayload;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.NewMessage;
import it.pagopa.pn.external.registries.middleware.cloudwatch.CloudWatchMetricHandler;
import it.pagopa.pn.external.registries.middleware.msclient.common.OcpBaseClient;
import lombok.CustomLog;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;

import java.awt.*;
import java.util.UUID;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.IO;

@CustomLog
class IOClient extends OcpBaseClient {

    protected final DefaultApi ioApi;
    private final PnExternalRegistriesConfig config;
    String ioMode;
    private final CloudWatchMetricHandler cloudWatchMetricJob;


    public IOClient(PnExternalRegistriesConfig config, DefaultApi ioApi, String ioMode, CloudWatchMetricHandler cloudWatchMetricHandler) {
        this.config = config;
        this.ioApi = ioApi;
        this.ioMode = ioMode;
        this.cloudWatchMetricJob = cloudWatchMetricHandler;
    }


    public Mono<CreatedMessage> submitMessageforUserWithFiscalCodeInBody(NewMessage message) {
        log.logInvokingExternalService(IO, "submitMessageforUserWithFiscalCodeInBody");
        log.debug("[enter] submitMessageforUserWithFiscalCodeInBody ioMode={} taxId={}", ioMode, LogUtils.maskTaxId(message.getFiscalCode()));

        if (!checkWhitelist(message.getFiscalCode()))
        {
            log.warn("submitMessageforUserWithFiscalCodeInBody taxId {} is not in whitelist, mocking IO response", LogUtils.maskTaxId(message.getFiscalCode()));
            CreatedMessage res = new CreatedMessage();
            res.setId(UUID.randomUUID().toString());
            return Mono.just(res);
        }
        Dimension dimension = Dimension.builder()
                .name("events")
                .value("courtesy-messages")
                .build();
        return ioApi.submitMessageforUserWithFiscalCodeInBody( message )
                .map((response)-> {
                    this.cloudWatchMetricJob.sendMetric(CloudWatchMetricHandler.NAMESPACE_CW_IO, dimension, CloudWatchMetricHandler.IO_SENT_SUCCESSFULLY, 1);
                    return response;
                })
                .onErrorResume(throwable -> {
                    log.error("error submitMessageforUserWithFiscalCodeInBody ioMode={} message={}", ioMode, elabExceptionMessage(throwable), throwable);
                    this.cloudWatchMetricJob.sendMetric(CloudWatchMetricHandler.NAMESPACE_CW_IO, dimension, CloudWatchMetricHandler.IO_SENT_FAILURE, 1);
                    return Mono.error(throwable);
        });
    }


    public Mono<LimitedProfile> getProfileByPOST(FiscalCodePayload payload) {
        log.logInvokingExternalService(IO, "getProfileByPOST");
        log.debug("[enter] getProfileByPOST ioMode={} taxId={}", ioMode, LogUtils.maskTaxId(payload.getFiscalCode()));

        if (!checkWhitelist(payload.getFiscalCode()))
        {
            log.warn("getProfileByPOST taxId {} is not in whitelist, mocking IO response", LogUtils.maskTaxId(payload.getFiscalCode()));
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
