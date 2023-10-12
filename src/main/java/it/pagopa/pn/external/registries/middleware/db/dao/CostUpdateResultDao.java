package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.CostUpdateResultEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@Slf4j
public class CostUpdateResultDao extends BaseDao {
    DynamoDbAsyncTable<CostUpdateResultEntity> costUpdateResultTable;

    public CostUpdateResultDao(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient,
                               PnExternalRegistriesConfig pnExternalRegistriesConfig) {
        this.costUpdateResultTable = dynamoDbAsyncClient.table(pnExternalRegistriesConfig.getDynamodbTableNameCostUpdateResult(), TableSchema.fromBean(CostUpdateResultEntity.class));
    }
}
