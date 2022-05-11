package it.pagopa.pn.external.registries.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.config.JwtConfig;
import it.pagopa.pn.external.registries.exceptions.AssertionGeneratorException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
public class AssertionGenerator {


    private final KmsAsyncClient kmsClient;
    private final Clock clock;

    public AssertionGenerator(KmsAsyncClient kmsAsyncClient, Clock clock) {
        this.kmsClient = kmsAsyncClient;
        this.clock = clock;
    }

    public CompletableFuture<String> generateClientAssertion( JwtConfig jwtCfg ) throws AssertionGeneratorException {
        try {

            TokenHeader th = new TokenHeader(jwtCfg);
            TokenPayload tp = new TokenPayload(jwtCfg);
            log.debug("jwtTokenObject header={} payload={}", th, tp);
            ObjectMapper mapper = new ObjectMapper();


            String headerBase64String = jsonObjectToUrlSafeBase64String( mapper.writeValueAsString(th));
            String payloadBase64String = jsonObjectToUrlSafeBase64String( mapper.writeValueAsString(tp));
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
        catch (Exception exc) {
            log.error("Error creating client_assertion: -> ", exc);
            throw new AssertionGeneratorException( exc );
        }
    }

    private static String jsonObjectToUrlSafeBase64String(String jsonString ) {
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

    @Data
    private class  TokenHeader {
        String alg;
        String kid;
        String typ;

        public TokenHeader(JwtConfig jwtCfg )
        {
            alg = "RS256";
            kid = jwtCfg.getKid();
            typ = "JWT";
        }
    }

    @Data
    private class  TokenPayload {
        String iss;
        String sub;
        String aud;
        String jti;
        long iat;
        long exp;
        String purposeId;

        public TokenPayload(JwtConfig jwtCfg){
            long nowSeconds = clock.millis() / 1000l;
            long ttlSeconds = jwtCfg.getClientAssertionTtl();
            long expireSeconds = nowSeconds + ttlSeconds;

            iss = jwtCfg.getIssuer();
            sub = jwtCfg.getSubject();
            aud = jwtCfg.getAudience();
            jti = UUID.randomUUID().toString();
            iat = nowSeconds;
            exp = expireSeconds;
            String confpurposeId = jwtCfg.getPurposeId();
            if(StringUtils.hasText( confpurposeId )) {
                purposeId = confpurposeId;
            }
        }
    }

}
