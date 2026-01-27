package it.pagopa.pn.external.registries.services.bottomsheet;


import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.services.io.dto.PreconditionContentInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static it.pagopa.pn.external.registries.util.AppIOUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CancelledProcessorTest {

    private CancelledProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CancelledProcessor();
    }

    @Test
    void process_shouldSetFieldsCorrectly() {
        PreconditionContentInt dto = new PreconditionContentInt();
        BottomSheetContext context = BottomSheetContext.builder()
                .isCancelled(true)
                .iun("iun123")
                .senderDenomination("sender")
                .build();

        PnExternalRegistriesConfig cfg = mock(PnExternalRegistriesConfig.class);
        PnExternalRegistriesConfig.AppIoTemplate template = mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        when(cfg.getAppIoTemplate()).thenReturn(template);
        when(template.getMarkdownDisclaimerCancelledAppIoMessage())
                .thenReturn(IUN_PLACEHOLDER + " " + SENDER_DENOMINATION_PLACEHOLDER + " ");

        PreconditionContentInt result = processor.process(dto, context, cfg);

        assertEquals(CANCELLED_TITLE, result.getTitle());
        assertEquals("iun123 sender ", result.getMarkdown());
    }

}