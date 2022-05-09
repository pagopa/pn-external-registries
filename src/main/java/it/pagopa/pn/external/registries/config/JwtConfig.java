package it.pagopa.pn.external.registries.config;

import lombok.Data;

@Data
public class JwtConfig {

    private String issuer;
    private String subject;
    private String audience;
    private String kid;

    private Integer clientAssertionTtl;
    private String keypairAlias;

}
