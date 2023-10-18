package it.pagopa.pn.external.registries.middleware.msclient.io;

import io.micrometer.core.instrument.MeterRegistry;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.api.DefaultApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IOOptInClient extends IOClient {

    //inject by name
    public IOOptInClient(PnExternalRegistriesConfig config, DefaultApi ioActApi, MeterRegistry meterRegistry)
    {
        super(config, ioActApi, "OptIn", meterRegistry);
    }
}
