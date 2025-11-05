package it.pagopa.pn.external.registries.services.bottomsheet;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.services.io.dto.PreconditionContentInt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static it.pagopa.pn.external.registries.util.AppIOUtils.*;

/**
 * Classe adibita alla gestione del BottomSheet nel caso in cui la modalità di spedizione della notifica sia analogica e la data di
 * schedulazione flusso analogico sia precedente alla data corrente.
 * */
@Component
@Slf4j
public class AnalogPreSchedulingProcessor implements BottomSheetProcessor {

    @Override
    public PreconditionContentInt process(PreconditionContentInt preconditionContentInt, BottomSheetContext context, PnExternalRegistriesConfig cfg) {
        log.debug("AnalogPreSchedulingProcessor process start for iun: {}", context.getIun());
        Instant schedulingAnalogDate = context.getSchedulingAnalogDate();
        String localDateTimeItaly = LocalDateTime.ofInstant(schedulingAnalogDate, ZoneId.of("Europe/Rome")).format(PROBABLE_SCHEDULING_ANALOG_DATE_DATE_FORMATTER);
        String[] schedulingDateWithHourItaly = localDateTimeItaly.split(" ");
        preconditionContentInt.setTitle(PRE_ANALOG_TITLE);

        preconditionContentInt.setMarkdown(cfg.getAppIoTemplate().getMarkdownDisclaimerBeforeAnalogDateAppIoMessage()
                .replace(DATE_PLACEHOLDER, schedulingDateWithHourItaly[0])
                .replace(TIME_PLACEHOLDER, schedulingDateWithHourItaly[1])
                .replace(IUN_PLACEHOLDER, context.getIun())
                .replace(SENDER_DENOMINATION_PLACEHOLDER, context.getSenderDenomination())
                .replace(SUBJECT_PLACEHOLDER, context.getSubject()));
        return preconditionContentInt;
    }
}
