package it.pagopa.pn.external.registries.rest.v1;

import io.jsonwebtoken.SignatureAlgorithm;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.client.v1.dto.ClientCredentialsResponseDto;
import it.pagopa.pn.external.registries.pdnd.client.PDNDClient;

import it.pagopa.pn.external.registries.pdnd.utils.AssertionGenerator;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static io.jsonwebtoken.SignatureAlgorithm.RS256;

@RestController
@Slf4j
@RequestMapping(value = "v1/pdnd")
public class PDNDController {

    @Autowired
    private PDNDClient pdndClient;

    @Autowired
    PnExternalRegistriesConfig config;

    @RequestMapping(value = "/getToken", method = RequestMethod.GET)
    public Mono<ResponseEntity<String>> getToken() {
        log.info("*** getToken 2 ***");
        pdndClient.createToken().block();

        pdndClient.createToken().subscribe(t -> {
            log.info("***** dentro il loop *****");
            log.info("\n" + t.getAccessToken());
            String[] chunks = t.getAccessToken().split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();

            String header = new String(decoder.decode(chunks[0]));
            String payload = new String(decoder.decode(chunks[1]));
            log.info("Header: " + header);
            log.info("payload:" + payload);
            log.info("dentro il loop - dopo aver scritto il token");
        });
        return Mono.just(new ResponseEntity<String>("Hello World!", HttpStatus.OK));
    }

}
