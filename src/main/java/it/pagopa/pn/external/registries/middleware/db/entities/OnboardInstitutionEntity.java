package it.pagopa.pn.external.registries.middleware.db.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.UpdateBehavior;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.Instant;

@DynamoDbBean
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class OnboardInstitutionEntity {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_CLOSED = "CLOSED";

    public static final String GSI_INDEX_LASTUPDATE = "status-lastUpdate-gsi";

    public static final String COL_PK = "id";
    public static final String COL_LASTUPDATE = "lastUpdate";
    private static final String COL_STATUS = "status";
    private static final String COL_CREATED = "created";

    private static final String COL_ADDRESS = "address";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_DIGITALADDRESS = "digitalAddress";
    private static final String COL_EXTERNALID = "externalId";
    private static final String COL_TAXCODE = "taxCode";
    private static final String COL_ZIPCODE = "zipCode";
    private static final String COL_IPACODE = "ipaCode";
    private static final String COL_SDICODE = "sdiCode";
    private static final String COL_ROOT_ID = "rootId";
    private static final String COL_ONLY_ROOT_STATUS = "onlyRootStatus";


    @DynamoDbIgnore
    public String getInstitutionId(){
        return this.pk;
    }

    @DynamoDbIgnore
    public boolean isActive(){
        return this.status!=null && this.status.equals(STATUS_ACTIVE);
    }

    @Setter @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)}))  private String pk;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_STATUS)}))  private String status;
    @Setter @Getter(onMethod=@__({@DynamoDbSecondarySortKey(indexNames = { GSI_INDEX_LASTUPDATE }), @DynamoDbAttribute(COL_LASTUPDATE)}))  private Instant lastUpdate;
    @Setter @Getter(onMethod=@__({@DynamoDbSecondaryPartitionKey(indexNames = { GSI_INDEX_LASTUPDATE}), @DynamoDbAttribute(COL_ONLY_ROOT_STATUS)}))  private String onlyRootStatus;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_ROOT_ID)}))  private String rootId;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_CREATED), @DynamoDbUpdateBehavior(UpdateBehavior.WRITE_IF_NOT_EXISTS)}))  private Instant created;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_DESCRIPTION)}))  private String description;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_ADDRESS)}))  private String address;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_TAXCODE)}))  private String taxCode;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_ZIPCODE)}))  private String zipCode;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_EXTERNALID)}))  private String externalId;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_DIGITALADDRESS)}))  private String digitalAddress;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_IPACODE)}))  private String ipaCode;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_SDICODE)}))  private String sdiCode;

}
