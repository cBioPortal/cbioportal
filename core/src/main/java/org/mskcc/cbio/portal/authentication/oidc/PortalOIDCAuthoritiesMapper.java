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

package org.mskcc.cbio.portal.authentication.oidc;

import java.util.ArrayList;

import org.mskcc.cbio.portal.dao.PortalUserDAO;
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

public class PortalOIDCAuthoritiesMapper {

    private static final Logger log = LoggerFactory.getLogger(PortalOIDCAuthoritiesMapper.class);
    
    private final PortalUserDAO portalUserDAO;
    
    /**
     * Constructor.
     *
     * Takes a ref to PortalUserDAO used to authenticate registered users in the
     * database.
     *
     * @param portalUserDAO PortalUserDAO
     */
    public PortalOIDCAuthoritiesMapper(PortalUserDAO portalUserDAO) {
        this.portalUserDAO = portalUserDAO;
    }

	public ArrayList<GrantedAuthority> getPortalAuthorities(String username) {
		
		ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        User user = null;
        
        if (log.isDebugEnabled()) {
            log.debug("getPortalAuthorities(), Looking for username: " + username);
        }
        
        try {
            user = portalUserDAO.getPortalUser(username);
        } catch (Exception e ){
            log.warn("User " +username + " was not found in the cbio users table");
        }
        
        if (user != null && user.isEnabled()) {
            if (log.isDebugEnabled()) {
                log.debug("loadUserByUsername(), attempting to fetch portal user authorities for: " + username);
            }
            UserAuthorities authorities = portalUserDAO.getPortalUserAuthorities(username);
            if (authorities != null) {
            	grantedAuthorities = (ArrayList<GrantedAuthority>) AuthorityUtils.createAuthorityList(
                                authorities.getAuthorities().toArray(new String[authorities.getAuthorities().size()])); 
            }
        }
        
        return grantedAuthorities;
	}

}

