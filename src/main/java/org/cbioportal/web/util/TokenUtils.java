package org.cbioportal.web.util;

import org.cbioportal.service.util.MskWholeSlideViewerTokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class TokenUtils {
    @Autowired
    private Environment env;
    public String getMskWholeSlideViewerToken() {
        // this token is for the msk portal 
        // the token is generated based on users' timestamp to let the slide viewer know whether the token is expired and then decide whether to allow the user to login the viewer
        // every time when we refresh the page or goto the new page, a new token should be generated
        String secretKey = env.getProperty("msk.whole.slide.viewer.secret.key");
        if (secretKey != null)
            secretKey = secretKey.trim();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String timeStamp = String.valueOf(System.currentTimeMillis());

        if (authentication != null && authentication.isAuthenticated() && secretKey != null &&
            !secretKey.isEmpty()) {
            return "{ \"token\":\"" + MskWholeSlideViewerTokenGenerator.generateTokenByHmacSHA256(
                authentication.getName(), secretKey, timeStamp) + "\", \"time\":\"" + timeStamp +
                "\"}";
        } else {
            return null;
        }
    }
}
