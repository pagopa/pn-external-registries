package it.pagopa.pn.external.registries.config;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "pn-er")
@Slf4j
@Data
@ToString
public class PnExternalRegistriesConfig {

    private String pdndM2MIssuer;
    private String pdndM2MSubjec;
    private String pdndM2MAudience;
    private String pdnpM2MKid;
    private String pdndServerURL;
    private String pdndM2MClientAssertionType;
    private String pdndM2MGrantType;
    private String pdndM2MClientId;

}
