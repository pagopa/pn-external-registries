package it.pagopa.pn.external.registries.pdnd.service;


import it.pagopa.pn.external.registries.generated.openapi.pdnd.client.v1.dto.ClientCredentialsResponseDto;
import it.pagopa.pn.external.registries.pdnd.client.PDNDClient;
import it.pagopa.pn.external.registries.pdnd.dto.TokenCacheEntry;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@Service
@Slf4j
public class AccessTokenCacheService {
    private final ConcurrentMap<String, TokenCacheEntry> accessTokenHolder = new ConcurrentHashMap<>();

    private final PDNDClient pdndClient;

    public AccessTokenCacheService(PDNDClient pdndClient) {
        this.pdndClient = pdndClient;
    }

    public Mono<String> getToken(String purposeId, boolean force) {
        TokenCacheEntry accessToken;
        log.info("richiesta token per porpouseid -> " + purposeId);
        boolean initializeToken = false;

        accessToken = accessTokenHolder.get(purposeId);
        if (accessToken == null) {
            log.info("richiesta token per purposeId -> " + purposeId + " token null");
            initializeToken = true;
        } else {
            if (accessToken.isExpired()) {
                initializeToken = true;
                accessTokenHolder.remove(purposeId);
            }else if (force){
                initializeToken = true;
                accessTokenHolder.remove(purposeId);
            }
        }
        if (initializeToken) {
            log.info("simulo chiamata a PDND per purposeId {} ... wait", purposeId);

            try {
                return pdndClient.createToken().flatMap(clientCredentials -> {
                    TokenCacheEntry tok = new TokenCacheEntry("purposeId");
                    tok.setClientCredentials(clientCredentials);
                    accessTokenHolder.put(purposeId, tok);
                    return Mono.just(tok.getAccessToken());
                });
            }catch(Exception e){
                log.error("Exceptiopn in getToken");
            }

        }
        return Mono.just(accessToken.getAccessToken());
    }

  }
