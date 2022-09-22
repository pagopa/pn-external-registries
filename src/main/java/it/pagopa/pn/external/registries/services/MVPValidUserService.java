package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.external.registries.generated.openapi.server.valid.mvp.user.v1.dto.MvpUserDto;
import it.pagopa.pn.external.registries.middleware.msclient.io.IOActivationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.external.registries.middleware.msclient.io.IOActivationClient.IO_STATUS_ACTIVE;

@Service
@Slf4j
public class MVPValidUserService {
    private final IOActivationClient client;

    public MVPValidUserService(IOActivationClient client) {
        this.client = client;
    }

    public Mono<MvpUserDto> checkValidUser(Mono<String> body) {
        return body
                .flatMap( taxId -> {
                    log.info("Get mvp user profile by post taxId={}", LogUtils.maskTaxId(taxId));

                    return client.getServiceActivation( taxId ).map( res ->{
                        log.info("Response getProfileByPOST, user with taxId={} have AppIo activated and isUserAllowed={}", LogUtils.maskTaxId(taxId), res.getStatus());
                        
                        return new MvpUserDto()
                                .taxId(taxId)
                                .valid(IO_STATUS_ACTIVE.equals(res.getStatus()));
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
