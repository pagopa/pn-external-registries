package it.pagopa.pn.external.registries.middleware.db.io.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.UpdateBehavior;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.Instant;

// Questa entità logica contiene sia i record per gestire i messaggi di optIn, sia i record che contengono
// la data probabile di inizio del workflow cartaceo.

// Per i record che contengo le informazioni sul messaggio di OptIn, la PK è una stringa contenente
// il codice fiscale crittografato.

// Per i record che contengo l'informazione della probabile data di inizio del workflow cartaceo, la PK
// ha il seguente formato: SENT##iun##recipientInternalId
@DynamoDbBean
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class IOMessagesEntity {

    public static final String COL_PK = "pk";
    private static final String COL_CREATED = "created";
    private static final String COL_LAST_MODIFIED = "lastModified";
    private static final String COL_SCHEDULING_ANALOG_DATE = "schedulingAnalogDate";
    private static final String COL_SENDER_DENOMINATION = "senderDenomination";
    private static final String COL_IUN = "iun";
    private static final String COL_SUBJECT = "subject";
    public static final String COL_I_TTL = "i_ttl";

    public IOMessagesEntity(String hashedTaxId){
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
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_SENDER_DENOMINATION)}))  private String senderDenomination;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_IUN)}))  private String iun;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_SUBJECT)}))  private String subject;
    @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_I_TTL)}))  private Long ttl;
}
