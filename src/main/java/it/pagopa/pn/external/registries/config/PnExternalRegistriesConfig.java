package it.pagopa.pn.external.registries.config;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Configuration
@ConfigurationProperties(prefix = "pn-er")
@Slf4j
@Data
@ToString
public class PnExternalRegistriesConfig {

    private String pdndM2MIssuer;
    private String pdndM2MSubjec;
    private String pdndM2MAudience;
    private String pdnpM2MKid;
    private String pdndM2MPrivateKeyFileName;
    private String pdndM2MPublicKeyFileName;
    private String pdndM2MPrivateKey;
    private String pdndM2MPublicKey;
    private String pdndServerURL;
    private String pdndM2MClientAssertionType;
    private String pdndM2MGrantType;
    private String pdndM2MClientId;

    @PostConstruct
    private void loadM2MSecurityKeys(){

        log.debug("Load publicKey -> "+ pdndM2MPublicKeyFileName);
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(pdndM2MPublicKeyFileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            pdndM2MPublicKey = new String();
            while ((line = reader.readLine()) != null) {
                pdndM2MPublicKey=pdndM2MPublicKey +line;
            }
            log.debug("pdndM2MPublicKey "+pdndM2MPublicKey );
        }catch(Exception e)
        {
            log.error("Fail to read public key: "+ e);
        }
        log.debug("Load privateKey -> "+ pdndM2MPrivateKeyFileName);
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(pdndM2MPrivateKeyFileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            pdndM2MPrivateKey = new String();
            while ((line = reader.readLine()) != null) {
                pdndM2MPrivateKey=pdndM2MPrivateKey +line;
            }
            log.debug("PrivateKey "+pdndM2MPrivateKey );
        }catch(Exception e)
        {
            log.error("Fail to read private key: "+ e);
        }
    }


}
