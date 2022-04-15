package it.pagopa.pn.external.registries.pdnd.service;


import it.pagopa.pn.external.registries.pdnd.client.PDNDClient;
import it.pagopa.pn.external.registries.pdnd.dto.TokenCacheEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class AccessTokenCacheService {
    private final ConcurrentMap<String, TokenCacheEntry> accessTokenHolder = new ConcurrentHashMap<>();

    private final PDNDClient pdndClient;

    public AccessTokenCacheService(PDNDClient pdndClient) {
        this.pdndClient = pdndClient;
    }

    public Mono<String> getToken(String purposeId) {
        TokenCacheEntry accessToken;
        log.info("richiesta token per porpouseid -> " + purposeId);
        boolean initializeToken = false;

        accessToken = accessTokenHolder.get(purposeId);
        if (accessToken == null) {
            log.info("richiesta token per purposeId -> " + purposeId + " token null");
            accessToken = new TokenCacheEntry(purposeId);
            accessTokenHolder.put(purposeId, accessToken);
            initializeToken = true;
        } else {
            if (accessToken.isExpired()) {
                initializeToken = true;
                accessTokenHolder.remove(purposeId);
            }
        }
        if (initializeToken) {
            log.info("simulo chiamata a PDND per purposeId {} ... wait", purposeId);
            return pdndClient.createToken().flatMap(clientCredentials -> {
                TokenCacheEntry tok = new TokenCacheEntry("purposeId");
                tok.setClientCredentials(clientCredentials);
                accessTokenHolder.put(purposeId, tok);
                return Mono.just(tok.getAccessToken());
            });
        }
        return Mono.just(accessToken.getAccessToken());
    }
}
