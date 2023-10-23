package it.pagopa.pn.external.registries.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_BADCONFIGURATION_MISSING_TEMPLATE;


@Configuration
@EnableScheduling
@ConfigurationProperties(prefix = "pn.external-registry")
@Slf4j
@Data
@ToString
@Import(SharedAutoConfiguration.class)
public class PnExternalRegistriesConfig {

    private String checkoutApiKey;
    private String checkoutApiBaseUrl;
    private String checkoutSiteUrl;
    private String checkoutCartApiBaseUrl;

    private String deliveryBaseUrl;
    private String deliveryPushBaseUrl;

    private String ioApiKey;
    private String ioactApiKey;
    private String ioBaseUrl;
    private List<String> ioWhitelist;

    private boolean enableIoMessage;
    private boolean enableIoActivationMessage;

    private String selfcareusergroupApiKey;
    private String selfcareusergroupBaseUrl;
    private String selfcareusergroupUid;

    private String selfcarepgusergroupApiKey;
    private String selfcarepgusergroupBaseUrl;
    private String selfcarepgusergroupUid;

    private String gpdApiKey;
    private String gpdApiBaseUrl;

    private String mockDataResources;

    private String dynamodbTableNameIOMessages;
    private String dynamodbTableNameOnboardInstitutions;
    private String dynamodbTableNameCostComponents;
    private String dynamodbTableNameCostUpdateResult;

    private int dynamodbTableNameCostUpdateResultTtlDays;

    private String piattaformanotificheurlTos;
    private String piattaformanotificheurlPrivacy;

    private int fulltextsearchMaxResults;
    private String fulltextsearchUpdateCronExpression; // usato direttamente come @value

    private AppIoTemplate appIoTemplate;
    private Topics topics;

    private int ioOptinMinDays;

    private String onetrustToken;
    private String onetrustBaseUrl;

    private List<String> aoouosenderid;

    @Data
    public static class AppIoTemplate{
        private String markdownUpgradeAppIoENMessage;
        private String markdownUpgradeAppIoITMessage;
        private String markdownActivationAppIoMessage;
        private String subjectActivationAppIoMessage;
        private String markdownDisclaimerAfterDateAppIoMessage;
        private String markdownDisclaimerBeforeDateAppIoMessage;

    }

    @Data
    public static class Topics {
        private String deliveryPushInput;
    }

    @PostConstruct
    public void init(){
        this.appIoTemplate = new AppIoTemplate();
        this.appIoTemplate.markdownUpgradeAppIoITMessage = fetchMessage("markdown_upgrade_app_io_message_IT.md");
        this.appIoTemplate.markdownUpgradeAppIoENMessage = fetchMessage("markdown_upgrade_app_io_message_EN.md");
        this.appIoTemplate.markdownActivationAppIoMessage = fetchMessage("markdown_activation_app_io_message.md");
        this.appIoTemplate.subjectActivationAppIoMessage = fetchMessage("subject_activation_app_io_message.md");
        this.appIoTemplate.markdownDisclaimerAfterDateAppIoMessage = fetchMessage("markdown_disclaimer_after_date_app_io_message.md");
        this.appIoTemplate.markdownDisclaimerBeforeDateAppIoMessage = fetchMessage("markdown_disclaimer_before_date_app_io_message.md");
    }

    private String fetchMessage(String filename){
        try( InputStream in = getInputStreamFromResource(filename)) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("cannot load message from resources", e);
            throw new PnInternalException("cannot load template ", ERROR_CODE_BADCONFIGURATION_MISSING_TEMPLATE);
        }
    }

    private InputStream getInputStreamFromResource(String filename) throws IOException {
        return ResourceUtils.getURL("classpath:appio_message_templates/" + filename).openStream();
    }
}
