package it.pagopa.pn.external.registries.pdnd.dto;

import it.pagopa.pn.external.registries.generated.openapi.client.v1.dto.ClientCredentialsResponseDto;
import it.pagopa.pn.external.registries.generated.openapi.client.v1.dto.TokenTypeDto;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONObject;


@Getter
@Setter
@Slf4j
public class TokenCacheEntry {
    private String accessToken;
    private TokenTypeDto tokenType;
    private long expiresIn;
    private String purposeId;

    public TokenCacheEntry(String purposeId) {
        this.purposeId = purposeId;
    }

    public void setClientCredentials(ClientCredentialsResponseDto clientCredential) {
        accessToken = clientCredential.getAccessToken();
        tokenType = clientCredential.getTokenType();
        expiresIn = clientCredential.getExpiresIn() * 1000 + System.currentTimeMillis();
    }

    public boolean isExpired() {
        if (System.currentTimeMillis() > expiresIn)
            return true;
        else return false;
    }
}
