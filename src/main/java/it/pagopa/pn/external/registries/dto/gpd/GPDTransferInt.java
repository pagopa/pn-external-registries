package it.pagopa.pn.external.registries.dto.gpd;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode
public class GPDTransferInt {
    private String organizationFiscalCode;
    private String idTransfer;
    private Integer amount;
    // remittanceInformation not present, because it could contain sensitive information
    private String category;
    private String iban;
    private String postalIban;
    private GPDStampInt stamp;
    private Instant insertedDate;
    private String status;
    private Instant lastUpdatedDate;
}
