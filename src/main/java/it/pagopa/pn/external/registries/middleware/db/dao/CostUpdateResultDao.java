package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.CostUpdateResultEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Repository
@Slf4j
public class CostUpdateResultDao extends BaseDao {
    DynamoDbAsyncTable<CostUpdateResultEntity> costUpdateResultTable;
    PnExternalRegistriesConfig pnExternalRegistriesConfig;

    public CostUpdateResultDao(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient,
                               PnExternalRegistriesConfig pnExternalRegistriesConfig) {
        this.costUpdateResultTable = dynamoDbAsyncClient.table(pnExternalRegistriesConfig.getDynamodbTableNameCostUpdateResult(), TableSchema.fromBean(CostUpdateResultEntity.class));
        this.pnExternalRegistriesConfig = pnExternalRegistriesConfig;
    }

    /**
     * insert or update, passing an entity;
     * ttl is set to now + parameter regardless of the passed value
     */
    public Mono<CostUpdateResultEntity> insertOrUpdate(CostUpdateResultEntity costUpdateResultEntity) {
        // the DAO always sets the TTL, ignoring the value passed in the entity
        costUpdateResultEntity.setTtl(Instant.now().plus(pnExternalRegistriesConfig.getDynamodbTableNameCostUpdateResultTtlDays(), ChronoUnit.DAYS).getEpochSecond());

        return Mono.fromFuture(costUpdateResultTable.putItem(costUpdateResultEntity).thenApply(item -> costUpdateResultEntity));
    }
}
