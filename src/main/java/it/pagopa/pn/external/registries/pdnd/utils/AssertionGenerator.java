package it.pagopa.pn.external.registries.pdnd.utils;

import com.amazonaws.services.kms.*;
import com.amazonaws.services.kms.model.*;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.config.aws.AwsConfigs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Future;


@Slf4j
@Component
public class AssertionGenerator {

    private PnExternalRegistriesConfig config;
    private AwsConfigs awsConfig;

    public AssertionGenerator(PnExternalRegistriesConfig config,AwsConfigs awsConfig) {
        this.config = config;
        this.awsConfig=awsConfig;
    }

    public String generateClientAssertion() {
        try {

            AWSKMSAsync kmsClient = AWSKMSAsyncClientBuilder.standard()
                    .withRegion(awsConfig.getRegionCode())
                    .build();

            if (kmsClient == null) {
                log.error("kmsClient null");
                return null;
            } else
                log.info(kmsClient.toString());

            JSONObject header = new JSONObject();
            JSONObject payload = new JSONObject();
            if (!generateTokenFields(header, payload))
                return null;

            log.debug("jwtToken header: " + header.toString());
            log.debug("jwtToken payload: " + payload.toString());

            Base64.Encoder encoder = Base64.getUrlEncoder();
            byte[] header64 = encoder.encode(header.toString().getBytes(StandardCharsets.UTF_8));
            byte[] payload64 = encoder.encode(payload.toString().getBytes(StandardCharsets.UTF_8));

            StringBuffer jwtContent =  new StringBuffer().append(new String(header64, "UTF8")).append(".").append(new String(payload64, "UTF8"));
            log.info("jwtContent= " + jwtContent);

            ByteBuffer jwtContentPlaintext = ByteBuffer.wrap(jwtContent.toString().getBytes(StandardCharsets.UTF_8));

            SignRequest signRequest = new SignRequest();
            signRequest.setMessage(jwtContentPlaintext);
            signRequest.setKeyId(awsConfig.getKeyARN());
            signRequest.setSigningAlgorithm("RSASSA_PKCS1_V1_5_SHA_256");
            signRequest.setMessageType("RAW");

            Future<SignResult> signingResult = kmsClient.signAsync(signRequest);
            SignResult signResult=signingResult.get();

            byte[] sign = Base64Utils.encodeUrlSafe(signResult.getSignature().array());
            String jwtSignature = new String(sign, StandardCharsets.UTF_8);

            log.info("Sign -> " + jwtSignature);
            log.info("Sign result OK- token -> \n" + jwtContent + "." + jwtSignature);
            return jwtContent + "." + jwtSignature;
        } catch (Exception e) {
            log.error(e.getMessage() + " - " + e);
        }
        return "OK";
    }
    private boolean generateTokenFields(JSONObject header, JSONObject payload) {
        try {
            header.put("alg", "RS256");
            header.put("kid", config.getPdnpM2MKid());
            header.put("typ", "JWT");

            long nowMillis = System.currentTimeMillis();
            long ttlMillis = 86400000; // 24 ore
            long expMillis = nowMillis + ttlMillis;
            payload.put("iss", config.getPdndM2MIssuer());
            payload.put("sub", config.getPdndM2MSubjec());
            payload.put("aud", config.getPdndM2MAudience());
            payload.put("jti", UUID.randomUUID().toString());
            payload.put("iat", nowMillis);
            payload.put("exp", expMillis);
        } catch (Exception e) {
            log.error("Error creating header/payload jwt Token: " + e.getMessage());
            return false;
        }
        return true;
    }
}
