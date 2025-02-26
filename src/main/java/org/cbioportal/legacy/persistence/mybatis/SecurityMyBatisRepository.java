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

package org.cbioportal.legacy.persistence.mybatis;

// imports
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cbioportal.legacy.model.User;
import org.cbioportal.legacy.model.UserAuthorities;
import org.cbioportal.legacy.persistence.SecurityRepository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Security resolver usable for any authentication scheme.
 * Requires presence of user entries & roles in the local database.
 * Even a successful authentication over a third-party provider will not
 * be accepted if the user does not exist in the database.
 */
@Repository
@ConditionalOnProperty(name = "security.repository.type", havingValue = "cbioportal", matchIfMissing = true)
public class SecurityMyBatisRepository implements SecurityRepository<Object> {

    private static final Logger log = LoggerFactory.getLogger(SecurityMyBatisRepository.class);

    @Autowired
    private SecurityMapper securityMapper;
    @Autowired
    private StudyGroupMapper studyGroupMapper;
    
    /**
     * Given a user id, returns a user instance.
     * If username does not exist in db, returns null.
     *
     * @param username String
     * @param _unusedUserInfo arbitrary user info dependent on what authentication is employed
     * @return User
     */
    @Override
    public User getPortalUser(String username, Object _unusedUserInfo) {
        User user = securityMapper.getPortalUser(username);
        if (user != null) {
            log.debug("User " + username + " was found in the users table, email is " + user.getEmail());
        } else {
            log.debug("User " + username + " is null");
        }
        return user;
    }

    /**
     * Given a user id, returns a UserAuthorities instance.
     * If username does not exist in db, returns null.
     *
     * @param username String
     * @param _unusedUserInfo arbitrary user info dependent on what authentication is employed
     * @return UserAuthorities
     */
    @Override
    public UserAuthorities getPortalUserAuthorities(String username, Object _unusedUserInfo) {
        return securityMapper.getPortalUserAuthorities(username);
    }

    @Override
    public void addPortalUser(User user) {
        securityMapper.addPortalUser(user);
    }

    @Override
    public void addPortalUserAuthorities(UserAuthorities userAuthorities) {
        for (String authority : userAuthorities.getAuthorities()) {
            securityMapper.addPortalUserAuthority(userAuthorities.getEmail(), authority);
        }
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
}
