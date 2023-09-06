package it.pagopa.pn.external.registries.middleware.msclient.io;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.api.DefaultApi;
import it.pagopa.pn.external.registries.middleware.cloudwatch.CloudWatchMetricHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IOOptInClient extends IOClient {

    //inject by name
    public IOOptInClient(PnExternalRegistriesConfig config, DefaultApi ioActApi, CloudWatchMetricHandler cloudWatchMetricJob)
    {
        super(config, ioActApi, "OptIn", cloudWatchMetricJob);
    }
}
