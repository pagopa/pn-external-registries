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

import static it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity.buildPk;

@Repository
@Slf4j
public class SenderConfigurationDao extends BaseDao {

    DynamoDbAsyncTable<LanguageDetailEntity> senderConfigurationTable;

    public SenderConfigurationDao(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient,
                             PnExternalRegistriesConfig pnExternalRegistriesConfig) {
        this.senderConfigurationTable = dynamoDbAsyncClient.table(pnExternalRegistriesConfig.getDynamodbTableNameSenderConfiguration(), TableSchema.fromBean(LanguageDetailEntity.class));
    }

    public Mono<LanguageDetailEntity> getSenderConfiguration(String paId, SenderConfigurationType configType){
        Key key = Key.builder()
                .partitionValue(buildPk(paId))
                .sortValue(configType.name())
                .build();
        log.info("KEY: {}", key);
        return Mono.fromFuture(senderConfigurationTable.getItem(key));
    }

    public Mono<Void> putItem(LanguageDetailEntity entity) {
        return Mono.fromFuture(senderConfigurationTable.putItem(entity));
    }

}
