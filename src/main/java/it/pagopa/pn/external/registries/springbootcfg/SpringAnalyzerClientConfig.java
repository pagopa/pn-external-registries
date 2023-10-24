package it.pagopa.pn.external.registries.springbootcfg;

import it.pagopa.pn.commons.configs.SpringAnalyzerConfiguration;
import it.pagopa.tech.lollipop.consumer.assertion.client.simple.AssertionSimpleClientConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAnalyzerClientConfig extends SpringAnalyzerConfiguration {}
