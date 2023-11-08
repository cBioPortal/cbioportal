package org.cbioportal.security.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ClaimRoleExtractorUtil {
    private static final Logger log = LoggerFactory.getLogger(ClaimRoleExtractorUtil.class);
    private static final String CLAIM_RESOURCE_ACCESS = "resource_access";
    private static final String CLAIM_ROLES = "roles";
    
    public static Collection<String> extractClientRoles(final String clientId, final Map<String, Object> claims) {
        try {
            if(claims.containsKey(CLAIM_RESOURCE_ACCESS)) {
                var realmAccess = (Map<String, Object>) claims.get(CLAIM_RESOURCE_ACCESS);
                var clientIdAccess = (Map<String, Object>) realmAccess.get(clientId);
                return (clientIdAccess.containsKey(CLAIM_ROLES)) ? (Collection<String>) clientIdAccess.get(CLAIM_ROLES) : Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error Grabbing Client Roles from OIDC User Info: Realm roles must follow the convention resource_access:client_id:roles");
        }
        return Collections.emptyList(); 
    }
    
}
