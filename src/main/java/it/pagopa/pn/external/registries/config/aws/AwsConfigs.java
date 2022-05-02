package it.pagopa.pn.external.registries.config.aws;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@Data
@ToString
@ConfigurationProperties("aws")
public class AwsConfigs {

    private String regionCode;
    private String endpointUrl;
    private String keyARN;

    private String accessKeyId;
    private String secretAccessKey;
}
