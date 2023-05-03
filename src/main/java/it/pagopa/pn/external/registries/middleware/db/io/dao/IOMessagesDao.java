package it.pagopa.pn.external.registries.middleware.db.io.dao;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.middleware.db.io.entities.IOMessagesEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@Slf4j
public class IOMessagesDao extends BaseDao {

    DynamoDbAsyncTable<IOMessagesEntity> ioMessagesTable;

    public IOMessagesDao(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient,
                         PnExternalRegistriesConfig pnExternalRegistriesConfig) {
        this.ioMessagesTable = dynamoDbAsyncClient.table(pnExternalRegistriesConfig.getDynamodbTableNameOptIn(), TableSchema.fromBean(IOMessagesEntity.class));
    }

    /**
     * Upsert di una nuova entity
     *
     * @param ioMessagesEntity entity da salvare
     * @return none
     */
    public Mono<Void> save(IOMessagesEntity ioMessagesEntity){
        log.debug("save ioMessagesEntity={}", ioMessagesEntity);

        return Mono.fromFuture(ioMessagesTable.updateItem(ioMessagesEntity)).then();
    }

    /**
     * Legge l'entity IOMessagesEntity associata al taxId
     *
     * @param hashedTaxId taxId utente
     * @return IOMessagesEntity
     */
     public Mono<IOMessagesEntity> get(String hashedTaxId) {
        log.debug("get hashedTaxId={}", hashedTaxId);

        return Mono.fromFuture(ioMessagesTable.getItem(new IOMessagesEntity(hashedTaxId)));
    }
}
