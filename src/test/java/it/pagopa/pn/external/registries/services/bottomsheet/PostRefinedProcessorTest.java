package it.pagopa.pn.external.registries.services.bottomsheet;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.services.io.dto.PreconditionContentInt;
import it.pagopa.pn.external.registries.util.AppIOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static it.pagopa.pn.external.registries.util.AppIOUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostRefinedProcessorTest {

    private PostRefinedProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new PostRefinedProcessor();
    }

    @Test
    void process_shouldSetFieldsCorrectly() {
        PreconditionContentInt dto = new PreconditionContentInt();

        PnExternalRegistriesConfig cfg = mock(PnExternalRegistriesConfig.class);
        PnExternalRegistriesConfig.AppIoTemplate template = mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        when(cfg.getAppIoTemplate()).thenReturn(template);
        when(template.getMarkdownDisclaimerAfterRefinementAppIoMessage())
                .thenReturn(IUN_PLACEHOLDER + " " + SENDER_DENOMINATION_PLACEHOLDER + " " + SUBJECT_PLACEHOLDER);

        BottomSheetContext context = BottomSheetContext.builder()
                .iun("iun123")
                .senderDenomination("sender")
                .subject("subject")
                .build();

        PreconditionContentInt result = processor.process(dto, context, cfg);

        assertEquals(AppIOUtils.REFINED_TITLE, result.getTitle());
        assertEquals("iun123 sender subject", result.getMarkdown());
    }
}