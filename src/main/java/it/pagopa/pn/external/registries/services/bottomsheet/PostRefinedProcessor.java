package it.pagopa.pn.external.registries.services.bottomsheet;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.services.io.dto.PreconditionContentInt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.external.registries.util.AppIOUtils.*;

/**
 * Classe adibita alla gestione del BottomSheet nel caso in cui la notifica sia stata perfezionata.
 * */
@Component
@Slf4j
public class PostRefinedProcessor implements BottomSheetProcessor {

    @Override
    public PreconditionContentInt process(PreconditionContentInt preconditionContentInt, BottomSheetContext context, PnExternalRegistriesConfig cfg) {
        log.debug("PostRefinedProcessor process start for iun {}", context.getIun());
        preconditionContentInt.setMessageCode(REFINED_MESSAGE_CODE);
        preconditionContentInt.setTitle(REFINED_TITLE);

        preconditionContentInt.setMarkdown(cfg.getAppIoTemplate().getMarkdownDisclaimerAfterRefinementAppIoMessage()
                .replace(IUN_PLACEHOLDER, preconditionContentInt.getMessageParams().get(IUN_PARAM))
                .replace(SENDER_DENOMINATION_PLACEHOLDER, preconditionContentInt.getMessageParams().get(SENDER_DENOMINATION_PARAM))
                .replace(SUBJECT_PLACEHOLDER, preconditionContentInt.getMessageParams().get(SUBJECT_PARAM)));
        return preconditionContentInt;
    }
}
