package it.pagopa.pn.external.registries.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.Map;


@Configuration
@ConfigurationProperties(prefix = "pn.external-registry")
@Slf4j
@Data
@ToString
@Import(SharedAutoConfiguration.class)
public class PnExternalRegistriesConfig {

    public static final String PDND_M2M_TOKEN = "pdnd";

    private Map<String,AccessTokenConfig> accessTokens = new HashMap<>();

    private String pdndServerUrl;

    private String anprX509CertificateChain;
    private String anprJWTHeaderDigestKeystoreAlias;

    private String checkoutApiKey;
    private String checkoutBaseUrl;

    private String ioApiKey;
    private String ioBaseUrl;

    private boolean enableIoMessage;

    private String selfcareApiKey;
    private String selfcareBaseUrl;
    private String selfcarePnProductId;
    private String selfcareUid;

    private String mockDataResources;
}
