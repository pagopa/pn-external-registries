package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.CostComponentsEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@Slf4j
public class CostComponentsDao extends BaseDao {
    DynamoDbAsyncTable<CostComponentsEntity> costComponentsTable;

    CostComponentsDao(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient,
                      PnExternalRegistriesConfig pnExternalRegistriesConfig) {
        this.costComponentsTable = dynamoDbAsyncClient.table(pnExternalRegistriesConfig.getDynamodbTableNameCostComponents(), TableSchema.fromBean(CostComponentsEntity.class));
    }
}
