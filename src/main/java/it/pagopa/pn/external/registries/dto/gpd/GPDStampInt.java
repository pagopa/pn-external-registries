package it.pagopa.pn.external.registries.dto.gpd;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class GPDStampInt {
    private String hashDocument;
    private String stampType;
    private String provincialResidence;
}
