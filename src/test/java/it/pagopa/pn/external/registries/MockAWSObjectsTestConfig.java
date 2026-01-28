package it.pagopa.pn.external.registries;

import io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration;
import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.api.dto.events.PnExtRegistryIOSentMessageEvent;
import it.pagopa.pn.api.dto.events.PnExtRegistryNotificationPaidEvent;
import it.pagopa.pn.external.registries.services.helpers.OnboardInstitutionFulltextSearchHelper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@EnableAutoConfiguration(exclude = {SqsAutoConfiguration.class})
public class MockAWSObjectsTestConfig {

    @MockitoBean
    private MomProducer<PnExtRegistryIOSentMessageEvent> iosentmessageProducer;

    @MockitoBean
    private MomProducer<PnExtRegistryNotificationPaidEvent> notificationPaidProducer;

    @MockitoBean
    private OnboardInstitutionFulltextSearchHelper onboardInstitutionFulltextSearchHelper;

    @MockitoBean
    private SqsAsyncClient sqsAsyncClient;
}