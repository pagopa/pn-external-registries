package it.pagopa.pn.external.registries.services.bottomsheet;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.services.io.dto.PreconditionContentInt;
import it.pagopa.pn.external.registries.util.AppIOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static it.pagopa.pn.external.registries.util.AppIOUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostRefinedProcessorTest {

    private PostRefinedProcessor processor;
    private BottomSheetContext context;

    @BeforeEach
    void setUp() {
        processor = new PostRefinedProcessor();
        context = mock(BottomSheetContext.class);
    }

    @Test
    void process_shouldSetFieldsCorrectly() {
        Map<String, String> params = new HashMap<>();
        params.put(IUN_PARAM, "iun123");
        params.put(SENDER_DENOMINATION_PARAM, "sender");
        params.put(SUBJECT_PARAM, "subject");

        PreconditionContentInt dto = PreconditionContentInt.builder()
                .messageParams(params)
                .build();
        dto.setMessageParams(params);

        PnExternalRegistriesConfig cfg = mock(PnExternalRegistriesConfig.class);
        PnExternalRegistriesConfig.AppIoTemplate template = mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        when(cfg.getAppIoTemplate()).thenReturn(template);
        when(template.getMarkdownDisclaimerAfterRefinementAppIoMessage())
                .thenReturn(IUN_PLACEHOLDER + " " + SENDER_DENOMINATION_PLACEHOLDER + " " + SUBJECT_PLACEHOLDER);

        PreconditionContentInt result = processor.process(dto, context, cfg);

        assertEquals(AppIOUtils.REFINED_MESSAGE_CODE, result.getMessageCode());
        assertEquals(AppIOUtils.REFINED_TITLE, result.getTitle());
        assertEquals("iun123 sender subject", result.getMarkdown());
    }
}