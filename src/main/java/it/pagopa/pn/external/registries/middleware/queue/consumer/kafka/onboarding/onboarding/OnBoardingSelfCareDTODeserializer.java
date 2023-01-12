package it.pagopa.pn.external.registries.middleware.queue.consumer.kafka.onboarding.onboarding;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class OnBoardingSelfCareDTODeserializer implements Deserializer<OnBoardingSelfCareDTO> {

    private final ObjectMapper objectMapper;

    public OnBoardingSelfCareDTODeserializer() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public OnBoardingSelfCareDTO deserialize(String topic, byte[] data) {
        try {
            if (data == null){
               log.warn("Null received at deserializing");
                return null;
            }
            log.trace("Deserializing from topic: {}...", topic);
            return objectMapper.readValue(data, OnBoardingSelfCareDTO.class);
        } catch (Exception e) {
            throw new SerializationException(String.format("Error when deserializing byte[] to OnBoardingSelfCareDTO with input: %s", new String(data)), e);
        }
    }
}
