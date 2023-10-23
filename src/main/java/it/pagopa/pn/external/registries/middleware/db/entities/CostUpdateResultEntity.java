package it.pagopa.pn.external.registries.middleware.db.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;

@DynamoDbBean
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class CostUpdateResultEntity {
    public static final String COL_PK = "pk";
    public static final String COL_SK = "sk";
    public static final String COL_REQUEST_ID = "requestId";
    public static final String COL_FAILED_IUV = "failedIuv";
    public static final String COL_COMMUNICATION_RESULT = "communicationResult";
    public static final String COL_COMMUNICATION_RESULT_GROUP = "communicationResultGroup";
    public static final String COL_UPDATE_COST_PHASE = "updateCostPhase";
    public static final String COL_NOTIFICATION_COST = "notificationCost";
    public static final String COL_IUN = "iun";
    public static final String COL_EVENT_TIMESTAMP = "eventTimestamp";
    public static final String COL_EVENT_STORAGE_TIMESTAMP = "eventStorageTimestamp";
    public static final String COL_COMMUNICATION_TIMESTAMP = "communicationTimestamp";
    public static final String COL_JSON_RESPONSE = "jsonResponse";
    public static final String COL_TTL = "ttl";

    @Setter
    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)}))
    private String pk; // creditorTaxId + "##" + noticeCode
    @Setter @Getter(onMethod=@__({@DynamoDbSortKey, @DynamoDbAttribute(COL_SK)}))
    private String sk; // updateCostPhase + "##" + communicationResultGroup + "##" + randomUUID

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_REQUEST_ID)}))
    private String requestId;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_FAILED_IUV)}))
    private String failedIuv;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_COMMUNICATION_RESULT)}))
    private CommunicationResultEntity communicationResult;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_COMMUNICATION_RESULT_GROUP)}))
    private String communicationResultGroup;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_UPDATE_COST_PHASE)}))
    private String updateCostPhase;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_NOTIFICATION_COST)}))
    private Integer notificationCost;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_IUN)}))
    private String iun;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_EVENT_TIMESTAMP)}))
    private Instant eventTimestamp;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_EVENT_STORAGE_TIMESTAMP)}))
    private Instant eventStorageTimestamp;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_COMMUNICATION_TIMESTAMP)}))
    private Instant communicationTimestamp;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_JSON_RESPONSE)}))
    private String jsonResponse; // the JSON string (not object) returned by the GPD service

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_TTL)}))
    private Long ttl;

    public CostUpdateResultEntity(CostUpdateResultEntity other) {
        // shallow copy
        this.pk = other.pk;
        this.sk = other.sk;
        this.requestId = other.requestId;
        this.failedIuv = other.failedIuv;
        this.communicationResult = other.communicationResult;
        this.communicationResultGroup = other.communicationResultGroup;
        this.updateCostPhase = other.updateCostPhase;
        this.notificationCost = other.notificationCost;
        this.iun = other.iun;
        this.eventTimestamp = other.eventTimestamp;
        this.eventStorageTimestamp = other.eventStorageTimestamp;
        this.communicationTimestamp = other.communicationTimestamp;
        this.jsonResponse = other.jsonResponse;
        this.ttl = other.ttl;
    }
}
