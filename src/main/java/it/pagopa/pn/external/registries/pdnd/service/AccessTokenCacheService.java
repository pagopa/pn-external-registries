package it.pagopa.pn.external.registries.pdnd.service;


import it.pagopa.pn.external.registries.exceptions.PnInternalException;
import it.pagopa.pn.external.registries.pdnd.client.PDNDClient;
import it.pagopa.pn.external.registries.pdnd.utils.AssertionGeneratorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class AccessTokenCacheService {
    private final ConcurrentMap<String, AccessTokenCacheEntry> accessTokenHolder = new ConcurrentHashMap<>();

    private final PDNDClient pdndClient;

    public AccessTokenCacheService(PDNDClient pdndClient) {
        this.pdndClient = pdndClient;
    }

    public Mono<String> getToken(String purposeId, boolean force) {
        AccessTokenCacheEntry accessToken;
        log.info("richiesta token per porpouseid -> " + purposeId);
        boolean initializeToken = false;

        accessToken = accessTokenHolder.get(purposeId);

        if ( accessToken == null || force || accessToken.isExpired() ) {
            log.info("richiesta token per purposeId -> " + purposeId + " token null");

            return requireNewAccessToken( purposeId );
        }
        else {

            return Mono.just(accessToken.getAccessToken());
        }


    }

    private Mono<String> requireNewAccessToken(String purposeId) {
        try {
            return pdndClient.createToken().flatMap(clientCredentials -> {
                AccessTokenCacheEntry tok = new AccessTokenCacheEntry("purposeId");
                tok.setClientCredentials(clientCredentials);
                accessTokenHolder.put(purposeId, tok);
                return Mono.just(tok.getAccessToken());
            });
        }
        catch ( AssertionGeneratorException exc ) {
            throw new PnInternalException( "Asking token for purposeId=" + purposeId, exc );
        }
    }

}
