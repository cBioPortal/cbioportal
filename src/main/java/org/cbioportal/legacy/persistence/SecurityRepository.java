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

package org.cbioportal.legacy.persistence;

// imports
import java.util.Set;

import org.cbioportal.legacy.model.User;
import org.cbioportal.legacy.model.UserAuthorities;

/**
 * The resolver class implementing SecurityRepository interface 
 * can define how users (and their rights) are evaluated.
 * Depending on the resolver type (template), it can be used in different
 * contexts. For example, FullAccessResolver implements SecurityRepository<Object> 
 * can be used anywhere since it implements object. If you need to access specific properties 
 * of the user authentication context, you have to implement for example 
 * SecurityRepository<OidcUser> interface, but then your resolver is usable only
 * with authentication type of oauth2.
 */
public interface SecurityRepository<AuthUserContext> {

    /**
     * Given a user id, returns a user instance.
     * If username does not exist in db, returns null.
     *
     * @param username String
     * @param user object that has necessary user information
     * @return User
     */
    User getPortalUser(String username, AuthUserContext user);

    /**
     * Given a user id, returns a UserAuthorities instance.
     * If username does not exist in db, returns null.
     *
     * @param username String
     * @param user object that has necessary user information
     * @return UserAuthorities
     */
    UserAuthorities getPortalUserAuthorities(String username, AuthUserContext user);

    void addPortalUser(User user);
    void addPortalUserAuthorities(UserAuthorities userAuthorities);

    /**
     * Given an internal cancer study id, returns a set of upper case cancer study group strings.
     * Returns empty set if cancer study does not exist or there are no groups.
     *
     * @param internalCancerStudyId Integer
     * @return Set<String> cancer study group strings in upper case
     */
    Set<String> getCancerStudyGroups(Integer internalCancerStudyId);
}
