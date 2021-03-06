package it.pagopa.pn.external.registries.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.external.registries.exceptions.InternalErrorException;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Configuration
@ConfigurationProperties(prefix = "pn.external-registry")
@Slf4j
@Data
@ToString
@Import(SharedAutoConfiguration.class)
public class PnExternalRegistriesConfig {

    public static final String PDND_M2M_TOKEN = "pdnd";

    private Map<String,AccessTokenConfig> accessTokens = new HashMap<>();

    private String pdndServerUrl;

    private String anprX509CertificateChain;
    private String anprJWTHeaderDigestKeystoreAlias;

    private String checkoutApiKey;
    private String checkoutBaseUrl;

    private String ioApiKey;
    private String ioactApiKey;
    private String ioBaseUrl;
    private List<String> ioWhitelist;

    private boolean enableIoMessage;
    private boolean enableIoActivationMessage;

    private String selfcareusergroupApiKey;
    private String selfcareusergroupBaseUrl;
    private String selfcareusergroupPnProductId;
    private String selfcareusergroupUid;

    private String selfcareinstitutionsApiKey;
    private String selfcareinstitutionsBaseUrl;
    private String selfcareinstitutionsPnProductId;
    private String selfcareinstitutionsUid;

    private String mockDataResources;

    private String dynamodbTableNameOptIn;

    private String piattaformanotificheurlTos;
    private String piattaformanotificheurlPrivacy;

    private AppIoTemplate appIoTemplate;

    private int ioOptinMinDays;

    @Data
    public static class AppIoTemplate{
        private String markdownUpgradeAppIoENMessage;
        private String markdownUpgradeAppIoITMessage;
        private String markdownActivationAppIoMessage;
        private String subjectActivationAppIoMessage;

    }


    @PostConstruct
    public void init(){
        this.appIoTemplate = new AppIoTemplate();
        this.appIoTemplate.markdownUpgradeAppIoITMessage = fetchMessage("markdown_upgrade_app_io_message_IT.md");
        this.appIoTemplate.markdownUpgradeAppIoENMessage = fetchMessage("markdown_upgrade_app_io_message_EN.md");
        this.appIoTemplate.markdownActivationAppIoMessage = fetchMessage("markdown_activation_app_io_message.md");
        this.appIoTemplate.subjectActivationAppIoMessage = fetchMessage("subject_activation_app_io_message.md");
    }

    private String fetchMessage(String filename){
        try( InputStream in = getInputStreamFromResource(filename)) {
            return IOUtils.toString(in, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            log.error("cannot load message from resources", e);
            throw new InternalErrorException();
        }
    }

    private InputStream getInputStreamFromResource(String filename) throws IOException {
        return ResourceUtils.getURL("classpath:appio_message_templates/" + filename).openStream();
    }
}
