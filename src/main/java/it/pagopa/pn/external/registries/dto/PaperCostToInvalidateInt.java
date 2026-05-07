package it.pagopa.pn.external.registries.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class PaperCostToInvalidateInt {
    private String recIndex;
    private List<AnalogUpdateCostPhaseInt> costPhases = new ArrayList<>();
    private Integer vat;
    private String creditorTaxId;
    private String noticeCode;
}
