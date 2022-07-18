package it.pagopa.pn.external.registries.middleware.db.io.dao;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.io.entities.OptInSentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@Slf4j
public class OptInSentDao extends BaseDao {

    DynamoDbAsyncTable<OptInSentEntity> optInSentTable;

    public OptInSentDao(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient,
                        PnExternalRegistriesConfig pnExternalRegistriesConfig) {
        this.optInSentTable = dynamoDbAsyncClient.table(pnExternalRegistriesConfig.getDynamodbTableNameOptIn(), TableSchema.fromBean(OptInSentEntity.class));
    }

    /**
     * Upsert di una nuova entity
     *
     * @param optInSentEntity entity da salvare
     * @return none
     */
    public Mono<Void> save(OptInSentEntity optInSentEntity){
        log.debug("save optInSentEntity={}", optInSentEntity);

        return Mono.fromFuture(optInSentTable.updateItem(optInSentEntity)).then();
    }

    /**
     * Legge l'entity OptInSentEntity associata al taxId
     *
     * @param hashedTaxId taxId utente
     * @return OptInSentEntity
     */
     public Mono<OptInSentEntity> get(String hashedTaxId) {
        log.debug("get hashedTaxId={}", hashedTaxId);

        return Mono.fromFuture(optInSentTable.getItem(new OptInSentEntity(hashedTaxId)));
    }
}
