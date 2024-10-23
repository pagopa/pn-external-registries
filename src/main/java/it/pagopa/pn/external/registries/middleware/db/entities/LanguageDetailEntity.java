package it.pagopa.pn.external.registries.middleware.db.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;
import java.util.Optional;



@DynamoDbBean
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class LanguageDetailEntity {


    public static final String COL_PK = "hashKey";
    public static final String COL_SK = "sortKey";
    public static final String COL_CREATED_AT = "createdAt";
    public static final String COL_UPDATED_AT = "updatedAt";

    private static final String HASH_KEY_PREFIX = "CFG_";
    public static final String SORT_KEY_VALUE = "LANG";
    public static final String COL_VALUE = HASH_KEY_PREFIX + SORT_KEY_VALUE;


    @Setter
    @Getter(onMethod = @__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)}))
    private String hashKey;
    @Setter
    @Getter(onMethod = @__({@DynamoDbSortKey, @DynamoDbAttribute(COL_SK)}))
    private String sortKey;
    @Setter
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_VALUE)}))
    private LangConfig value;
    @Setter
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_CREATED_AT)}))
    private Instant createdAt;
    @Setter
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_UPDATED_AT)}))
    private Instant updatedAt;

    public static String buildPk(String paId) {
        return Optional.ofNullable(paId).map(s -> HASH_KEY_PREFIX + s)
                .orElse(null);
    }

    public static String getPaId(String hashKey) {
        return Optional.ofNullable(hashKey).map(s -> s.replace(HASH_KEY_PREFIX, ""))
                .orElse(null);
    }
}
