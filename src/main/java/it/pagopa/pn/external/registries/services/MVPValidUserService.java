package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.external.registries.generated.openapi.msclient.io.v1.dto.FiscalCodePayload;
import it.pagopa.pn.external.registries.generated.openapi.server.valid.mvp.user.v1.dto.MvpUserDto;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOOptInClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class MVPValidUserService {
    private final IOOptInClient client;

    public MVPValidUserService(IOOptInClient client) {
        this.client = client;
    }

    public Mono<MvpUserDto> checkValidUser(Mono<String> body) {
        return body
                .flatMap( taxId -> {
                    log.info("Get mvp user profile by post taxId={}", LogUtils.maskTaxId(taxId));
                    FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
                    fiscalCodePayload.setFiscalCode( taxId );

                    return client.getProfileByPOST( fiscalCodePayload ).map( res ->{
                        log.info("Response getProfileByPOST, user with taxId={} have AppIo activated and isUserAllowed={}", LogUtils.maskTaxId(taxId), res.getSenderAllowed());
                        
                        return new MvpUserDto()
                                .taxId(taxId)
                                .valid(true);
                    }).onErrorResume( WebClientResponseException.class, exception ->{
                        if(HttpStatus.NOT_FOUND.equals(exception.getStatusCode())){
                            log.info("Response status is 'NOT_FOUND' user with taxId={} haven't AppIo activated ", LogUtils.maskTaxId(taxId));
                            return Mono.just( new MvpUserDto()
                                    .taxId(taxId)
                                    .valid(false)
                            );
                        }
                        log.error("Error in call getProfileByPOST ex={} for taxId={} ", exception, LogUtils.maskTaxId(taxId));
                        return Mono.error(exception);
                    });
                });
    }
}
