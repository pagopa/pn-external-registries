package it.pagopa.pn.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

public class TestUtils {

    private static final ObjectMapper om;

    static {
        om = new ObjectMapper();
//        JSR310Module javaTimeModule = new JSR310Module();
//        om.registerModule(javaTimeModule);
    }

    public static String toJson(Object o) {
        try {
            return om.writeValueAsString(o);
        } catch (Exception e) {
            return null;
        }
    }

}
