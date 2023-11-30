package org.cbioportal.utils.security;

public class PortalSecurityConfig {

    // This method is the equivalent of GlobalProperties.usersMustAuthenticate()
    // method in the org.mskcc.cbio.portal.util package. Update both when changes are needed.
    public static boolean userAuthorizationEnabled(String authenticate) {
        return authenticate != null
            && !authenticate.equals("false");
    }
    
}
