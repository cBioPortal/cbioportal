package org.cbioportal.utils;

import java.util.Base64;

public class Encoder {

    private Encoder() {}
    
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    public static final String DELIMITER = ":";

    public static String calculateBase64(String firstInput, String secondInput) {
        return BASE64_ENCODER.encodeToString((firstInput + DELIMITER + secondInput).getBytes());
    }

    public static String decodeBase64(String input) {
        return new String(BASE64_DECODER.decode(input));
    }

}
