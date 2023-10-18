package it.pagopa.pn.external.registries.middleware.queue.consumer;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.external.registries.dto.deliverypush.UpdateNotificationCost;
import it.pagopa.pn.external.registries.middleware.queue.consumer.handler.DeliveryPushEventHandler;
import it.pagopa.pn.external.registries.middleware.queue.consumer.handler.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
class PnEventInboundServiceTest { 
    //Viene fatto l'@Autowired di MessageRoutingCallback perchè in PnEventInboundService viene fatta un implementazione anonima di questa classe, 
    // iniettandola nel contesto spring con il @Bean. Dunque con l'@Autowired ci riferiamo a quella specifica implementazione
    @Autowired
    private MessageRoutingCallback customRouter;
    
    @Autowired
    private EventHandler eventHandler;

    @Test
    void testCustomRouterForUpdateCostPhase() {
        //GIVEN
        String messageIdKeyValue = "testMessageId";
        String iunValue = "testIun";
        String eventType = "UPDATE_COST_PHASE_EVENT";

        final MessageHeaders headers = getMessageHeaders(messageIdKeyValue, iunValue, eventType);
        final String contentMessage = "testMessage";
        Message<?> fakeMessage = new GenericMessage<>(contentMessage, headers);
        
        // Viene chiamato il metodo Routing result routingResult(), di cui è stato effettuato l'ovveride nella classe anonima definita
        // in PnEventInboundService, per ottenerne il risultato
        MessageRoutingCallback.FunctionRoutingResult result = customRouter.routingResult(fakeMessage);
        
        //Assert headers
        String expectedValueMessageId = MDC.get(MDCUtils.MDC_PN_CTX_MESSAGE_ID);
        String expectedValueIun = MDC.get(MDCUtils.MDC_PN_IUN_KEY);
        String expectedValueTraceId = MDC.get(MDCUtils.MDC_TRACE_ID_KEY);
        Assertions.assertEquals(messageIdKeyValue, expectedValueMessageId);
        Assertions.assertEquals(iunValue, expectedValueIun);
        Assertions.assertNotNull(expectedValueTraceId);
        
        //Assert called function
        assertNotNull(result);
        Map<String, String> handlerMap = eventHandler.getHandler();
        String expectedHandler = handlerMap.get(eventType);
        String handler = result.getFunctionDefinition();
        Assertions.assertEquals(expectedHandler, handler);
    }

    @NotNull
    private static MessageHeaders getMessageHeaders(String messageIdKeyValue, String iunValue, String eventType) {
        Map<String,Object> headersMap = new HashMap<>();
        headersMap.put("aws_messageId", messageIdKeyValue);
        headersMap.put("iun", iunValue);
        headersMap.put("eventType", eventType);
        return new MessageHeaders(headersMap);
    }
}
