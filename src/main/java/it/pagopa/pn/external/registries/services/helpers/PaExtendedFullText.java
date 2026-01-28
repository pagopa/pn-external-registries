package it.pagopa.pn.external.registries.services.helpers;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryExtendedDto;
import reactor.core.publisher.Flux;


public interface PaExtendedFullText {
    Flux<PaSummaryExtendedDto> extendedFullTextSearch(String query, Boolean onlySons);
}