package it.pagopa.pn.external.registries.services.bottomsheet;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class BottomSheetProcessorFactoryTest {

    private AnalogPreSchedulingProcessor analogPreSchedulingProcessor;
    private AnalogPostSchedulingProcessor analogPostSchedulingProcessor;
    private DigitalProcessor digitalProcessor;
    private PostRefinedProcessor postRefinedProcessor;
    private CancelledProcessor cancelledProcessor;
    private BottomSheetProcessorFactory factory;

    @BeforeEach
    void setUp() {
        analogPreSchedulingProcessor = mock(AnalogPreSchedulingProcessor.class);
        analogPostSchedulingProcessor = mock(AnalogPostSchedulingProcessor.class);
        digitalProcessor = mock(DigitalProcessor.class);
        postRefinedProcessor = mock(PostRefinedProcessor.class);
        cancelledProcessor = mock(CancelledProcessor.class);

        factory = new BottomSheetProcessorFactory(
                analogPreSchedulingProcessor,
                analogPostSchedulingProcessor,
                digitalProcessor,
                cancelledProcessor,
                postRefinedProcessor
        );
    }

    @Test
    void returnsPostRefinedProcessorWhenRefinementOrViewDateIsBeforeCurrentDate() {
        BottomSheetContext context = BottomSheetContext.builder()
                .isCancelled(false)
                .deliveryMode(ExtendedDeliveryMode.ANALOG)
                .refinementOrViewDate(Instant.now().minusSeconds(3600))
                .build();
        BottomSheetProcessor processor = factory.getBottomSheetProcessor(context);
        assertSame(postRefinedProcessor, processor);
    }

    @Test
    void returnsAnalogPostSchedulingProcessorWhenAnalogAndSchedulingDateIsBeforeCurrentDate() {
        BottomSheetContext context = BottomSheetContext.builder()
                .isCancelled(false)
                .deliveryMode(ExtendedDeliveryMode.ANALOG)
                .schedulingAnalogDate(Instant.now().minusSeconds(3600))
                .build();
        BottomSheetProcessor processor = factory.getBottomSheetProcessor(context);
        assertSame(analogPostSchedulingProcessor, processor);
    }

    @Test
    void returnsAnalogPreSchedulingProcessorWhenAnalogAndSchedulingDateIsAfterCurrentDate() {
        BottomSheetContext context = BottomSheetContext.builder()
                .isCancelled(false)
                .deliveryMode(ExtendedDeliveryMode.ANALOG)
                .schedulingAnalogDate(Instant.now().plus(Duration.ofMillis(3600)))
                .build();
        BottomSheetProcessor processor = factory.getBottomSheetProcessor(context);
        assertSame(analogPreSchedulingProcessor, processor);
    }

    @Test
    void returnsDigitalProcessorWhenDigital() {
        BottomSheetContext context = BottomSheetContext.builder()
                .isCancelled(false)
                .deliveryMode(ExtendedDeliveryMode.DIGITAL)
                .build();
        BottomSheetProcessor processor = factory.getBottomSheetProcessor(context);
        assertSame(digitalProcessor, processor);
    }

    @Test
    void returnsCancelledProcessor() {
        BottomSheetContext context = BottomSheetContext.builder()
                .isCancelled(true)
                .build();
        BottomSheetProcessor processor = factory.getBottomSheetProcessor(context);
        assertSame(cancelledProcessor, processor);
    }

    @Test
    void throwsExceptionWhenNoProcessorMatches() {
        BottomSheetContext context = BottomSheetContext.builder()
                .deliveryMode(null)
                .schedulingAnalogDate(null)
                .refinementOrViewDate(null)
                .build();

        PnInternalException ex = assertThrows(PnInternalException.class, () -> factory.getBottomSheetProcessor(context));
        assertTrue(ex.getProblem().getDetail().contains("No BottomSheetProcessor found"));
    }

}