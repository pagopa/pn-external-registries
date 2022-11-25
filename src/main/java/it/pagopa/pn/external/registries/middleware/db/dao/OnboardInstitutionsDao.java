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
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.Map;

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

        // TODO usare indice al posto dello scan
         ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
                 .filterExpression(Expression.builder()
                         .expression(OnboardInstitutionEntity.COL_LASTUPDATE + " > :time")
                         .expressionValues(Map.of(":time", AttributeValue.builder().s(instant.toString()).build()))
                         .build())
                 .build();

        return Flux.from(onboardInstitutionsTable.scan(scanEnhancedRequest).items());
    }
}
