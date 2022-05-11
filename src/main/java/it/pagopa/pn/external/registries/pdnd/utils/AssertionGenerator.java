package it.pagopa.pn.external.registries.pdnd.utils;

import it.pagopa.pn.external.registries.config.JwtConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
public class AssertionGenerator {

    private static final String JWT_HEADER_FILED_NAME = "header";
    private static final String JWT_PAYLOAD_FIELD_NAME = "payload";

    private final KmsAsyncClient kmsClient;
    private final Clock clock;

    public AssertionGenerator(KmsAsyncClient kmsAsyncClient, Clock clock) {
        this.kmsClient = kmsAsyncClient;
        this.clock = clock;
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
                String signatureString = bytesToUrlSafeBase64String( signature );

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
        String jsonString = jsonObj.toString();
        return stringToUrlSafeBase64String( jsonString );
    }

    private static String stringToUrlSafeBase64String( String inString ) {
        byte[] jsonBytes = inString.getBytes(StandardCharsets.UTF_8);
        return bytesToUrlSafeBase64String( jsonBytes );
    }

    private static String bytesToUrlSafeBase64String( byte[] bytes ) {
        byte[] base64JsonBytes = Base64Utils.encodeUrlSafe( bytes );
        return new String( base64JsonBytes, StandardCharsets.UTF_8 )
                .replaceFirst("=+$", "");
    }

    private JSONObject generateJwtObject( JwtConfig jwtCfg ) throws JSONException {
        JSONObject header = new JSONObject();
        JSONObject payload = new JSONObject();

        header.put("alg", "RS256");
        header.put("kid", jwtCfg.getKid());
        header.put("typ", "JWT");

        long nowSeconds = clock.millis() / 1000l;
        long ttlSeconds = jwtCfg.getClientAssertionTtl();
        long expireSeconds = nowSeconds + ttlSeconds;
        payload.put("iss", jwtCfg.getIssuer());
        payload.put("sub", jwtCfg.getSubject());
        payload.put("aud", jwtCfg.getAudience());
        payload.put("jti", UUID.randomUUID().toString());
        payload.put("iat", nowSeconds);
        payload.put("exp", expireSeconds);
        String purposeId = jwtCfg.getPurposeId();
        if(StringUtils.hasText( purposeId )) {
            payload.put("purposeId", purposeId);
        }

        JSONObject jwt = new JSONObject();
        jwt.put("header", header);
        jwt.put("payload", payload);
        return jwt;
    }
}
