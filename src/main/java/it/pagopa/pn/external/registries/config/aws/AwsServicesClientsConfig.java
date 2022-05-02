package it.pagopa.pn.external.registries.config.aws;

import it.pagopa.pn.external.registries.config.RuntimeMode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty( name = "pn.external.registries.init.aws", havingValue = "true")
@Slf4j
public class AwsServicesClientsConfig {

    private final AwsConfigs props;

    public AwsServicesClientsConfig(AwsConfigs props, RuntimeMode runtimeMode) {
        this.props = props;
        setAwsCredentialPropertiesInSystem();
        log.info("create aws service: props -> "+ props + " - runtime mode -> "+ runtimeMode );
/*
       if( RuntimeMode.DEVELOPMENT.equals( runtimeMode) ) {
            setAwsCredentialPropertiesInSystem();
       }
       */

    }

    private void setAwsCredentialPropertiesInSystem() {
        if( StringUtils.isNotBlank( props.getAccessKeyId() ) ) {
            System.setProperty( "aws.accessKeyId", props.getAccessKeyId() );
        }
        if( StringUtils.isNotBlank( props.getSecretAccessKey() ) ) {
            System.setProperty( "aws.secretAccessKey", props.getSecretAccessKey() );
        }
        if( StringUtils.isNotBlank( props.getRegionCode() ) ) {
            System.setProperty( "aws.region-code", props.getRegionCode());
        }
        if( StringUtils.isNotBlank( props.getKeyARN() ) ) {
            System.setProperty( "aws.keyARN", props.getKeyARN() );
        }

    }

}
