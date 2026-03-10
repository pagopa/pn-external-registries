package it.pagopa.pn.external.registries.middleware.queue.utils;

import it.pagopa.pn.commons.utils.MDCUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConsumerUtilsTest {

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void testSetMdc_WithAllHeaders() {
        // GIVEN
        String awsMessageId = "test-aws-message-id-123";
        String traceId = "test-trace-id-456";
        String iun = "test-iun-789";

        Map<String, Object> headers = new HashMap<>();
        headers.put("aws_messageId", awsMessageId);
        headers.put("X-Amzn-Trace-Id", traceId);
        headers.put("iun", iun);

        Message<String> message = new GenericMessage<>("test payload", headers);

        // WHEN
        ConsumerUtils.setMdc(message);

        // THEN
        assertEquals(awsMessageId, MDC.get(MDCUtils.MDC_PN_CTX_MESSAGE_ID));
        assertEquals(traceId, MDC.get(MDCUtils.MDC_TRACE_ID_KEY));
        assertEquals(iun, MDC.get(MDCUtils.MDC_PN_IUN_KEY));
    }

    @Test
    void testSetMdc_WithoutTraceId_GeneratesUUID() {
        // GIVEN
        String awsMessageId = "test-aws-message-id-123";
        String iun = "test-iun-789";

        Map<String, Object> headers = new HashMap<>();
        headers.put("aws_messageId", awsMessageId);
        headers.put("iun", iun);

        Message<String> message = new GenericMessage<>("test payload", headers);

        // WHEN
        ConsumerUtils.setMdc(message);

        // THEN
        assertEquals(awsMessageId, MDC.get(MDCUtils.MDC_PN_CTX_MESSAGE_ID));
        assertEquals(iun, MDC.get(MDCUtils.MDC_PN_IUN_KEY));

        // Verify that a trace ID was generated (should be a valid UUID string)
        String traceId = MDC.get(MDCUtils.MDC_TRACE_ID_KEY);
        assertNotNull(traceId);
        assertDoesNotThrow(() -> UUID.fromString(traceId));
    }

    @Test
    void testSetMdc_WithoutIun() {
        // GIVEN
        String awsMessageId = "test-aws-message-id-123";
        String traceId = "test-trace-id-456";

        Map<String, Object> headers = new HashMap<>();
        headers.put("aws_messageId", awsMessageId);
        headers.put("X-Amzn-Trace-Id", traceId);

        Message<String> message = new GenericMessage<>("test payload", headers);

        // WHEN
        ConsumerUtils.setMdc(message);

        // THEN
        assertEquals(awsMessageId, MDC.get(MDCUtils.MDC_PN_CTX_MESSAGE_ID));
        assertEquals(traceId, MDC.get(MDCUtils.MDC_TRACE_ID_KEY));
        assertNull(MDC.get(MDCUtils.MDC_PN_IUN_KEY));
    }

    @Test
    void testSetMdc_WithoutAwsMessageId() {
        // GIVEN
        String traceId = "test-trace-id-456";
        String iun = "test-iun-789";

        Map<String, Object> headers = new HashMap<>();
        headers.put("X-Amzn-Trace-Id", traceId);
        headers.put("iun", iun);

        Message<String> message = new GenericMessage<>("test payload", headers);

        // WHEN
        ConsumerUtils.setMdc(message);

        // THEN
        assertNull(MDC.get(MDCUtils.MDC_PN_CTX_MESSAGE_ID));
        assertEquals(traceId, MDC.get(MDCUtils.MDC_TRACE_ID_KEY));
        assertEquals(iun, MDC.get(MDCUtils.MDC_PN_IUN_KEY));
    }

    @Test
    void testSetMdc_WithEmptyHeaders() {
        // GIVEN
        Map<String, Object> headers = new HashMap<>();
        Message<String> message = new GenericMessage<>("test payload", headers);

        // WHEN
        ConsumerUtils.setMdc(message);

        // THEN
        assertNull(MDC.get(MDCUtils.MDC_PN_CTX_MESSAGE_ID));
        assertNull(MDC.get(MDCUtils.MDC_PN_IUN_KEY));

        // Verify that a trace ID was generated
        String traceId = MDC.get(MDCUtils.MDC_TRACE_ID_KEY);
        assertNotNull(traceId);
        assertDoesNotThrow(() -> UUID.fromString(traceId));
    }

    @Test
    void testSetMdc_WithNullIun() {
        // GIVEN
        String awsMessageId = "test-aws-message-id-123";
        String traceId = "test-trace-id-456";

        Map<String, Object> headers = new HashMap<>();
        headers.put("aws_messageId", awsMessageId);
        headers.put("X-Amzn-Trace-Id", traceId);
        headers.put("iun", null);

        Message<String> message = new GenericMessage<>("test payload", headers);

        // WHEN
        ConsumerUtils.setMdc(message);

        // THEN
        assertEquals(awsMessageId, MDC.get(MDCUtils.MDC_PN_CTX_MESSAGE_ID));
        assertEquals(traceId, MDC.get(MDCUtils.MDC_TRACE_ID_KEY));
        assertNull(MDC.get(MDCUtils.MDC_PN_IUN_KEY));
    }

    @Test
    void testSetMdc_ClearsPreviousMdcKeys() {
        // GIVEN - Set some initial MDC values
        MDC.put(MDCUtils.MDC_PN_CTX_MESSAGE_ID, "old-message-id");
        MDC.put(MDCUtils.MDC_TRACE_ID_KEY, "old-trace-id");
        MDC.put(MDCUtils.MDC_PN_IUN_KEY, "old-iun");

        String newAwsMessageId = "new-aws-message-id";
        String newTraceId = "new-trace-id";
        String newIun = "new-iun";

        Map<String, Object> headers = new HashMap<>();
        headers.put("aws_messageId", newAwsMessageId);
        headers.put("X-Amzn-Trace-Id", newTraceId);
        headers.put("iun", newIun);

        Message<String> message = new GenericMessage<>("test payload", headers);

        // WHEN
        ConsumerUtils.setMdc(message);

        // THEN - Verify old values are replaced with new ones
        assertEquals(newAwsMessageId, MDC.get(MDCUtils.MDC_PN_CTX_MESSAGE_ID));
        assertEquals(newTraceId, MDC.get(MDCUtils.MDC_TRACE_ID_KEY));
        assertEquals(newIun, MDC.get(MDCUtils.MDC_PN_IUN_KEY));
    }

    @Test
    void testSetMdc_WithDifferentPayloadTypes() {
        // GIVEN
        String awsMessageId = "test-aws-message-id-123";
        String traceId = "test-trace-id-456";

        Map<String, Object> headers = new HashMap<>();
        headers.put("aws_messageId", awsMessageId);
        headers.put("X-Amzn-Trace-Id", traceId);

        // Test with different payload types
        Message<Map<String, String>> message = new GenericMessage<>(new HashMap<>(), headers);

        // WHEN
        ConsumerUtils.setMdc(message);

        // THEN
        assertEquals(awsMessageId, MDC.get(MDCUtils.MDC_PN_CTX_MESSAGE_ID));
        assertEquals(traceId, MDC.get(MDCUtils.MDC_TRACE_ID_KEY));
    }
}

