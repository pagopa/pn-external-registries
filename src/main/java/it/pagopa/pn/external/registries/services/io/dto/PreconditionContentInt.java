package it.pagopa.pn.external.registries.services.io.dto;

import lombok.*;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PreconditionContentInt {
    private String title;
    private String markdown;
}
