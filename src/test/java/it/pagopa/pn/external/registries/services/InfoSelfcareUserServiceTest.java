package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserInstitutionResourceDto;
import it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.UserResponseDto;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePaInstitutionClient;
import it.pagopa.pn.external.registries.middleware.msclient.SelfcarePgInstitutionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ContextConfiguration(classes = {InfoSelfcareUserService.class})
@ExtendWith(SpringExtension.class)
class InfoSelfcareUserServiceTest {

    @Autowired
    private InfoSelfcareUserService userService;

    @MockitoBean
    private SelfcarePgInstitutionClient selfcarePgInstitutionClient;

    @MockitoBean
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

    @Test
    void getPgUserDetailsReturnsUserDetails() {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId("id");
        userResponseDto.setTaxCode("taxCode");


        Mockito.when(selfcarePgInstitutionClient.retrieveUserDetail(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.just(userResponseDto));

        StepVerifier.create(userService.getPgUserDetails("xPagopaPnUid", "xPagopaPnCxId"))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getPgUserDetailsReturnsEmptyWhenUserNotFound() {
        Mockito.when(selfcarePgInstitutionClient.retrieveUserDetail(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(userService.getPgUserDetails("xPagopaPnUid", "xPagopaPnCxId"))
                .verifyComplete();
    }

    @Test
    void getPgUserDetailsHandlesError() {
        Mockito.when(selfcarePgInstitutionClient.retrieveUserDetail(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.error(new RuntimeException("Internal Server Error")));

        StepVerifier.create(userService.getPgUserDetails("xPagopaPnUid", "xPagopaPnCxId"))
                .expectError(RuntimeException.class)
                .verify();
    }
}
