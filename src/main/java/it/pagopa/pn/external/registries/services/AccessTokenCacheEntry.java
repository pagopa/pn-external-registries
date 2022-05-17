package it.pagopa.pn.external.registries.services;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import it.pagopa.pn.external.registries.generated.openapi.pdnd.client.v1.dto.TokenTypeDto;
import it.pagopa.pn.external.registries.generated.openapi.pdnd.client.v1.dto.ClientCredentialsResponseDto;


@Getter
@Setter
@Slf4j
class AccessTokenCacheEntry {
    private String accessToken;
    private TokenTypeDto tokenType;
    private long expiresAtEpochMillis;

    private final String purposeId;

    public AccessTokenCacheEntry(String purposeId) {
        this.purposeId = purposeId;
    }

    public void setClientCredentials(ClientCredentialsResponseDto clientCredential) {
        accessToken = clientCredential.getAccessToken();
        tokenType = clientCredential.getTokenType();
        expiresAtEpochMillis = System.currentTimeMillis() + (clientCredential.getExpiresIn().longValue() * 1000L);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAtEpochMillis;
    }
}
