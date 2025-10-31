package it.pagopa.pn.external.registries.services.bottomsheet;


import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.services.io.dto.PreconditionContentInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static it.pagopa.pn.external.registries.util.AppIOUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DigitalProcessorTest {

    private DigitalProcessor processor;
    private BottomSheetContext context;

    @BeforeEach
    void setUp() {
        processor = new DigitalProcessor();
        context = mock(BottomSheetContext.class);
    }

    @Test
    void process_shouldSetFieldsCorrectly() {
        Map<String, String> params = new HashMap<>();
        params.put(IUN_PARAM, "iun456");
        params.put(SENDER_DENOMINATION_PARAM, "sender2");
        params.put(SUBJECT_PARAM, "subject2");

        PreconditionContentInt dto = PreconditionContentInt.builder()
                .messageParams(params)
                .build();

        PnExternalRegistriesConfig cfg = mock(PnExternalRegistriesConfig.class);
        PnExternalRegistriesConfig.AppIoTemplate template = mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        when(cfg.getAppIoTemplate()).thenReturn(template);
        when(template.getMarkdownDisclaimerDigitalAppIoMessage())
                .thenReturn(IUN_PLACEHOLDER + " " + SENDER_DENOMINATION_PLACEHOLDER + " " + SUBJECT_PLACEHOLDER);

        PreconditionContentInt result = processor.process(dto, context, cfg);

        assertEquals(DIGITAL_MESSAGE_CODE, result.getMessageCode());
        assertEquals(DIGITAL_TITLE, result.getTitle());
        assertEquals("iun456 sender2 subject2", result.getMarkdown());
    }

}