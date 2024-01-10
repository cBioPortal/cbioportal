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

package org.cbioportal.persistence.mybatis;

// imports
import org.apache.ibatis.annotations.Param;

import org.cbioportal.model.User;
import org.cbioportal.model.UserAuthorities;

/**
 * Interface to use to retrieve
 * portal user information.
 */
public interface SecurityMapper {

    /**
     * Given a user id, returns a user instance.
     * If username does not exist in db, returns null.
     *
     * @param username String
     * @return User
     */
    User getPortalUser(@Param("username") String username);

    /**
     * Given a user id, returns a UserAuthorities instance.
     * If username does not exist in db, returns null.
     *
     * @param username String
     * @return User
     */
    UserAuthorities getPortalUserAuthorities(@Param("username") String username);

    void addPortalUser(User user);
    void addPortalUserAuthority(@Param("email") String email, @Param("authority") String authority);

    /**
     * Given an internal cancer study id, returns groups string.
     * Returns null if cancer study does not exist.
     *
     * @param internalCancerStudyId Integer
     * @return String groups
     */
    String getCancerStudyGroups(Integer internalCancerStudyId);
}
