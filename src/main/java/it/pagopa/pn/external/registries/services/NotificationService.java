package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.dto.delivery.NotificationInt;
import it.pagopa.pn.external.registries.mapper.NotificationMapper;
import it.pagopa.pn.external.registries.middleware.msclient.DeliveryClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class NotificationService {
    private final DeliveryClient deliveryClient;

    public Mono<NotificationInt> getSentNotificationPrivate(String iun) {
        return deliveryClient.getSentNotificationPrivate(iun)
                .map(NotificationMapper::externalToInternal);
    }
}
