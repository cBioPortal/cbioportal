/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cbioportal.security.config.access;

// imports
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cbioportal.model.User;
import org.cbioportal.model.UserAuthorities;
import org.cbioportal.persistence.SecurityRepository;
import org.cbioportal.persistence.mybatis.StudyGroupMapper;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

@Service
@ConditionalOnProperty(name = "security.repository.type", havingValue = "lsaai")
//todo also condition that auth is oauth2
public class LsaaiOauth2DrivenResolver implements SecurityRepository<OidcUser> {

    private static final Logger log = LoggerFactory.getLogger(LsaaiOauth2DrivenResolver.class);

    @Autowired
    private StudyGroupMapper studyGroupMapper;

    @Value("${security.repository.lsaai.userinfo}")
    private String apiUrl;

    private UserAuthorities authorities = null;

    /**
     * Always returns a valid user.
     *
     * @param username String
     * @return User
     */
    @Override
    public User getPortalUser(String username, OidcUser user) {
        if (authorities == null) {
            parseUserInfo(username, user.getUserInfo());
        }
        return new User(username, username, true);
    }

    /**
     * Given a user id, returns a UserAuthorities instance.
     * If username does not exist in db, returns null.
     *
     * @param username String
     * @return UserAuthorities
     */
    @Override
    public UserAuthorities getPortalUserAuthorities(String username, OidcUser user) {
        return authorities;
    }

    @Override
    public void addPortalUser(User user) {
        //no-op
    }

    @Override
    public void addPortalUserAuthorities(UserAuthorities userAuthorities) {
        //no-op
    }

    /**
     * Given an internal cancer study id, returns a set of upper case cancer study group strings.
     * Returns empty set if cancer study does not exist or there are no groups.
     *
     * @param internalCancerStudyId Integer
     * @return Set<String> cancer study group strings in upper case
     */
    @Override
    public Set<String> getCancerStudyGroups(Integer internalCancerStudyId) {
        String groups = studyGroupMapper.getCancerStudyGroups(internalCancerStudyId);
        if (groups == null) {
            return Collections.emptySet();
        }
        return new HashSet<String>(Arrays.asList(groups.toUpperCase().split(";"))); 
    }

    private void parseUserInfo(String username, OidcUserInfo info) {
        if (info == null) {
            log.warn("LSAAI is missing user info object " + username);
            return;
        }

        var claims = info.getClaims();
        String[] entitlements;
        Object entitlementObj = claims.get("eduperson_entitlement");

        if (entitlementObj instanceof String[]) {
            entitlements = (String[]) entitlementObj; // Cast to String array
        } else if (entitlementObj instanceof List) {
            List<?> entitlementList = (List<?>) entitlementObj; // Cast to List
            entitlements = entitlementList.toArray(new String[0]); // Convert List to String array
        } else {
            log.warn("LSAAI is missing eduperson_entitlement information: user access rights cannot be validated for user " + username);
            return; 
        }
        
        // We get institution - project couples:
        List<String> result = new ArrayList<>();
        for (String rule : entitlements) {
            var roles = extractNLastFromArgRule(rule, 2);
            if (roles != null) {
                result.add(roles[0] + ":" + roles[1]);
            }
        }
        authorities = new UserAuthorities(username, result);
    }

    private String[] extractNLastFromArgRule(String input, int size) {
        int index = input.indexOf(":res:");
        if (index >= 0) {
            // Keep only the part after ':res:' and skip it
            String attributes = input.substring(index + 5);
            String cleanedInput = attributes.contains("#") ? attributes.split("#")[0] : attributes;
            String[] parts = cleanedInput.split(":");

            if (parts.length >= size) {
                return Arrays.copyOfRange(parts, parts.length - size, parts.length);
            }
            log.debug("Parsed ARG rule does not contain enough elements! " + input);
        }
        return null;
    }
}
