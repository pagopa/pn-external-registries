package it.pagopa.pn.external.registries.middleware.db.dao;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.dto.SenderConfigurationType;
import it.pagopa.pn.external.registries.middleware.db.entities.LangConfig;
import it.pagopa.pn.external.registries.middleware.db.entities.LanguageDetailEntity;
import it.pagopa.pn.external.registries.middleware.db.io.dao.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.List;

@Repository
@Slf4j
public class SenderConfigurationDao extends BaseDao {

    DynamoDbAsyncTable<LanguageDetailEntity> senderConfigurationTable;

    public SenderConfigurationDao(DynamoDbEnhancedAsyncClient dynamoDbAsyncClient,
                             PnExternalRegistriesConfig pnExternalRegistriesConfig) {
        this.senderConfigurationTable = dynamoDbAsyncClient.table(pnExternalRegistriesConfig.getDynamodbTableNameCostComponents(), TableSchema.fromBean(LanguageDetailEntity.class));
    }

    public Mono<LanguageDetailEntity> getSenderConfiguration(String paId){
        return Mono.fromFuture(senderConfigurationTable.getItem(getKeyBuild(paId, SenderConfigurationType.LANG.name())));
    }

    public Mono<LanguageDetailEntity> createOrUpdateLang(String paId, List<String> additionalLanguages) {
        LangConfig langConfig = new LangConfig();
        langConfig.setAdditionalLangs(additionalLanguages);

        LanguageDetailEntity languageDetailEntity = new LanguageDetailEntity();
        languageDetailEntity.setHashKey(LanguageDetailEntity.buildPk(paId));
        languageDetailEntity.setValue(langConfig);
        return Mono.fromFuture(senderConfigurationTable.updateItem(languageDetailEntity));
    }

}
