package it.pagopa.pn.external.registries.dto.gpd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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
