package it.pagopa.pn.external.registries.middleware.queue.consumer.handler;

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

import java.util.HashMap;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
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

        //WHEN
        Consumer<Message<UpdateNotificationCost>> consumer = actionHandler.pnDeliveryPushUpdateCostEventConsumer();
        consumer.accept(message);

        //THEN
        verify(costUpdateOrchestratorService).handleCostUpdate();
    }

    @Test
    void pnDeliveryPushUpdateCostEventConsumerError(){
        //GIVEN
        Message<UpdateNotificationCost> message = getActionMessageError();

        //WHEN
        Consumer<Message<UpdateNotificationCost>> consumer = actionHandler.pnDeliveryPushUpdateCostEventConsumer();
        
        Assertions.assertThrows(NullPointerException.class, () -> consumer.accept(message));

        //THEN
        verify(costUpdateOrchestratorService, never()).handleCostUpdate();
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