
package org.cbioportal.service.util;

import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MskWholeSlideViewerTokenGenerator {
	// ~ Static fields
    // ========================================================================================================

    private final static Logger logger = LoggerFactory.getLogger(MskWholeSlideViewerTokenGenerator.class);
    private static final String PORTAL_NAME = "cbioportal";

	// ~ Methods
	// ========================================================================================================

	public static String generateTokenByHmacSHA256(String username, String secretKey, String timeStamp) {
        try {
            String message = PORTAL_NAME + "?" + "user=" + username + "&t=" + timeStamp;

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            String hash = Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(message.getBytes()));
            return hash;
        }
        catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error generate msk Whole slide viewer token: " + e.getMessage());
            }
            return null;
        }
	}
}
