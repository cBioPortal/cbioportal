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

package org.cbioportal.security.config;

// imports
import java.util.Collections;
import java.util.Set;

import org.cbioportal.model.User;
import org.cbioportal.model.UserAuthorities;
import org.cbioportal.persistence.SecurityRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.stereotype.Service;


/**
 * Security resolver usable for any authentication scheme.
 * Allows any user authenticated by any auth method.
 * Allows access to public datasets only for any user.
 * Does not support non-public datasets.
 */
@Service
@ConditionalOnProperty(name = "security.repository.type", havingValue = "disabled")
public class SecurityDisabledRepository implements SecurityRepository<Object> {
    
    /**
     * Always returns a valid user.
     *
     * @param username String
     * @param user Object
     * @return User
     */
    @Override
    public User getPortalUser(String username, Object user) {
        return new User(username, username, true);
    }

    /**
     * Given a user id, returns a UserAuthorities instance.
     * If username does not exist in db, returns null.
     *
     * @param username String
     * @param user Object
     * @return UserAuthorities
     */
    @Override
    public UserAuthorities getPortalUserAuthorities(String username, Object user) {
        return new UserAuthorities();
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
        return Collections.emptySet();
    }
}
