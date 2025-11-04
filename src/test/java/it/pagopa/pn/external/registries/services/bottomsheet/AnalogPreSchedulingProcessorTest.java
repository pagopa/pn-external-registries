package it.pagopa.pn.external.registries.services.bottomsheet;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.services.io.dto.PreconditionContentInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static it.pagopa.pn.external.registries.util.AppIOUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalogPreSchedulingProcessorTest {

    private AnalogPreSchedulingProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new AnalogPreSchedulingProcessor();
    }

    @Test
    void process_shouldSetFieldsCorrectly() {
        PreconditionContentInt dto = new PreconditionContentInt();

        Instant schedulingDate = Instant.parse("2024-06-01T10:15:30.00Z");
        BottomSheetContext context = BottomSheetContext.builder()
                .iun("iunTest")
                .senderDenomination("senderTest")
                .subject("subjectTest")
                .schedulingAnalogDate(schedulingDate)
                .build();

        PnExternalRegistriesConfig cfg = mock(PnExternalRegistriesConfig.class);
        PnExternalRegistriesConfig.AppIoTemplate template = mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        when(cfg.getAppIoTemplate()).thenReturn(template);
        when(template.getMarkdownDisclaimerBeforeAnalogDateAppIoMessage())
                .thenReturn(DATE_PLACEHOLDER + " " + TIME_PLACEHOLDER + " " +
                        IUN_PLACEHOLDER + " " + SENDER_DENOMINATION_PLACEHOLDER + " " + SUBJECT_PLACEHOLDER);

        PreconditionContentInt result = processor.process(dto, context, cfg);

        assertEquals(PRE_ANALOG_TITLE, result.getTitle());

        // Check markdown contains replaced values
        assertTrue(result.getMarkdown().contains("iunTest"));
        assertTrue(result.getMarkdown().contains("senderTest"));
        assertTrue(result.getMarkdown().contains("subjectTest"));
        assertFalse(result.getMarkdown().contains(DATE_PLACEHOLDER));
        assertFalse(result.getMarkdown().contains(TIME_PLACEHOLDER));
    }

}