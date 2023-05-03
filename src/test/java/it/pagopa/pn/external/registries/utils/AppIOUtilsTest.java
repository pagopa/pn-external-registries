package it.pagopa.pn.external.registries.utils;

import it.pagopa.pn.external.registries.util.AppIOUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppIOUtilsTest {


    @Test
    void build() {
        final String iun = "iun";
        final String recipientInternalId = "recipientId";
        final String expectedPK = "SENT##iun##recipientId";

        String actualPK = AppIOUtils.buildPkProbableSchedulingAnalogDate(iun, recipientInternalId);

        assertThat(actualPK).isEqualTo(expectedPK);
    }
}
