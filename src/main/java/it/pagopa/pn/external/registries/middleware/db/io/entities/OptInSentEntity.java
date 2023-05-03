package it.pagopa.pn.external.registries.middleware.db.io.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.UpdateBehavior;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.Instant;

@DynamoDbBean
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class OptInSentEntity {

    public static final String COL_PK = "pk";
    private static final String COL_CREATED = "created";
    private static final String COL_LAST_MODIFIED = "lastModified";
    private static final String COL_SCHEDULING_ANALOG_DATE = "schedulingAnalogDate";
    public static final String COL_I_TTL = "i_ttl";

    public OptInSentEntity(String hashedTaxId){
        this.pk = hashedTaxId;
        this.setCreated(Instant.now());
        this.setLastModified(this.getCreated());
    }

    @DynamoDbIgnore
    public String getHashedTaxId(){
        return this.pk;
    }


    @Setter @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)}))  private String pk;


    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_CREATED), @DynamoDbUpdateBehavior(UpdateBehavior.WRITE_IF_NOT_EXISTS)}))  private Instant created;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_LAST_MODIFIED)}))  private Instant lastModified;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_SCHEDULING_ANALOG_DATE)}))  private Instant schedulingAnalogDate;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_I_TTL)}))  private Long ttl;
}
