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

    private AccessTokenConfig pdndM2m;

    private String pdndServerURL;


//    private String anprJWTHeaderDigestCertChains;
//    private String anprJWTHeaderDigestPassword;
//    private String anprServerURL;

    private String anprX509CertificateChain;
    private String anprJWTHeaderDigestKeystoreAlias;

}
