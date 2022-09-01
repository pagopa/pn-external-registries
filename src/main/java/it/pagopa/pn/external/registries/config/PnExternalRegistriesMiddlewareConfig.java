package it.pagopa.pn.external.registries.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.middleware.queue.producer.sqs.SqsNotificationPaidProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class PnExternalRegistriesMiddlewareConfig {
    private final PnExternalRegistriesConfig cfg;

    public PnExternalRegistriesMiddlewareConfig(PnExternalRegistriesConfig cfg) {
        this.cfg = cfg;
    }

    @Bean
    public SqsNotificationPaidProducer sqsNotificationPaidProducer(SqsClient sqs, ObjectMapper objMapper) {
        return new SqsNotificationPaidProducer( sqs, cfg.getTopics().getDeliveryPushInput(), objMapper);
    }

}
