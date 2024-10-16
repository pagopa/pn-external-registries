package it.pagopa.pn.external.registries.middleware.db.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CostComponentsEntity {
    public static final String COL_PK = "pk";
    public static final String COL_SK = "sk";
    public static final String COL_BASECOST = "baseCost";
    public static final String COL_SIMPLE_REGISTERED_LETTER_COST = "simpleRegisteredLetterCost";
    public static final String COL_FIRST_ANALOG_COST = "firstAnalogCost";
    public static final String COL_SECOND_ANALOG_COST = "secondAnalogCost";
    public static final String COL_IS_REFUSED_CANCELLED = "isRefusedCancelled";
    public static final String COL_VAT = "vat";

    @Setter @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)}))
    private String pk; // iun + "##" + recIndex
    @Setter @Getter(onMethod=@__({@DynamoDbSortKey, @DynamoDbAttribute(COL_SK)}))
    private String sk; // creditorTaxId + "##" + noticeCode

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_BASECOST)}))
    private Integer baseCost = 0;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_SIMPLE_REGISTERED_LETTER_COST)}))
    private Integer simpleRegisteredLetterCost = null;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_FIRST_ANALOG_COST)}))
    private Integer firstAnalogCost = null;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_SECOND_ANALOG_COST)}))
    private Integer secondAnalogCost = null;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_IS_REFUSED_CANCELLED)}))
    private Boolean isRefusedCancelled = false;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_VAT)}))
    private Integer vat = null;

}
