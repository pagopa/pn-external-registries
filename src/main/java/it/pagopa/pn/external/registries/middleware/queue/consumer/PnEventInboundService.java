package it.pagopa.pn.external.registries.middleware.queue.consumer;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.external.registries.middleware.queue.consumer.handler.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Configuration
@Slf4j
public class PnEventInboundService {
    private final EventHandler eventHandler;

    public PnEventInboundService(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }
    
    //Viene definita un implementazione (anonima) di MessageRoutingCallback. Nel contesto di Spring, quando viene ricevuto un messaggio da una coda gestita da Spring Cloud Stream,
    // il framework cerca un bean che implementa l'interfaccia MessageRoutingCallback, per richiamarne il metodo routingResult, che dovrà fornirne il nome del bean che gestisce quello
    // specifico messaggio, dunque l'handler per quel messaggio. Spring utilizza il nome del bean per cercare all'interno del proprio contesto  e recuperare l'istanza del bean corrispondente.
    // Questo avviene attraverso il "BeanFactory" di Spring.
    @Bean
    public MessageRoutingCallback customRouter() {
        return new MessageRoutingCallback() {
            @Override
            public FunctionRoutingResult routingResult(Message<?> message) {
                setMdc(message);
                final String handlerName = handleMessage(message);
                return new FunctionRoutingResult(handlerName);
            }
        };
    }

    private void setMdc(Message<?> message) {
        MessageHeaders messageHeaders = message.getHeaders();
        MDCUtils.clearMDCKeys();

        if (messageHeaders.containsKey("aws_messageId")){
            String awsMessageId = messageHeaders.get("aws_messageId", String.class);
            MDC.put(MDCUtils.MDC_PN_CTX_MESSAGE_ID, awsMessageId);
        }

        if (messageHeaders.containsKey("X-Amzn-Trace-Id")){
            String traceId = messageHeaders.get("X-Amzn-Trace-Id", String.class);
            MDC.put(MDCUtils.MDC_TRACE_ID_KEY, traceId);
        } else {
            MDC.put(MDCUtils.MDC_TRACE_ID_KEY, String.valueOf(UUID.randomUUID()));
        }

        String iun = (String) message.getHeaders().get("iun");
        if(iun != null){
            MDC.put(MDCUtils.MDC_PN_IUN_KEY, iun);
        }
    }

    private String handleMessage(Message<?> message) {
        String eventType = (String) message.getHeaders().get("eventType");
        log.info("Received message from customRouter with eventType={}", eventType);

        String iun = (String) message.getHeaders().get("iun");
        
        String handlerName = eventHandler.getHandler().get(eventType);
        if (!StringUtils.hasText(handlerName)) {
            log.error("undefined handler for eventType={}", eventType);
        }

        log.debug("Handler for eventType={} is {} - iun={}", eventType, handlerName, iun);

        return handlerName;
    }
}
