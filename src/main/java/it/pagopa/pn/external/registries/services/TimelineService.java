package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.timelineservice.DeliveryInformationResponseInt;
import it.pagopa.pn.external.registries.mapper.TimelineServiceMapper;
import it.pagopa.pn.external.registries.middleware.msclient.TimelineServiceClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class TimelineService {
    private final TimelineServiceClient timelineServiceClient;

    public Mono<DeliveryInformationResponseInt> getDeliveryInformation(String iun, Integer recIndex) {
        log.info("Invoking TimelineService to get delivery information for iun={}", iun);
        return timelineServiceClient.getDeliveryInformation(iun, recIndex)
                .map(TimelineServiceMapper::externalToInternal);
    }
}
