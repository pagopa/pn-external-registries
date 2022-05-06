package it.pagopa.pn.external.registries.pdnd.utils;

import it.pagopa.pn.external.registries.config.JwtConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
public class AssertionGenerator {

    public static final String JWT_HEADER_FILED_NAME = "header";
    public static final String JWT_PAYLOAD_FIELD_NAME = "payload";
    private KmsAsyncClient kmsClient;

    public AssertionGenerator( KmsAsyncClient kmsAsyncClient) {
        this.kmsClient = kmsAsyncClient;
    }

    public CompletableFuture<String> generateClientAssertion( JwtConfig jwtCfg ) throws AssertionGeneratorException {
        try {

            JSONObject jwtObj = generateJwtObject( jwtCfg );
            log.debug("jwtTokenObject={} ", jwtObj );


            String headerBase64String = jsonObjectToUrlSafeBase64String( jwtObj.getJSONObject(JWT_HEADER_FILED_NAME) );
            String payloadBase64String = jsonObjectToUrlSafeBase64String( jwtObj.getJSONObject(JWT_PAYLOAD_FIELD_NAME) );
            String jwtContent = headerBase64String + "." + payloadBase64String;
            log.info("jwtContent={}", jwtContent);


            SdkBytes awsBytesJwtContent = SdkBytes.fromByteArray( jwtContent.getBytes(StandardCharsets.UTF_8) );
            SignRequest signRequest = SignRequest.builder()
                    .message( awsBytesJwtContent )
                    .messageType( MessageType.RAW )
                    .signingAlgorithm( SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256 )
                    .keyId( jwtCfg.getKeypairAlias() )
                    .build();


            return kmsClient.sign( signRequest ).thenApply( signResult -> {

                byte[] signature = signResult.signature().asByteArray();
                byte[] urlSafeBase64Signature = Base64Utils.encodeUrlSafe(signature);
                String signatureString = new String( urlSafeBase64Signature, StandardCharsets.UTF_8 );

                log.info("Sign result OK - jwt={}", jwtContent + "." + signatureString);
                return jwtContent + "." + signatureString;

            });
        }
        catch (JSONException exc) {
            log.error("Error creating client_assertion: -> ", exc);
            throw new AssertionGeneratorException("Error creating client_assertion: ", exc );
        }
    }

    private static String jsonObjectToUrlSafeBase64String(JSONObject jsonObj ) {
        byte[] jsonBytes = jsonObj.toString().getBytes(StandardCharsets.UTF_8);
        byte[] base64JsonBytes = Base64Utils.encodeUrlSafe( jsonBytes );
        return new String( base64JsonBytes, StandardCharsets.UTF_8 );
    }

    private static JSONObject generateJwtObject( JwtConfig jwtCfg ) throws JSONException {
        JSONObject header = new JSONObject();
        JSONObject payload = new JSONObject();

        header.put("alg", "RS256");
        header.put("kid", jwtCfg.getKid());
        header.put("typ", "JWT");

        long nowMillis = System.currentTimeMillis();
        long ttlMillis = jwtCfg.getClientAssertionTTL(); // 24 ore
        long expMillis = nowMillis + ttlMillis;
        payload.put("iss", jwtCfg.getIssuer());
        payload.put("sub", jwtCfg.getSubject());
        payload.put("aud", jwtCfg.getAudience());
        payload.put("jti", UUID.randomUUID().toString());
        payload.put("iat", nowMillis);
        payload.put("exp", expMillis);

        JSONObject jwt = new JSONObject();
        jwt.put("header", header);
        jwt.put("payload", payload);
        return jwt;
    }
}
