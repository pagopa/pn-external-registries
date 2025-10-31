package it.pagopa.pn.external.registries.services.io.dto;

import lombok.*;

import java.util.Map;

@Getter
@Builder(toBuilder = true)
@Data
public class PreconditionContentInt {
    private String messageCode;
    private Map<String, String> messageParams;
    private String title;
    private String markdown;
}
