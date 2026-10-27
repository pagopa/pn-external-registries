package it.pagopa.pn.external.registries.middleware.db.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@DynamoDbBean
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class PtDelegationEntity {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DELETED = "DELETED";

    public static final String COL_PT_ID = "ptId";
    public static final String COL_EVENT_KEY = "eventKey";
    public static final String COL_DELEGATION_ID = "delegationId";
    public static final String COL_DELEGATION_TYPE = "delegationType";
    public static final String COL_PRODUCT_ID = "productId";
    public static final String COL_STATUS = "status";
    public static final String COL_IS_TEST = "isTest";
    public static final String COL_SOURCE_EVENT_ID = "sourceEventId";
    public static final String COL_SOURCE_EVENT_TYPE = "sourceEventType";
    public static final String COL_CREATED_AT = "createdAt";
    public static final String COL_UPDATED_AT = "updatedAt";
    public static final String COL_CLOSED_AT = "closedAt";
    public static final String COL_LAST_CHANGE_AT = "lastChangeAt";
    public static final String COL_INSTITUTION_ID = "institutionId";
    public static final String COL_INSTITUTION_NAME = "institutionName";
    public static final String COL_INSTITUTION_ROOT_NAME = "institutionRootName";
    public static final String COL_INSTITUTION_TAX_CODE = "institutionTaxCode";
    public static final String COL_INSTITUTION_TYPE = "institutionType";
    public static final String COL_PT_NAME = "ptName";
    public static final String COL_PT_TAX_CODE = "ptTaxCode";
    public static final String COL_PT_TYPE = "ptType";

    public static String buildEventKey(String institutionId, Instant lastChangeAt) {
        return institutionId + "#" + DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").withZone(ZoneOffset.UTC).format(lastChangeAt);
    }

    public static String buildEventKeyPrefix(String institutionId) {
        return institutionId + "#";
    }

    @Setter @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PT_ID)}))  private String ptId;
    @Setter @Getter(onMethod=@__({@DynamoDbSortKey, @DynamoDbAttribute(COL_EVENT_KEY)}))  private String eventKey;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_DELEGATION_ID)}))  private String delegationId;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_DELEGATION_TYPE)}))  private String delegationType;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_PRODUCT_ID)}))  private String productId;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_STATUS)}))  private String status;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_IS_TEST)}))  private Boolean isTest;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_SOURCE_EVENT_ID)}))  private String sourceEventId;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_SOURCE_EVENT_TYPE)}))  private String sourceEventType;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_CREATED_AT)}))  private Instant createdAt;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_UPDATED_AT)}))  private Instant updatedAt;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_CLOSED_AT)}))  private Instant closedAt;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_LAST_CHANGE_AT)}))  private Instant lastChangeAt;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_INSTITUTION_ID)}))  private String institutionId;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_INSTITUTION_NAME)}))  private String institutionName;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_INSTITUTION_ROOT_NAME)}))  private String institutionRootName;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_INSTITUTION_TAX_CODE)}))  private String institutionTaxCode;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_INSTITUTION_TYPE)}))  private String institutionType;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_PT_NAME)}))  private String ptName;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_PT_TAX_CODE)}))  private String ptTaxCode;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_PT_TYPE)}))  private String ptType;

}
