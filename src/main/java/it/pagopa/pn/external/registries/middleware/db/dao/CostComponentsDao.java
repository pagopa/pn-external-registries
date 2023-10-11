package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.CostComponentsEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

@Repository
@Slf4j
public class CostComponentsDao extends BaseDao {
    DynamoDbAsyncTable<CostComponentsEntity> costComponentsTable;

    public CostComponentsDao(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient,
                      PnExternalRegistriesConfig pnExternalRegistriesConfig) {
        this.costComponentsTable = dynamoDbAsyncClient.table(pnExternalRegistriesConfig.getDynamodbTableNameCostComponents(), TableSchema.fromBean(CostComponentsEntity.class));
    }

    /**
     * insert or update, passing an entity
     */
    public Mono<CostComponentsEntity> insertOrUpdate(CostComponentsEntity costComponentsEntity) {
        return Mono.fromFuture(costComponentsTable.putItem(costComponentsEntity).thenApply(item -> costComponentsEntity));
    }

    /**
     * update from and entity (if not exists, it will not insert): returns the Entity in case of success or empty in case of failure
     */
    public Mono<CostComponentsEntity> updateNotNullIfNotExists(CostComponentsEntity costComponentsEntity) {
        UpdateItemEnhancedRequest<CostComponentsEntity> updateItemEnhancedRequest = UpdateItemEnhancedRequest.builder(CostComponentsEntity.class)
                .item(costComponentsEntity)
                .ignoreNulls(true)
                .conditionExpression(Expression.builder()
                    .expression("attribute_exists(pk) AND attribute_exists(sk)") // if it doesn't exist, ConditionalCheckFailedException is thrown
                    .build())
                .build();

        return Mono.fromFuture(() -> costComponentsTable.updateItem(updateItemEnhancedRequest).thenApply(item -> costComponentsEntity))
                .onErrorResume(ConditionalCheckFailedException.class, e -> {
                    log.error("Item with pk {} and sk {} does not exist", costComponentsEntity.getPk(), costComponentsEntity.getSk());
                    return Mono.empty();
                });
    }

    /**
     * getItem by pk and sk
     */
    public Mono<CostComponentsEntity> getItem(String pk, String sk){
        return Mono.fromFuture(costComponentsTable.getItem(getKeyBuild(pk, sk)));
    }

    /**
     * Get items by pk
     */
    public Flux<CostComponentsEntity> getItems(String pk) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(getKeyBuild(pk));

        return Flux.from(costComponentsTable.query(queryConditional))
                .flatMap(p -> Flux.fromIterable(p.items()));
    }
}
