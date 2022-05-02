package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.pdnd.client.PDNDClient;

import it.pagopa.pn.external.registries.pdnd.service.AccessTokenCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(value = "v1/pdnd")
public class PDNDController {

    @Autowired
    private PDNDClient pdndClient;

    @Autowired
    PnExternalRegistriesConfig config;
    @Autowired
    AccessTokenCacheService tokenService;

    @RequestMapping(value = "/getToken", method = RequestMethod.GET)
    public Mono<ResponseEntity<String>> getToken() {
        log.debug("*** getToken 2 ***");
        tokenService.getToken("M2M",false);

        return Mono.just(new ResponseEntity<String>("Hello World!", HttpStatus.OK));
    }

}
