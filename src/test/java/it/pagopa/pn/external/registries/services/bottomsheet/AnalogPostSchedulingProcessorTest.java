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

class AnalogPostSchedulingProcessorTest {

    private AnalogPostSchedulingProcessor processor;
    private BottomSheetContext context;

    @BeforeEach
    void setUp() {
        processor = new AnalogPostSchedulingProcessor();
        context = mock(BottomSheetContext.class);
    }

    @Test
    void process_shouldSetFieldsCorrectly() {
        Map<String, String> params = new HashMap<>();
        params.put(IUN_PARAM, "iun789");
        params.put(SENDER_DENOMINATION_PARAM, "sender3");
        params.put(SUBJECT_PARAM, "subject3");

        PreconditionContentInt dto = PreconditionContentInt.builder()
                .messageParams(params)
                .build();

        dto.setMessageParams(params);

        PnExternalRegistriesConfig cfg = mock(PnExternalRegistriesConfig.class);
        PnExternalRegistriesConfig.AppIoTemplate template = mock(PnExternalRegistriesConfig.AppIoTemplate.class);
        when(cfg.getAppIoTemplate()).thenReturn(template);
        when(template.getMarkdownDisclaimerAfterDateAppIoMessage())
                .thenReturn(IUN_PLACEHOLDER + " " + SENDER_DENOMINATION_PLACEHOLDER + " " + SUBJECT_PLACEHOLDER);

        PreconditionContentInt result = processor.process(dto, context, cfg);

        assertEquals(POST_ANALOG_TITLE, result.getTitle());
        assertEquals(POST_ANALOG_MESSAGE_CODE, result.getMessageCode());
        assertEquals("iun789 sender3 subject3", result.getMarkdown());
    }

}