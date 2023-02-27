package it.pagopa.pn.external.registries;

import io.awspring.cloud.autoconfigure.messaging.SqsAutoConfiguration;
import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.api.dto.events.PnExtRegistryIOSentMessageEvent;
import it.pagopa.pn.api.dto.events.PnExtRegistryNotificationPaidEvent;
import it.pagopa.pn.external.registries.services.helpers.OnboardInstitutionFulltextSearchHelper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@EnableAutoConfiguration(exclude = {SqsAutoConfiguration.class})
public class MockAWSObjectsTest {

    @MockBean
    private MomProducer<PnExtRegistryIOSentMessageEvent> iosentmessageProducer;

    @MockBean
    private MomProducer<PnExtRegistryNotificationPaidEvent> notificationPaidProducer;

    @MockBean
    private OnboardInstitutionFulltextSearchHelper onboardInstitutionFulltextSearchHelper;
}
