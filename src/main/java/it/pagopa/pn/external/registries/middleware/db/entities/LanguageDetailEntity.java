package it.pagopa.pn.external.registries.middleware.db.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;
import java.util.Map;

@DynamoDbBean
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class LanguageDetailEntity {

    public static final String COL_PK = "pk";
    private static final String COL_SK = "sk";
    private static final String COL_VALUE = "value";
    private static final String COL_CREATED_AT = "createdAt";
    private static final String COL_UPDATED_AT = "updatedAt";

    @Setter@Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)}))  private String pk;
    @Setter @Getter(onMethod=@__({@DynamoDbSortKey, @DynamoDbAttribute(COL_SK)}))  private String sk;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_VALUE)}))  private Map<String,String> value;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_CREATED_AT)}))  private Instant createdAt;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_UPDATED_AT)}))  private Instant updatedAt;
    
}
