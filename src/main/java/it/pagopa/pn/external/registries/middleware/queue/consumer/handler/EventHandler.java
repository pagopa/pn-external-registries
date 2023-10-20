package it.pagopa.pn.external.registries.middleware.queue.consumer.handler;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "pn.external-registries.event")
public class EventHandler {
    private Map<String, String> handler;
}
