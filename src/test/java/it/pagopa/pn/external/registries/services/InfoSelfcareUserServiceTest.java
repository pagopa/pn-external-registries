package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserInstitutionResourceDto;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePaInstitutionClient;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePgInstitutionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ContextConfiguration(classes = {InfoSelfcareUserService.class})
@ExtendWith(SpringExtension.class)
class InfoSelfcareUserServiceTest {

    @Autowired
    private InfoSelfcareUserService userService;

    @MockBean
    private SelfcarePgInstitutionClient selfcarePgInstitutionClient;

    @MockBean
    private SelfcarePaInstitutionClient selfcarePaInstitutionClient;

    @Test
    void getPgUserData(){
        UserInstitutionResourceDto userInstitutionResourceDto = new UserInstitutionResourceDto();
        userInstitutionResourceDto.setUserId("xPagopaPnUid");
        userInstitutionResourceDto.setInstitutionId("xPagopaPnCxId");

        Mockito.when(selfcarePgInstitutionClient.retrieveUserInstitution(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.just(userInstitutionResourceDto));

        StepVerifier.create(userService.getPgUserData("xPagopaPnUid", "xPagopaPnCxId"))
                .expectNextCount(1)
                .verifyComplete();
    }
}
