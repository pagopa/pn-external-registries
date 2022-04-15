package it.pagopa.pn.external.registries.config.aws;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("aws")
public class AwsConfigs {

    private String regionCode;
    private String endpointUrl;
    private String keyARN;

    private String accessKeyId;
    private String secretAccessKey;
}
