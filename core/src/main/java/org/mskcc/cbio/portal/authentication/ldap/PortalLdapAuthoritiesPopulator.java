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

package org.mskcc.cbio.portal.authentication.ldap;

import org.mskcc.cbio.portal.model.UserAuthorities;
import org.mskcc.cbio.portal.dao.PortalUserDAO;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.core.authority.AuthorityUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PortalLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

    // logger
    private static final Log log = LogFactory.getLog(PortalLdapAuthoritiesPopulator.class);

    private final ContextSource contextSource;
    private final PortalUserDAO portalUserDAO;

    /**
     * Constructor.
     *
     * Takes a ref to PortalUserDAO used to authenticate registered
     * users in the database.
     *
     * @param portalUserDAO PortalUserDAO
     */
    public PortalLdapAuthoritiesPopulator(ContextSource contextSource, PortalUserDAO portalUserDAO) {
        this.contextSource = contextSource;
        this.portalUserDAO = portalUserDAO;
    }

    /**
     * Get the list of authorities for the user.
     *
     * @param userData the context object which was returned by the LDAP authenticator.
     * @param username the user name of principle return by the LDAP authenticator.
     * @return the granted authorities for the given user.
     */
    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();

        if (username != null) {
            if (log.isDebugEnabled()) {
                log.debug("getGrantedAuthororites(), attempting to fetch portal user authorities, : " + username);
            }
            UserAuthorities authorities = portalUserDAO.getPortalUserAuthorities(username);
            if (authorities != null) {
                grantedAuthorities.addAll(
                        AuthorityUtils.createAuthorityList(authorities.getAuthorities().toArray(new String[authorities.getAuthorities().size()])));
            }
        }

        return grantedAuthorities;
    }
}
