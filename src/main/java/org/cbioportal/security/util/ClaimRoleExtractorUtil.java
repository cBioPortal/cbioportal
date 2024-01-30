package org.cbioportal.security.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ClaimRoleExtractorUtil {
    private static final Logger log = LoggerFactory.getLogger(ClaimRoleExtractorUtil.class);
    
    public static Collection<String> extractClientRoles(final Map<String, Object> claims, final String jwtRolesPath) {
        try {
            // Convert the map to a JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(claims);

            JsonNode rolesCursor = new ObjectMapper().readTree(jsonString);
            return extractClientRoles(rolesCursor, jwtRolesPath);
        } catch (Exception e) {
            log.error("Error extracting claims as a json string");
        }
        return Collections.emptyList(); 
    }

    public static Collection<String> extractClientRoles(final String claims, final String jwtRolesPath) {
        try {
            JsonNode rolesCursor = new ObjectMapper().readTree(claims);
            return extractClientRoles(rolesCursor, jwtRolesPath);
        } catch (Exception e) {
            log.error("Error converting Json String to JsonNode Object");
        }
        return Collections.emptyList();
    }
    
    public static Collection<String> extractClientRoles(final JsonNode claims, final String jwtRolesPath) {
        try {
            
            JsonNode rolesCursor = claims;
            for (var keyName : jwtRolesPath.split("::")) {
                if (rolesCursor.has(keyName)) {
                    rolesCursor = rolesCursor.get(keyName);
                } else {
                    throw new BadCredentialsException("Cannot Find user Roles in JWT Access Token ");
                }
                
            }
            return StreamSupport.stream(rolesCursor.spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Error Grabbing Client Roles from OIDC User Info: Realm roles must follow the convention {}", jwtRolesPath);
        }
        return Collections.emptyList();
    } 
    
    
}
