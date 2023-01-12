package it.pagopa.pn.external.registries.middleware.queue.consumer.kafka.onboarding.onboarding;

import it.pagopa.pn.external.registries.mapper.OnBoardSelfCareToOnBoardInstituteEntityMapper;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OnBoardingSelfCareConsumer {

    private final OnboardInstitutionsDao onboardInstitutionsDao;

    private final OnBoardSelfCareToOnBoardInstituteEntityMapper mapper;

    @KafkaListener(id = "${pn.external-registry.kafka-onboarding-group-id}", topics = "${pn.external-registry.kafka-onboarding-topic-name}")
    public void listen(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Payload OnBoardingSelfCareDTO payload) {
        log.info("Received message from topic: {}, with value: {}", topic, payload);

        if(payload != null) {
            OnboardInstitutionEntity entity = mapper.toEntity(payload);
            onboardInstitutionsDao.put(entity).subscribe();
            log.info("Entity saved from topic: {}, with value: {}", topic, entity);
        }

    }

}
