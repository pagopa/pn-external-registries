package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.dto.SenderConfigurationType;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.BaseDao;
import it.pagopa.pn.external.registries.util.SenderConfigurationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@Slf4j
public class SenderConfigurationDao extends BaseDao {

    DynamoDbAsyncTable<LanguageDetailEntity> senderConfigurationTable;

    public SenderConfigurationDao(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient,
                             PnExternalRegistriesConfig pnExternalRegistriesConfig) {
        this.senderConfigurationTable = dynamoDbAsyncClient.table(pnExternalRegistriesConfig.getDynamodbTableNameCostComponents(), TableSchema.fromBean(LanguageDetailEntity.class));
    }

    public Mono<LanguageDetailEntity> getSenderConfiguration(String paId){
        return Mono.fromFuture(senderConfigurationTable.getItem(getKeyBuild(SenderConfigurationUtils.getPk(paId), SenderConfigurationType.LANG.name())));
    }

    public Mono<Void> putItem(LanguageDetailEntity entity) {
        return Mono.fromFuture(senderConfigurationTable.putItem(entity));
    }

}
