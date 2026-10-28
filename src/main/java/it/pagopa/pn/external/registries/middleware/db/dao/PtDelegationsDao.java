package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.BaseDao;
import lombok.CustomLog;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import static it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity.COL_EVENT_KEY;
import static it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity.COL_PT_ID;

@Repository
@CustomLog
public class PtDelegationsDao extends BaseDao {

    DynamoDbAsyncTable<PtDelegationEntity> ptDelegationsTable;

    public PtDelegationsDao(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient,
                            PnExternalRegistriesConfig pnExternalRegistriesConfig) {
        this.ptDelegationsTable = dynamoDbAsyncClient.table(pnExternalRegistriesConfig.getDynamodbTableNamePtDelegations(), TableSchema.fromBean(PtDelegationEntity.class));
    }

    public Mono<Boolean> insertIfAbsent(PtDelegationEntity entity) {
        log.debug("insertIfAbsent ptId={} eventKey={} delegationId={}", entity.getPtId(), entity.getEventKey(), entity.getDelegationId());

        PutItemEnhancedRequest<PtDelegationEntity> putItemEnhancedRequest = PutItemEnhancedRequest.builder(PtDelegationEntity.class)
                .item(entity)
                .conditionExpression(Expression.builder()
                        .expression("attribute_not_exists(" + COL_PT_ID + ") and attribute_not_exists(" + COL_EVENT_KEY + ")")
                        .build())
                .build();

        return Mono.fromFuture(() -> ptDelegationsTable.putItem(putItemEnhancedRequest))
                .thenReturn(Boolean.TRUE)
                .onErrorResume(ConditionalCheckFailedException.class, e -> {
                    log.info("Event already present ptId={} eventKey={} delegationId={}", entity.getPtId(), entity.getEventKey(), entity.getDelegationId());
                    return Mono.just(Boolean.FALSE);
                });
    }

    public Flux<PtDelegationEntity> getLatestEventPerInstitution(String ptId, String lastInstitutionId) {
        log.debug("getLatestEventPerInstitution ptId={} lastInstitutionId={}", ptId, lastInstitutionId);

        QueryConditional queryConditional = lastInstitutionId == null
                ? QueryConditional.keyEqualTo(getKeyBuild(ptId))
                : QueryConditional.sortLessThan(getKeyBuild(ptId, PtDelegationEntity.buildEventKeyPrefix(lastInstitutionId)));

        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .scanIndexForward(false)
                .build();

        return Flux.from(ptDelegationsTable.query(queryEnhancedRequest).flatMapIterable(Page::items))
                .distinctUntilChanged(PtDelegationEntity::getInstitutionId);
    }
}
