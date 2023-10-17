package it.pagopa.pn.external.registries.middleware.db.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class CommunicationResultEntity {
     private static final String COL_STATUS_CODE = "statusCode";
     private static final String COL_RESULT_ENUM = "resultEnum";
     private static final String COL_JSON_RESPONSE = "jsonResponse";

     @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_STATUS_CODE)}))
     Integer statusCode;

     @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_RESULT_ENUM)}))
     String resultEnum; // String from enum UpdateResultResponseInt

     @Setter @Getter(onMethod=@__({@DynamoDbAttribute(COL_JSON_RESPONSE)}))
     String jsonResponse; // String from GPDPaymentInfoInt
}
