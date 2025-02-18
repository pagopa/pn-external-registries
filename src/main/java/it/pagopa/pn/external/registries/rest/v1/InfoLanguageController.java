package it.pagopa.pn.external.registries.rest.v1;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.api.AdditionalLangApi;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.AdditionalLanguagesDto;
import it.pagopa.pn.external.registries.services.InfoLanguageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class InfoLanguageController implements AdditionalLangApi {

    private final InfoLanguageService infoLanguageService;

    /**
     * GET /ext-registry-private/pa/v1/additional-lang/{paId}
     * Retrieve the additional language info chosen by pa
     *
     * @param paId An unique ID that identify a Public Administration (required)
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<AdditionalLanguagesDto>> getAdditionalLang(String paId, final ServerWebExchange exchange) {
        return infoLanguageService.retrievePaAdditionalLang(paId)
                .map(additionalLanguagesDto ->  ResponseEntity.ok().body(additionalLanguagesDto));
    }

    /**
     * PUT /ext-registry-private/pa/v1/additional-lang
     * Allows the configuration of a new additional language for pa
     *
     * @param additionalLanguagesDto  (optional)
     * @return OK (status code 200)
     *         or Invalid input (status code 400)
     *         or Internal Server Error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<AdditionalLanguagesDto>> putAdditionalLang(Mono<AdditionalLanguagesDto> additionalLanguagesDto,  final ServerWebExchange exchange) {
        return additionalLanguagesDto
                .flatMap(infoLanguageService::createOrUpdateLang)
                .map(additionalLangDto -> ResponseEntity.ok().body(additionalLangDto));
    }

}
