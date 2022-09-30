package it.pagopa.pn.external.registries.middleware.msclient.io;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IOOptInClient extends IOClient {

    public IOOptInClient(PnExternalRegistriesConfig config)
    {
        super(config, config.getIoactApiKey(), "OptIn");
    }
}
