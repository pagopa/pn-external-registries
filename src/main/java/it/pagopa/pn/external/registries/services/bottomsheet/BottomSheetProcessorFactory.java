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
    private final PostRefinedProcessor postRefinedProcessor;

    public BottomSheetProcessor getBottomSheetProcessor(BottomSheetContext context) {
        log.info("Get BottomSheet processor with context: {}", context);

        if (context.getRefinementOrViewDate() != null &&
                context.getRefinementOrViewDate().isAfter(Instant.now())) {
            log.debug("PostRefinedProcessor selected");
            return postRefinedProcessor;
        }

        if (context.getDeliveryMode() != null &&
                context.getDeliveryMode() == DeliveryModeInt.ANALOG &&
                context.getSchedulingAnalogDate().isAfter(Instant.now())) {
            log.debug("AnalogPostSchedulingProcessor selected");
            return analogPostSchedulingProcessor;
        }

        if (context.getDeliveryMode() != null &&
                context.getDeliveryMode() == DeliveryModeInt.ANALOG &&
                context.getSchedulingAnalogDate().isBefore(Instant.now())) {
            log.debug("AnalogPreSchedulingProcessor selected");
            return analogPreSchedulingProcessor;
        }

        if (context.getDeliveryMode() != null &&
                context.getDeliveryMode() == DeliveryModeInt.DIGITAL) {
            log.debug("DigitalProcessor selected");
            return digitalProcessor;
        }

        throw new PnInternalException("No BottomSheetProcessor found for context:" + context, ERROR_CODE_EXTERNALREGISTRIES_BOTTOM_SHEET_PROCESSOR_NOT_FOUND);
    }
}
