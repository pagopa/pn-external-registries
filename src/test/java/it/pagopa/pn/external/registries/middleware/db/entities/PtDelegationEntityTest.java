package it.pagopa.pn.external.registries.middleware.db.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class PtDelegationEntityTest {

    @Test
    void buildEventKey() {
        Assertions.assertEquals("ente-A#2026-11-02T10:00:00.000000Z",
                PtDelegationEntity.buildEventKey("ente-A", Instant.parse("2026-11-02T10:00:00Z")));
    }

    @Test
    void buildEventKeyKeepsMicros() {
        Assertions.assertEquals("ente-A#2026-11-02T10:00:00.123456Z",
                PtDelegationEntity.buildEventKey("ente-A", Instant.parse("2026-11-02T10:00:00.123456Z")));
    }

    @Test
    void buildEventKeyPadsMillisToMicros() {
        Assertions.assertEquals("ente-A#2026-11-02T10:00:00.120000Z",
                PtDelegationEntity.buildEventKey("ente-A", Instant.parse("2026-11-02T10:00:00.12Z")));
    }

    @Test
    void buildEventKeyTruncatesToMicros() {
        Assertions.assertEquals("ente-A#2026-11-02T10:00:00.123456Z",
                PtDelegationEntity.buildEventKey("ente-A", Instant.parse("2026-11-02T10:00:00.123456789Z")));
    }

    @Test
    void buildEventKeyKeepsChronologicalOrder() {
        String withoutFraction = PtDelegationEntity.buildEventKey("ente-A", Instant.parse("2026-11-02T10:00:00Z"));
        String withMicros = PtDelegationEntity.buildEventKey("ente-A", Instant.parse("2026-11-02T10:00:00.000001Z"));
        String withMillis = PtDelegationEntity.buildEventKey("ente-A", Instant.parse("2026-11-02T10:00:00.5Z"));

        Assertions.assertTrue(withoutFraction.compareTo(withMicros) < 0);
        Assertions.assertTrue(withMicros.compareTo(withMillis) < 0);
    }

    @Test
    void buildEventKeyPrefix() {
        Assertions.assertEquals("ente-A#", PtDelegationEntity.buildEventKeyPrefix("ente-A"));
    }

    @Test
    void buildEventKeyStartsWithPrefix() {
        Assertions.assertTrue(PtDelegationEntity.buildEventKey("ente-A", Instant.parse("2026-11-02T10:00:00Z"))
                .startsWith(PtDelegationEntity.buildEventKeyPrefix("ente-A")));
    }
}
