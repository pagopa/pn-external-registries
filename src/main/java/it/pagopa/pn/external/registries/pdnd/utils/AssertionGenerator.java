package it.pagopa.pn.external.registries.pdnd.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import lombok.extern.slf4j.Slf4j;


import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Slf4j
public class AssertionGenerator {
    private PnExternalRegistriesConfig config;

    public AssertionGenerator(PnExternalRegistriesConfig config){
        this.config=config;
    }
    public String generateClientAssertion()
    {
        Map header = new HashMap<String,String>();
        header.put("alg","RS256");
        header.put("kid",config.getPdnpM2MKid());
        header.put("typ","JWT");

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
       // long ttlMillis = 1200000; // 20 min
        long ttlMillis = 86400000; // 24 ore
        long expMillis = nowMillis + ttlMillis;
        Date exp = new Date(expMillis);
        try {
            PrivateKey privateKey = getPrivateKey();
            String jwtToken = Jwts.builder().setHeader(header)
                    .setIssuer(config.getPdndM2MIssuer())
                    .setSubject(config.getPdndM2MSubjec())
                    .setAudience(config.getPdndM2MAudience())
                    .setId(UUID.randomUUID().toString())
                    .setIssuedAt(now)
                    .signWith(privateKey)
                    .setExpiration(exp).compact();
            log.info("token -> "+ jwtToken);
            Jws<Claims> token = parseJwt(jwtToken);

            return jwtToken;
        }catch(Exception e)
        {
            log.error("Exception e"+ e.getMessage());
        }
        return null;
    }

    public  Jws<Claims> parseJwt(String jwtString) throws InvalidKeySpecException, NoSuchAlgorithmException {
        log.info("parseJWT init");
        PublicKey publicKey = getPublicKey();
        log.info("getPublic key OK");
        Jws<Claims> jwt = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(jwtString);
        log.info("parseJWT end");
        return jwt;
    }

    private  PublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String rsaPublicKey= config.getPdndM2MPublicKey();
        rsaPublicKey = rsaPublicKey.replace("-----BEGIN PUBLIC KEY-----", "");
        rsaPublicKey = rsaPublicKey.replace("-----END PUBLIC KEY-----", "");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(rsaPublicKey));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(keySpec);
        return publicKey;
    }

    public  PrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {

        String rsaPrivateKey = config.getPdndM2MPrivateKey();
        rsaPrivateKey = rsaPrivateKey.replace("-----BEGIN PRIVATE KEY-----", "");
        rsaPrivateKey = rsaPrivateKey.replace("-----END PRIVATE KEY-----", "");

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(rsaPrivateKey));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);
        return privKey;
    }
}
