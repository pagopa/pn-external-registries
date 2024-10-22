package it.pagopa.pn.external.registries.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SenderConfigurationUtils {
    private static final String CONFIG_PREFIX = "CFG-";

    public static String buildPk(String paId) {
        String pk = null;
        return pk = CONFIG_PREFIX + paId;
    }

    public static String getPk(String paId) {
        return paId.replace(CONFIG_PREFIX, "");
    }
}
