package it.pagopa.pn.external.registries.services;


import it.pagopa.pn.external.registries.middleware.msclient.PDNDClient;
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
        log.info("richiesta token per purposeId:{}  force:{}", purposeId, force);


        accessToken = accessTokenHolder.get(purposeId);

        if ( accessToken == null || force || accessToken.isExpired() ) {
            log.info("richiesta token per purposeId: {} forced, null or expired", purposeId);

            return requireNewAccessToken( purposeId );
        }
        else {

            return Mono.just(accessToken.getAccessToken());
        }


    }

    private Mono<String> requireNewAccessToken(String purposeId) {
        return pdndClient.createToken( purposeId )
            .map(clientCredentials -> {
                AccessTokenCacheEntry tok = new AccessTokenCacheEntry( purposeId );
                tok.setClientCredentials( clientCredentials );
                accessTokenHolder.put(purposeId, tok);
                return tok.getAccessToken();
            });
    }

}
