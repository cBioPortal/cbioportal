
package org.mskcc.cbio.portal.util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class MskWholeSlideViewerTokenGenerator {
	// ~ Static fields
    // ========================================================================================================
    
    private static Log LOG = LogFactory.getLog(MskWholeSlideViewerTokenGenerator.class);
	private static final String PORTAL_NAME = "cbioportal";

	// ~ Methods
	// ========================================================================================================

	public static String generateTokenByHmacSHA256(String username, String secretKey, String timeStamp) {
        try {
            String message = PORTAL_NAME + "?" + "user=" + username + "&t=" + timeStamp;

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(message.getBytes()));
            return hash;
        }
        catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error generate msk Whole slide viewer token: " + e.getMessage());
            }
            return null;
        }
	}
}
