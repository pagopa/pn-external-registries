package it.pagopa.pn.external.registries.services.bottomsheet;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.services.io.dto.PreconditionContentInt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.external.registries.util.AppIOUtils.*;

@Component
@Slf4j
public class CancelledProcessor implements BottomSheetProcessor{
    @Override
    public PreconditionContentInt process(PreconditionContentInt preconditionContentInt, BottomSheetContext context, PnExternalRegistriesConfig cfg) {
        log.debug("CancelledProcessor process start for iun: {}", context.getIun());
        preconditionContentInt.setTitle(CANCELLED_TITLE);

        preconditionContentInt.setMarkdown(cfg.getAppIoTemplate().getMarkdownDisclaimerCancelledAppIoMessage()
                .replace(SENDER_DENOMINATION_PLACEHOLDER, context.getSenderDenomination())
                .replace(IUN_PLACEHOLDER, context.getIun()));
        return preconditionContentInt;
    }
}
