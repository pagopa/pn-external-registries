package it.pagopa.pn.external.registries.pdnd.utils;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;

/**
 {
 "kid": "J04kdrHD0afRpQeQWT_4B2Vkp9yQiViA9-mv2MSMRCM",
 "typ": "JWT",
 "alg": "RS256"
 }

 {
 "iss": "43ce03cd-30ae-4e79-be48-dbb40207e3e1",
 "sub": "43ce03cd-30ae-4e79-be48-dbb40207e3e1",
 "aud": "test.interop.pagopa.it",
 "jti": "4b7377bc-bace-4c4a-aacc-9d36f1a8c74b",
 "iat": 1649665803,
 "exp": 1649752203
 }

 */
public class JWTBuilder {

    private PnExternalRegistriesConfig config;

    public JWTBuilder (PnExternalRegistriesConfig config){
        this.config=config;
    }

    public String sign

}
