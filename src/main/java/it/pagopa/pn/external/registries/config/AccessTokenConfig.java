package it.pagopa.pn.external.registries.config;

import lombok.Data;

@Data
public class AccessTokenConfig {

    private JwtConfig jwtCfg;

    private String clientAssertionType;
    private String grantType;
    private String clientId;

}
