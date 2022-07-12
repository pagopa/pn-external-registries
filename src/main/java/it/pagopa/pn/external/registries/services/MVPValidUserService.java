package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.external.registries.generated.openapi.io.client.v1.dto.FiscalCodePayload;
import it.pagopa.pn.external.registries.generated.openapi.server.valid.mvp.user.v1.dto.MvpUserDto;
import it.pagopa.pn.external.registries.middleware.msclient.IOClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class MVPValidUserService {
    private final IOClient client;

    public MVPValidUserService(IOClient client) {
        this.client = client;
    }

    public Mono<MvpUserDto> checkValidUser(Mono<String> body) {
        return body
                .flatMap( r -> {
                    log.info("Get mvp user profile by post taxId={}", LogUtils.maskTaxId(r));
                    FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
                    fiscalCodePayload.setFiscalCode( r );
                    return client.getProfileByPOST( fiscalCodePayload ).then( Mono.just( r ) );
                })
                .map( r -> {
                    MvpUserDto res = new MvpUserDto();
                    res.setValid( true );
                    res.setTaxId( r );
                    return res;
                });
    }
}
