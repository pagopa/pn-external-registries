package it.pagopa.pn.external.registries.middleware.db.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class CostComponentsEntity {
    public static final String COL_PK = "pk";
    public static final String COL_SK = "sk";
    public static final String COL_BASECOST = "baseCost";
    public static final String COL_SIMPLE_REGISTERED_LETTER_COST = "simpleRegisteredLetterCost";
    public static final String COL_FIRST_ANALOG_COST = "firstAnalogCost";
    public static final String COL_SECOND_ANALOG_COST = "secondAnalogCost";
    public static final String COL_IS_REFUSED_CANCELLED = "isRefusedCancelled";

    @Setter @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)}))
    private String pk;
    @Setter @Getter(onMethod=@__({@DynamoDbSortKey, @DynamoDbAttribute(COL_SK)}))
    private String sk;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_BASECOST)}))
    private Integer baseCost;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_SIMPLE_REGISTERED_LETTER_COST)}))
    private Integer simpleRegisteredLetterCost;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_FIRST_ANALOG_COST)}))
    private Integer firstAnalogCost;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_SECOND_ANALOG_COST)}))
    private Integer secondAnalogCost;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_IS_REFUSED_CANCELLED)}))
    private Boolean isRefusedCancelled;
}
