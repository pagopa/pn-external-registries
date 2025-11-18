package it.pagopa.pn.external.registries.util;

import java.time.format.DateTimeFormatter;

public class AppIOUtils {

    public static final String PRE_ANALOG_TITLE = "Questo messaggio contiene una comunicazione a valore legale";
    public static final String POST_ANALOG_TITLE = "Questo messaggio contiene una comunicazione a valore legale";
    public static final String DIGITAL_TITLE = "Questo messaggio contiene una comunicazione a valore legale";
    public static final String REFINED_TITLE = "Questo messaggio contiene una comunicazione a valore legale";

    public static final String DATE_PLACEHOLDER = "{{data}}";
    public static final String TIME_PLACEHOLDER = "{{ora}}";
    public static final String SENDER_DENOMINATION_PLACEHOLDER = "{{senderDenomination}}";
    public static final String IUN_PLACEHOLDER = "{{iun}}";
    public static final String SUBJECT_PLACEHOLDER = "{{subject}}";
    public static final String ANALOG_COST_PLACEHOLDER = "{{analogCost}}";

    public static final String PROBABLE_SCHEDULING_ANALOG_DATE_PK_PREFIX = "SENT";
    public static final String PROBABLE_SCHEDULING_ANALOG_DATE_DELIMITER_PK = "##";
    public static final DateTimeFormatter PROBABLE_SCHEDULING_ANALOG_DATE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private AppIOUtils() {}

    //SENT##iun##internalId
    public static String buildPkProbableSchedulingAnalogDate(String iun, String recipientInternalId) {
        return PROBABLE_SCHEDULING_ANALOG_DATE_PK_PREFIX + PROBABLE_SCHEDULING_ANALOG_DATE_DELIMITER_PK + iun +
                PROBABLE_SCHEDULING_ANALOG_DATE_DELIMITER_PK + recipientInternalId;
    }
}
