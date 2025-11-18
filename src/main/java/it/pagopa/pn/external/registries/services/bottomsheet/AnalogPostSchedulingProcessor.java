package it.pagopa.pn.external.registries.services.bottomsheet;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.services.io.dto.PreconditionContentInt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.external.registries.util.AppIOUtils.*;

/**
 * Classe adibita alla gestione del BottomSheet nel caso in cui la modalità di spedizione della notifica sia analogica e la data di
 * schedulazione flusso analogico sia successiva alla data corrente.
 * */
@Component
@Slf4j
@RequiredArgsConstructor
public class AnalogPostSchedulingProcessor implements BottomSheetProcessor {

    private final PnExternalRegistriesConfig config;

    @Override
    public PreconditionContentInt process(PreconditionContentInt preconditionContentInt, BottomSheetContext context, PnExternalRegistriesConfig cfg) {
        log.debug("AnalogPostSchedulingProcessor process start for iun: {}", context.getIun());
        preconditionContentInt.setTitle(POST_ANALOG_TITLE);

        preconditionContentInt.setMarkdown(cfg.getAppIoTemplate().getMarkdownDisclaimerAfterAnalogDateAppIoMessage()
                .replace(IUN_PLACEHOLDER, context.getIun())
                .replace(SENDER_DENOMINATION_PLACEHOLDER, context.getSenderDenomination())
                .replace(SUBJECT_PLACEHOLDER, context.getSubject())
                .replace(ANALOG_COST_PLACEHOLDER, config.getBottomsheetAnalogCost()));
        return preconditionContentInt;
    }
}
