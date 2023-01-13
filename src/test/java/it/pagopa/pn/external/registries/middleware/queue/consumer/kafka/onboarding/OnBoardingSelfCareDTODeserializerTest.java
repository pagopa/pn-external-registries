package it.pagopa.pn.external.registries.middleware.queue.consumer.kafka.onboarding;

import it.pagopa.pn.external.registries.middleware.queue.consumer.kafka.onboarding.onboarding.OnBoardingSelfCareDTO;
import it.pagopa.pn.external.registries.middleware.queue.consumer.kafka.onboarding.onboarding.OnBoardingSelfCareDTODeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OnBoardingSelfCareDTODeserializerTest {


    private OnBoardingSelfCareDTODeserializer deserializer;

    @BeforeEach
    public void init() {
        deserializer = new OnBoardingSelfCareDTODeserializer();
    }


    @Test
    void deserializeTest() {
        String requestFromSelfCare = inputRequestFormSelfCare();

        OnBoardingSelfCareDTO actual = deserializer.deserialize("topic", requestFromSelfCare.getBytes());

        assertThat(actual).isNotNull();
        assertThat(actual.getInternalIstitutionID()).isEqualTo("7861b02d-8cb4-4de9-95d2-5ed02f3de38a");
        assertThat(actual.getState()).isEqualTo("ACTIVE");
        assertThat(actual.getUpdatedAt()).isEqualTo(Instant.parse("2023-01-10T15:20:38.94Z"));
        assertThat(actual.getInstitution().getAddress()).isEqualTo("Piazza Umberto I, 1");
        assertThat(actual.getInstitution().getDigitalAddress()).isEqualTo("protocollo@comunetovosangiacomo.it");
        assertThat(actual.getInstitution().getTaxCode()).isEqualTo("00338460090");
        assertThat(actual.getInstitution().getDescription()).isEqualTo("Comune di Tovo San Giacomo");
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
