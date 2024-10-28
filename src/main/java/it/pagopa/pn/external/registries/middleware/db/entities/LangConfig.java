package it.pagopa.pn.external.registries.middleware.db.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

@DynamoDbBean
@Data
@NoArgsConstructor
public class LangConfig {

    public static final String COL_ADDITIONAL_LANGS = "additionalLangs";

    private List<String> additionalLangs;
}
