package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.BaseDao;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPage;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.Instant;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch.Builder;

import static it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity.GSI_INDEX_LASTUPDATE;

@Repository
@Slf4j
public class OnboardInstitutionsDao extends BaseDao {

    DynamoDbAsyncTable<OnboardInstitutionEntity> onboardInstitutionsTable;
    private DynamoDbEnhancedAsyncClient dynamoDbAsyncClient;

    public OnboardInstitutionsDao(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient,
                                  PnExternalRegistriesConfig pnExternalRegistriesConfig) {
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
        this.onboardInstitutionsTable = dynamoDbAsyncClient.table(pnExternalRegistriesConfig.getDynamodbTableNameOnboardInstitutions(), TableSchema.fromBean(OnboardInstitutionEntity.class));
    }

    public Mono<OnboardInstitutionEntity> get(String institutionId){
        return Mono.fromFuture(onboardInstitutionsTable.getItem(getKeyBuild(institutionId)));
    }

    /**
     * Legge le entity più nuove di
     *
     * @param instant istante di partenza
     * @return OptInSentEntity
     */
     public Flux<OnboardInstitutionEntity> getNewer(Instant instant) {
         if (instant == null)
             instant = Instant.EPOCH;

        log.debug("getNewer instant={}", instant);

         QueryEnhancedRequest queryEnhancedRequestACTIVE = QueryEnhancedRequest.builder()
                 .queryConditional(QueryConditional.sortGreaterThan(getKeyBuild(OnboardInstitutionEntity.STATUS_ACTIVE, instant.toString())))
                 .build();
         QueryEnhancedRequest queryEnhancedRequestDELETED = QueryEnhancedRequest.builder()
                 .queryConditional(QueryConditional.sortGreaterThan(getKeyBuild(OnboardInstitutionEntity.STATUS_CLOSED, instant.toString())))
                 .build();

         return Flux.merge(
            Flux.from(onboardInstitutionsTable.index(GSI_INDEX_LASTUPDATE).query(queryEnhancedRequestACTIVE).flatMapIterable(Page::items)),
                 Flux.from(onboardInstitutionsTable.index(GSI_INDEX_LASTUPDATE).query(queryEnhancedRequestDELETED).flatMapIterable(Page::items)));
    }

    public Flux<OnboardInstitutionEntity> filterOutRootIds(List<String> ids){
        return Flux.fromIterable(ids)
            .window(100)
            .flatMap(institutionId -> {
                Builder<OnboardInstitutionEntity> builder = ReadBatch.builder(OnboardInstitutionEntity.class)
                    .mappedTableResource(onboardInstitutionsTable);

                Mono<BatchGetResultPage> deferred = Mono.defer(() ->
                    Mono.from(dynamoDbAsyncClient.batchGetItem(BatchGetItemEnhancedRequest.builder()
                        .readBatches(builder.build())
                        .build())));

                return institutionId
                    .doOnNext(item -> {
                        Key key = Key.builder().partitionValue(item).build();
                        builder.addGetItem(key);
                    })
                    .then(deferred);
            })
            .flatMap(page -> {
                List<OnboardInstitutionEntity> results = page.resultsForTable(onboardInstitutionsTable);
                log.debug("request size: {}, query result size: {}", ids, results.size());
                if (!page.unprocessedKeysForTable(onboardInstitutionsTable).isEmpty()) {
                    List<Key> unprocessedKeys = page.unprocessedKeysForTable(onboardInstitutionsTable);
                    List<String> unprocessedIds = filterAlreadyProcessed(ids, unprocessedKeys);
                    log.info("unprocessed entities {} over total entities {}", unprocessedIds, ids);
                    return Flux.fromIterable(results).concatWith(
                        filterOutRootIds(unprocessedIds)
                    );
                }
                return Flux.fromIterable(results).filter(entity -> !entity.getInstitutionId().equals(entity.getRootId()));
            });
    }
    private List<String> filterAlreadyProcessed(List<String> unprocessedIds, List<Key> unprocessedKeys) {
        Set<Key> setKeys = new HashSet<>(unprocessedKeys);
        return unprocessedIds.stream()
            .filter(id -> {
                Key key = Key.builder().partitionValue(id).build();
                return setKeys.contains(key);
            })
            .toList();
    }
}
