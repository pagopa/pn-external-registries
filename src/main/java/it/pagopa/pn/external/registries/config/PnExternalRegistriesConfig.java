package it.pagopa.pn.external.registries.config;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
@ConfigurationProperties(prefix = "pn.external-registry")
@Slf4j
@Data
@ToString
public class PnExternalRegistriesConfig {

    public static final String PDND_M2M_TOKEN = "pdnd";

    private Map<String,AccessTokenConfig> accessTokens = new HashMap<>();

    private String pdndServerUrl;

    private String anprX509CertificateChain;
    private String anprJWTHeaderDigestKeystoreAlias;

}
