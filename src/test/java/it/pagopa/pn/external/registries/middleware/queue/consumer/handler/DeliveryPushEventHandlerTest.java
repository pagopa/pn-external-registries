package it.pagopa.pn.external.registries.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.dto.CommunicationResultGroupInt;
import it.pagopa.pn.external.registries.dto.CostUpdateCostPhaseInt;
import it.pagopa.pn.external.registries.dto.UpdateCostResponseInt;
import it.pagopa.pn.external.registries.dto.deliverypush.AnalogUpdateCostPhase;
import it.pagopa.pn.external.registries.dto.deliverypush.UpdateNotificationCost;
import it.pagopa.pn.external.registries.services.CostUpdateOrchestratorService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class DeliveryPushEventHandlerTest {
    @InjectMocks
    private DeliveryPushEventHandler actionHandler;
    @Mock
    private CostUpdateOrchestratorService costUpdateOrchestratorService;
    
    @Test
    void pnDeliveryPushUpdateCostEventConsumer(){
        //GIVEN
        Message<UpdateNotificationCost> message = getActionMessage();
        UpdateNotificationCost updateNotificationCost = message.getPayload();

        UpdateCostResponseInt response1 = UpdateCostResponseInt.builder()
                .creditorTaxId("testCredTaxId")
                .noticeCode("testNoticeCode")
                .recIndex(0)
                .result(CommunicationResultGroupInt.OK)
                .build();
        List<UpdateCostResponseInt> updateCostResponseList = Collections.singletonList(response1);
        
        Mockito.when(costUpdateOrchestratorService.handleCostUpdateForIun(
                updateNotificationCost.getVat(),
                updateNotificationCost.getNotificationStepCost(),
                updateNotificationCost.getIun(),
                updateNotificationCost.getRecIndex(),
                updateNotificationCost.getEventTimestamp(),
                updateNotificationCost.getEventStorageTimestamp(),
                CostUpdateCostPhaseInt.valueOf(updateNotificationCost.getUpdateCostPhase().getValue())
        )).thenReturn(Flux.fromIterable(updateCostResponseList));
        
        //WHEN
        Consumer<Message<UpdateNotificationCost>> consumer = actionHandler.pnDeliveryPushUpdateCostEventConsumer();
        Assertions.assertDoesNotThrow(() -> consumer.accept(message));
    }

    @Test
    void pnDeliveryPushUpdateCostEventConsumerKO(){
        //GIVEN
        Message<UpdateNotificationCost> message = getActionMessage();
        UpdateNotificationCost updateNotificationCost = message.getPayload();

        UpdateCostResponseInt response1 = UpdateCostResponseInt.builder()
                .creditorTaxId("testCredTaxId")
                .noticeCode("testNoticeCode")
                .recIndex(0)
                .result(CommunicationResultGroupInt.KO)
                .build();
        List<UpdateCostResponseInt> updateCostResponseList = Collections.singletonList(response1);

        Mockito.when(costUpdateOrchestratorService.handleCostUpdateForIun(
                updateNotificationCost.getVat(),
                updateNotificationCost.getNotificationStepCost(),
                updateNotificationCost.getIun(),
                updateNotificationCost.getRecIndex(),
                updateNotificationCost.getEventTimestamp(),
                updateNotificationCost.getEventStorageTimestamp(),
                CostUpdateCostPhaseInt.valueOf(updateNotificationCost.getUpdateCostPhase().getValue())
        )).thenReturn(Flux.fromIterable(updateCostResponseList));

        //WHEN
        Consumer<Message<UpdateNotificationCost>> consumer = actionHandler.pnDeliveryPushUpdateCostEventConsumer();
        Assertions.assertDoesNotThrow(() -> consumer.accept(message));
    }

    @Test
    void pnDeliveryPushUpdateCostEventConsumerRetry(){
        //GIVEN
        Message<UpdateNotificationCost> message = getActionMessage();
        UpdateNotificationCost updateNotificationCost = message.getPayload();

        UpdateCostResponseInt response1 = UpdateCostResponseInt.builder()
                .creditorTaxId("testCredTaxId")
                .noticeCode("testNoticeCode")
                .recIndex(0)
                .result(CommunicationResultGroupInt.RETRY)
                .build();
        List<UpdateCostResponseInt> updateCostResponseList = Collections.singletonList(response1);

        Mockito.when(costUpdateOrchestratorService.handleCostUpdateForIun(
                updateNotificationCost.getVat(),
                updateNotificationCost.getNotificationStepCost(),
                updateNotificationCost.getIun(),
                updateNotificationCost.getRecIndex(),
                updateNotificationCost.getEventTimestamp(),
                updateNotificationCost.getEventStorageTimestamp(),
                CostUpdateCostPhaseInt.valueOf(updateNotificationCost.getUpdateCostPhase().getValue())
        )).thenReturn(Flux.fromIterable(updateCostResponseList));

        //WHEN
        Consumer<Message<UpdateNotificationCost>> consumer = actionHandler.pnDeliveryPushUpdateCostEventConsumer();
        Assertions.assertThrows(PnInternalException.class, ()-> consumer.accept(message));
    }
    
    @NotNull
    private static Message<UpdateNotificationCost> getActionMessage() {
        return new Message<>() {
            @Override
            @NotNull
            public UpdateNotificationCost getPayload() {
                return UpdateNotificationCost.builder()
                        .iun("test_IUN")
                        .recIndex(0)
                        .updateCostPhase(AnalogUpdateCostPhase.SEND_SIMPLE_REGISTERED_LETTER)
                        .build();
            }
            @Override
            @NotNull
            public MessageHeaders getHeaders() {
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("eventId", "eventIdTest");
                return new MessageHeaders(hashMap);
            }
        };
    }

    @NotNull
    private static Message<UpdateNotificationCost> getActionMessageError() {
        return new Message<>() {
            @Override
            public UpdateNotificationCost getPayload() {
                return null;
            }
            @Override
            @NotNull
            public MessageHeaders getHeaders() {
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("eventId", "eventIdTest");
                return new MessageHeaders(hashMap);
            }
        };
    }

}