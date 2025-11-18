package it.pagopa.pn.external.registries.services.bottomsheet;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.services.io.dto.PreconditionContentInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static it.pagopa.pn.external.registries.util.AppIOUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalogPostSchedulingProcessorTest {

    private AnalogPostSchedulingProcessor processor;
    private PnExternalRegistriesConfig config;

    @BeforeEach
    void setUp() {
        config = mock(PnExternalRegistriesConfig.class);
        processor = new AnalogPostSchedulingProcessor(config);
    }

    @Test
    void process_shouldSetFieldsCorrectly() {
        PreconditionContentInt dto = new PreconditionContentInt();
        BottomSheetContext context = BottomSheetContext.builder()
                .iun("iun123")
                .senderDenomination("sender")
                .subject("subject")
                .build();

        String analogCost = "5.00 EUR";
        when(config.getBottomsheetAnalogCost()).thenReturn(analogCost);

        PnExternalRegistriesConfig cfg = mock(PnExternalRegistriesConfig.class);
        PnExternalRegistriesConfig.AppIoTemplate template = mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        when(cfg.getAppIoTemplate()).thenReturn(template);
        when(template.getMarkdownDisclaimerAfterAnalogDateAppIoMessage())
                .thenReturn(IUN_PLACEHOLDER + " " + SENDER_DENOMINATION_PLACEHOLDER + " " + SUBJECT_PLACEHOLDER + " " + ANALOG_COST_PLACEHOLDER);

        PreconditionContentInt result = processor.process(dto, context, cfg);

        assertEquals(POST_ANALOG_TITLE, result.getTitle());
        assertEquals("iun123 sender subject 5.00 EUR", result.getMarkdown());
    }

}