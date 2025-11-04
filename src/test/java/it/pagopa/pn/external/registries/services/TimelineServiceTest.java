package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.timelineservice.DeliveryInformationResponseInt;
import it.pagopa.pn.external.registries.generated.openapi.msclient.timelineservice.v1.dto.DeliveryInformationResponse;
import it.pagopa.pn.external.registries.generated.openapi.msclient.timelineservice.v1.dto.ExtendedDeliveryMode;
import it.pagopa.pn.external.registries.middleware.msclient.TimelineServiceClient;
import it.pagopa.pn.external.registries.services.bottomsheet.DeliveryModeInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

class TimelineServiceTest {
    private TimelineServiceClient timelineServiceClient;
    private TimelineService timelineService;

    @BeforeEach
    void setUp() {
        timelineServiceClient = Mockito.mock(TimelineServiceClient.class);
        timelineService = new TimelineService(timelineServiceClient);
    }

    @Test
    void getDeliveryInformation_shouldReturnMappedResponse() {
        String iun = "test-iun";
        Integer recIndex = 1;
        Instant dateTime = Instant.now();

        DeliveryInformationResponse extResponse = new DeliveryInformationResponse();
        extResponse.setSchedulingAnalogDate(dateTime);
        extResponse.setRefinementOrViewedDate(dateTime);
        extResponse.setDeliveryMode(ExtendedDeliveryMode.DIGITAL);

        DeliveryInformationResponseInt intResponse = DeliveryInformationResponseInt.builder()
                .schedulingAnalogDate(dateTime)
                .refinementOrViewedDate(dateTime)
                .deliveryMode(DeliveryModeInt.DIGITAL)
                .build();

        Mockito.when(timelineServiceClient.getDeliveryInformation(iun, recIndex))
                .thenReturn(Mono.just(extResponse));

        StepVerifier.create(timelineService.getDeliveryInformation(iun, recIndex))
                .expectNext(intResponse)
                .verifyComplete();
    }
  
}