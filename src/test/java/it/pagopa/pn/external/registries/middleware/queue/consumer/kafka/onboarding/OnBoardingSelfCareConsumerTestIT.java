package it.pagopa.pn.external.registries.middleware.queue.consumer.kafka.onboarding;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.LocalStackTestConfig;
import it.pagopa.pn.external.registries.mapper.OnBoardSelfCareToOnBoardInstituteEntityMapper;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.middleware.queue.consumer.kafka.onboarding.onboarding.OnBoardingSelfCareConsumer;
import it.pagopa.pn.external.registries.middleware.queue.consumer.kafka.onboarding.onboarding.OnBoardingSelfCareDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.ExecutionException;

@SpringBootTest(properties = {
        "spring.kafka.consumer.bootstrap-servers=PLAINTEXT://localhost:9092",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "pn.external-registry.kafka-pagamenti-group-id=consumer-test",
        "spring.kafka.consumer.properties.security.protocol=PLAINTEXT"
})
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@Import(LocalStackTestConfig.class)
class OnBoardingSelfCareConsumerTestIT {

    @SpyBean
    private OnBoardingSelfCareConsumer onBoardingSelfCareConsumer;

    @Autowired
    private OnboardInstitutionsDao onboardInstitutionsDao;

    @Autowired
    private OnBoardSelfCareToOnBoardInstituteEntityMapper mapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listenOKTest() throws ExecutionException, InterruptedException, JsonProcessingException {
        String inputRequest = inputRequestFormSelfCare();

        //scrivo su Kafka una Request presa dal flusso reale di SelfCare
        kafkaTemplate.send("sc-contracts", inputRequest).get();

        OnBoardingSelfCareDTO expectedValue = objectMapper.readValue(inputRequest, OnBoardingSelfCareDTO.class);

        //verifico che il consumer riceva correttamente il messaggio (e quindi il deserializer funzioni)
        Mockito.verify(onBoardingSelfCareConsumer, Mockito.timeout(1000).times(1)).listen("sc-contracts", expectedValue);

        //verifico che alla fine del flusso, il consumer abbia scritto su Dynamo, verificando che il record sia presente
        Mono<OnboardInstitutionEntity> onboardInstitutionEntityMono = onboardInstitutionsDao.get("7861b02d-8cb4-4de9-95d2-5ed02f3de38a");

        StepVerifier.create(onboardInstitutionEntityMono)
                .expectNext(mapper.toEntity(expectedValue))
                .verifyComplete();

        //clean dynamodb
        onboardInstitutionsDao.delete("7861b02d-8cb4-4de9-95d2-5ed02f3de38a").subscribe();
    }

    private String inputRequestFormSelfCare() {
        return """
                {
                   "billing":{
                      "recipientCode":"bc_0432",
                      "vatNumber":"00338460090"
                   },
                   "contentType":"application/octet-stream",
                   "fileName":"App IO_accordo_adesione.pdf7419256794741715935.pdf",
                   "filePath":"parties/docs/7014954b-5a2f-4aed-9f26-b2b778c2a120/App IO_accordo_adesione.pdf7419256794741715935.pdf",
                   "id":"7014954b-5a2f-4aed-9f26-b2b778c2a120",
                   "institution":{
                      "address":"Piazza Umberto I, 1",
                      "description":"Comune di Tovo San Giacomo",
                      "digitalAddress":"protocollo@comunetovosangiacomo.it",
                      "institutionType":"PA",
                      "origin":"IPA",
                      "originId":"c_l315",
                      "taxCode":"00338460090"
                   },
                   "internalIstitutionID":"7861b02d-8cb4-4de9-95d2-5ed02f3de38a",
                   "onboardingTokenId":"7014954b-5a2f-4aed-9f26-b2b778c2a120",
                   "product":"prod-io",
                   "state":"ACTIVE",
                   "updatedAt":"2023-01-10T15:20:38.94Z"
                }
                """;
    }

}
