package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.Instant;

import static it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity.GSI_INDEX_LASTUPDATE;

@Repository
@Slf4j
public class OnboardInstitutionsDao extends BaseDao {

    DynamoDbAsyncTable<OnboardInstitutionEntity> onboardInstitutionsTable;

    public OnboardInstitutionsDao(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient,
                                  PnExternalRegistriesConfig pnExternalRegistriesConfig) {
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
         QueryEnhancedRequest queryEnhancedRequestSUSPENDED = QueryEnhancedRequest.builder()
                 .queryConditional(QueryConditional.sortGreaterThan(getKeyBuild(OnboardInstitutionEntity.STATUS_SUSPENDED, instant.toString())))
                 .build();

         return Flux.merge(
            Flux.from(onboardInstitutionsTable.index(GSI_INDEX_LASTUPDATE).query(queryEnhancedRequestACTIVE).flatMapIterable(Page::items)),
                 Flux.from(onboardInstitutionsTable.index(GSI_INDEX_LASTUPDATE).query(queryEnhancedRequestSUSPENDED).flatMapIterable(Page::items)));
    }

    public Mono<Void> put(OnboardInstitutionEntity onboardInstitutionEntity) {
         return Mono.fromFuture(onboardInstitutionsTable.putItem(onboardInstitutionEntity));
    }

}
