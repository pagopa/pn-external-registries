package it.pagopa.pn.external.registries.dto.gpd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GPDPaymentInfoInt {
    private String iuv;
    private String organizationFiscalCode;
    private Integer amount;
    private String description;
    private Boolean isPartialPayment;
    private Instant dueDate;
    private Instant retentionDate;
    private Instant paymentDate;
    private Instant reportingDate;
    private Instant insertedDate;
    private String paymentMethod;
    private Integer fee;
    private Integer notificationFee;
    private String pspCompany;
    private String idReceipt;
    private String idFlowReporting;
    private String status;
    private Instant lastUpdatedDate;
    private List<GPDTransferInt> transfer;
}