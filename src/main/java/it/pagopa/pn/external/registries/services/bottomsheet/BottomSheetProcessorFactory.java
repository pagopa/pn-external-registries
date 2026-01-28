package it.pagopa.pn.external.registries.services.bottomsheet;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static it.pagopa.pn.external.registries.exceptions.PnExternalregistriesExceptionCodes.ERROR_CODE_EXTERNALREGISTRIES_BOTTOM_SHEET_PROCESSOR_NOT_FOUND;

@Service
@Slf4j
@AllArgsConstructor
public class BottomSheetProcessorFactory {
    private final AnalogPreSchedulingProcessor analogPreSchedulingProcessor;
    private final AnalogPostSchedulingProcessor analogPostSchedulingProcessor;
    private final DigitalProcessor digitalProcessor;
    private final CancelledProcessor cancelledProcessor;
    private final PostRefinedProcessor postRefinedProcessor;

    public BottomSheetProcessor getBottomSheetProcessor(BottomSheetContext context) {
        log.info("Get BottomSheet processor with context: {}", context);
        Instant now = Instant.now();

        if(context.isCancelled()){
            log.debug("CancelledProcessor selected");
            return cancelledProcessor;
        }
        if (context.getRefinementOrViewDate() != null &&
                now.isAfter(context.getRefinementOrViewDate())) {
            log.debug("PostRefinedProcessor selected");
            return postRefinedProcessor;
        }

        if (context.getDeliveryMode() != null &&
                context.getDeliveryMode() == ExtendedDeliveryMode.ANALOG &&
                now.isAfter(context.getSchedulingAnalogDate())) {
            log.debug("AnalogPostSchedulingProcessor selected");
            return analogPostSchedulingProcessor;
        }

        if (context.getDeliveryMode() != null &&
                context.getDeliveryMode() == ExtendedDeliveryMode.ANALOG &&
                now.isBefore(context.getSchedulingAnalogDate())) {
            log.debug("AnalogPreSchedulingProcessor selected");
            return analogPreSchedulingProcessor;
        }

        if (context.getDeliveryMode() != null &&
                context.getDeliveryMode() == ExtendedDeliveryMode.DIGITAL) {
            log.debug("DigitalProcessor selected");
            return digitalProcessor;
        }

        throw new PnInternalException("No BottomSheetProcessor found for context:" + context, ERROR_CODE_EXTERNALREGISTRIES_BOTTOM_SHEET_PROCESSOR_NOT_FOUND);
    }
}
