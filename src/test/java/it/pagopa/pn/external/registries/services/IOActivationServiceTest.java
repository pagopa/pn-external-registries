package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.Activation;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.CreatedMessage;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.LimitedProfile;
import it.pagopa.pn.external.registries.generated.openapi.server.io.v1.dto.*;
import it.pagopa.pn.external.registries.middleware.msclient.IOActivationClient;
import it.pagopa.pn.external.registries.middleware.msclient.IOClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IOActivationServiceTest {

    @InjectMocks
    private IOActivationService service;

    @Mock
    IOActivationClient ioClient;

    @Mock
    PnExternalRegistriesConfig cfg;


    @Test
    void getServiceActivation() {
        //Given
        FiscalCodePayloadDto req = new FiscalCodePayloadDto();
        req.setFiscalCode("EEEEEE00E00E000A");

        Activation activation = new Activation();
        activation.setServiceId("PN");
        activation.setFiscalCode("EEEEEE00E00E000A");
        activation.setStatus("ACTIVE");
        activation.setVersion(1);

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .amount( 2000 )
                .creditorTaxId( "creditorTaxId" )
                .dueDate( OffsetDateTime.ofInstant( Instant.now(), ZoneId.of( "UTC" ) ) )
                .iun( "iun" )
                .noticeNumber( "noticeNumber" )
                .recipientTaxID( "recipientTaxId" )
                .creditorTaxId( "creditorTaxId" )
                .subject( "subject" );

        //When
        Mockito.when( ioClient.getServiceActivation( Mockito.any() )).thenReturn( Mono.just( activation ) );

        ActivationDto responseDto = service.getServiceActivation( Mono.just( req ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        assertEquals(activation.getFiscalCode(), responseDto.getFiscalCode());
        assertEquals(activation.getVersion(), responseDto.getVersion());
        assertEquals(activation.getStatus(), responseDto.getStatus().getValue());
    }

    @Test
    void upsertServiceActivation() {
        //Given
        ActivationPayloadDto req = new ActivationPayloadDto();
        req.setFiscalCode("EEEEEE00E00E000A");
        req.setStatus(ActivationStatusDto.ACTIVE);

        Activation activation = new Activation();
        activation.setServiceId("PN");
        activation.setFiscalCode("EEEEEE00E00E000A");
        activation.setStatus("ACTIVE");
        activation.setVersion(1);

        SendMessageRequestDto messageRequestDto = new SendMessageRequestDto()
                .amount( 2000 )
                .creditorTaxId( "creditorTaxId" )
                .dueDate( OffsetDateTime.ofInstant( Instant.now(), ZoneId.of( "UTC" ) ) )
                .iun( "iun" )
                .noticeNumber( "noticeNumber" )
                .recipientTaxID( "recipientTaxId" )
                .creditorTaxId( "creditorTaxId" )
                .subject( "subject" );

        //When
        Mockito.when( ioClient.upsertServiceActivation( Mockito.any(), Mockito.anyBoolean() )).thenReturn( Mono.just( activation ) );

        ActivationDto responseDto = service.upsertServiceActivation( Mono.just( req ) ).block();

        //Then
        Assertions.assertNotNull( responseDto );
        assertEquals(activation.getFiscalCode(), responseDto.getFiscalCode());
        assertEquals(activation.getVersion(), responseDto.getVersion());
        assertEquals(activation.getStatus(), responseDto.getStatus().getValue());
    }


}