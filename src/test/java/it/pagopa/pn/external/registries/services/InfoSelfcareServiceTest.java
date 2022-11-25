package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.exceptions.PnPANotFoundException;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.dto.InstitutionDto;
import it.pagopa.pn.external.registries.generated.openapi.selfcare.external.client.v1.dto.InstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcareInstitutionsClient;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcareUserGroupClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@SpringBootTest
@ActiveProfiles("test")
class InfoSelfcareServiceTest {

    private final Duration d = Duration.ofMillis(3000);

    @Autowired
    private InfoSelfcareInstitutionsService service;

    @MockBean
    private SelfcareInstitutionsClient selfcareInstitutionsClient;

    @MockBean
    private SelfcareUserGroupClient selfcareUserGroupClient;

    @Test
    void getOnePa() {
        //GIVEN
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        InstitutionDto inst = new InstitutionDto();
        inst.setId(UUID.fromString(id));
        inst.setDescription("Comune di Milano");
        // inst.setStatus("ACTIVE");
        inst.setTaxCode("123456789");

        Mockito.when(selfcareInstitutionsClient.getInstitution(Mockito.anyString())).thenReturn(Mono.just(inst));

        // WHEN
        PaInfoDto res = service.getOnePa(id).block();


        //THEN
        assertNotNull(res);
        assertEquals("Comune di Milano", res.getName());
        assertEquals(id, res.getId());
    }

    @Test
    void getOnePaNotExist() {
        //GIVEN
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        Mockito.when(selfcareInstitutionsClient.getInstitution(Mockito.anyString())).thenReturn(Mono.error(new WebClientResponseException(404, null,null,null,null )));


        // WHEN
        Mono<PaInfoDto> mono =service.getOnePa(id);
        assertThrows(PnPANotFoundException.class, () -> {
            mono.block(d);
        });
        //THEN
    }

    @Test
    void listOnboardedPaByName() {
        //GIVEN
        String name = "comune";
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        InstitutionResourceDto inst = new InstitutionResourceDto();
        inst.setId(UUID.fromString(id));
        inst.setDescription("Comune di Milano");
        inst.setStatus("ACTIVE");
        inst.setTaxCode("123456789");

        List<InstitutionResourceDto> list = new ArrayList<>();
        list.add(inst);

        Mockito.when(selfcareInstitutionsClient.getInstitutions()).thenReturn(Flux.fromIterable(list));


        // WHEN
        List<PaSummaryDto> res = service.listOnboardedPaByName(name).collectList().block();


        //THEN
        assertNotNull(res);
        assertEquals("Comune di Milano", res.get(0).getName());
    }

    @Test
    void listOnboardedPaByIds() {
        //GIVEN
        String id = "d0d28367-1695-4c50-a260-6fda526e9aab";
        InstitutionResourceDto inst = new InstitutionResourceDto();
        inst.setId(UUID.fromString(id));
        inst.setDescription("Comune di Milano");
        inst.setStatus("ACTIVE");
        inst.setTaxCode("123456789");

        List<InstitutionResourceDto> list = new ArrayList<>();
        list.add(inst);

        Mockito.when(selfcareInstitutionsClient.getInstitutions()).thenReturn(Flux.fromIterable(list));

        // WHEN
        List<PaSummaryDto> res = service.listOnboardedPaByIds(List.of(id)).collectList().block();


        //THEN
        assertNotNull(res);
        assertEquals("Comune di Milano", res.get(0).getName());
    }

}
