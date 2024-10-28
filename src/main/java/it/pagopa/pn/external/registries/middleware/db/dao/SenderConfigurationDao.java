package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.dto.SenderConfigurationType;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.utils.CollectionUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.external.registries.middleware.db.entities.LangConfig.COL_ADDITIONAL_LANGS;
import static it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity.*;

@Repository
@Slf4j
public class SenderConfigurationDao extends BaseDao {

    DynamoDbAsyncTable<LanguageDetailEntity> senderConfigurationTable;

    DynamoDbAsyncClient dynamoDbAsyncClient;
    public static final String QUERY_IF_NOT_EXISTS = " = if_not_exists(";

    public SenderConfigurationDao(DynamoDbEnhancedAsyncClient dynamoDbEnahnced,
                                  DynamoDbAsyncClient dynamoDbAsyncClient,
                                  PnExternalRegistriesConfig pnExternalRegistriesConfig) {
        this.senderConfigurationTable = dynamoDbEnahnced.table(pnExternalRegistriesConfig.getDynamodbTableNameSenderConfiguration(), TableSchema.fromBean(LanguageDetailEntity.class));
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
    }
    public Mono<LanguageDetailEntity> getSenderConfiguration(String hashKey, SenderConfigurationType configType){
        Key key = Key.builder()
                .partitionValue(hashKey)
                .sortValue(configType.name())
                .build();
        return Mono.fromFuture(senderConfigurationTable.getItem(key));
    }

    public Mono<String> createOrUpdateLang(String hashKey, SenderConfigurationType configType, List<String> values) {
        UpdateItemRequest.Builder builder = UpdateItemRequest.builder()
                .tableName(senderConfigurationTable.tableName())
                .key(buildDynamoKey(hashKey, configType.name()))
                .updateExpression(constructUpdateExpression(configType))
                .expressionAttributeValues(constructexpressionAttributeValuesMap(values, configType));

        return Mono.fromFuture(dynamoDbAsyncClient.updateItem(builder.build()))
                .thenReturn(hashKey);
    }

    private Map<String, AttributeValue> constructexpressionAttributeValuesMap(List<String> values, SenderConfigurationType configType) {
        Map<String, AttributeValue> attributeValueMap = new HashMap<>();
        if(SenderConfigurationType.LANG.equals(configType) && !CollectionUtils.isNullOrEmpty(values)) {
            attributeValueMap.put(":" + COL_CREATED_AT, AttributeValue.builder().s(Instant.now().toString()).build());
            attributeValueMap.put(":" + COL_UPDATED_AT, AttributeValue.builder().s(Instant.now().toString()).build());
            attributeValueMap.put(":" + COL_VALUE, AttributeValue.builder().m(constructAttributeValuesMapForAdditionalLang(values)).build());
        }
        return attributeValueMap;
    }

    private Map<String, AttributeValue> constructAttributeValuesMapForAdditionalLang(List<String> values) {
        Map<String, AttributeValue> metadataMap = new HashMap<>();
        if(!CollectionUtils.isNullOrEmpty(values)) {
            metadataMap.put(COL_ADDITIONAL_LANGS, AttributeValue.builder().l(constructListAttributeValue(values))
                    .build());
        }
        return metadataMap;
    }

    private List<AttributeValue> constructListAttributeValue(List<String> additionalLangs) {
        return additionalLangs.stream()
                .map(s -> AttributeValue.builder().s(s).build())
                .toList();
    }

    private String constructUpdateExpression(SenderConfigurationType configType) {
        StringBuilder stringBuilder = new StringBuilder("SET ");
        if(SenderConfigurationType.LANG.equals(configType)) {
            stringBuilder.append(LanguageDetailEntity.COL_CREATED_AT).append(QUERY_IF_NOT_EXISTS).append(LanguageDetailEntity.COL_CREATED_AT).append(",:").append(COL_CREATED_AT).append("), ");
            stringBuilder.append(LanguageDetailEntity.COL_UPDATED_AT).append(" = :").append(LanguageDetailEntity.COL_UPDATED_AT).append(", ");
            stringBuilder.append(LanguageDetailEntity.COL_VALUE).append(" = :").append(LanguageDetailEntity.COL_VALUE);
        }
        return stringBuilder.toString();
    }

    protected Map<String, AttributeValue> buildDynamoKey(String hashKey, String sortKey) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(COL_PK, AttributeValue.builder().s(hashKey).build());
        key.put(COL_SK, AttributeValue.builder().s(sortKey).build());
        return key;
    }

}
