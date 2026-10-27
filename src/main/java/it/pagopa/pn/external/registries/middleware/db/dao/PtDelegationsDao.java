package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.PtDelegationEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@Slf4j
public class PtDelegationsDao extends BaseDao {

    DynamoDbAsyncTable<PtDelegationEntity> ptDelegationsTable;

    public PtDelegationsDao(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient,
                            PnExternalRegistriesConfig pnExternalRegistriesConfig) {
        this.ptDelegationsTable = dynamoDbAsyncClient.table(pnExternalRegistriesConfig.getDynamodbTableNamePtDelegations(), TableSchema.fromBean(PtDelegationEntity.class));
    }

    public Mono<Boolean> insertIfAbsent(PtDelegationEntity entity) {
        return Mono.error(new UnsupportedOperationException());
    }

    public Flux<PtDelegationEntity> getLatestEventPerInstitution(String ptId, String lastInstitutionId) {
        return Flux.error(new UnsupportedOperationException());
    }
}
