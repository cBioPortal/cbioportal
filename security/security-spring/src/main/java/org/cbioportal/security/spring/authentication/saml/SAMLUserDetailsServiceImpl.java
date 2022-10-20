/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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

package org.cbioportal.security.spring.authentication.saml;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.cbioportal.model.User;
import org.cbioportal.model.UserAuthorities;
import org.cbioportal.persistence.SecurityRepository;
import org.cbioportal.security.spring.authentication.PortalUserDetails;

import org.opensaml.saml2.core.Attribute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService which parses SAML messages and checks authorization of
 * user against cbioportal's `authorities` configuration. Authentication is done by the SAML IDP.
 *
 * @author Pieter Lukasse
 */
@Service
public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService
{
    private static final Log log = LogFactory.getLog(SAMLUserDetailsServiceImpl.class);

    private static String samlIdpMetadataEmailAttributeName;
    @Value("${saml.idp.metadata.attribute.email:mail}")
    public void setSamlIdpMetadataEmailAttributeName(String property) { this.samlIdpMetadataEmailAttributeName = property; }

    private static String samlIdpMetadataUserNameAttributeName;
    @Value("${saml.idp.metadata.attribute.userName:username}")
    public void setsamlIdpMetadataUserNameAttributeName(String property) { this.samlIdpMetadataUserNameAttributeName = property; }

    @Autowired
    private SecurityRepository securityRepository;

    /**
     * Constructor.
     */
    public SAMLUserDetailsServiceImpl() {
    }          

    /**
     * Implementation of {@code SAMLUserDetailsService}. Parses user details from given 
     * SAML credential object.
     */
    @Override
    public Object loadUserBySAML(SAMLCredential credential)
    {
        PortalUserDetails toReturn = null;

        String email = null;
        String userName = null;
        // get userid and name: iterate over attributes searching for "mail" and "displayName":
        for (Attribute cAttribute : credential.getAttributes()) {
            String attrName = cAttribute.getName();
            log.debug("loadUserBySAML(), parsing attribute: " + cAttribute.getName() + "=" + credential.getAttributeAsString(attrName));
            if (email == null && attrName.equals(samlIdpMetadataEmailAttributeName))
            {
                email = credential.getAttributeAsString(attrName);
            }
            if (userName == null && attrName.equals(samlIdpMetadataUserNameAttributeName))
            {
                userName = credential.getAttributeAsString(attrName);
            }
        }

        //check if this user exists in our DB
        try {
            //validate parsing:
            if (email == null) {
                String errorMessage = "loadUserBySAML(), Could not parse the user details from credential message. Expected '" + samlIdpMetadataEmailAttributeName + "' attribute, but attribute was not found. "
                        + " Previous debug messages show which attributes were found and parsed.";
                log.error(errorMessage);
                throw new Exception(errorMessage);
            }
            
            log.debug("loadUserBySAML(), IDP successfully authenticated user, username: " + userName +", email: " + email);
            log.debug("loadUserBySAML(), now attempting to fetch portal user authorities for user with username: " + userName +", email: " + email);
            
            //try to find user in DB
            User user = securityRepository.getPortalUser(email);
            if (user != null && user.isEnabled()) {
                log.debug("loadUserBySAML(), user is enabled; attempting to fetch portal user authorities, for user with username: " + userName +", email: " + email);

                UserAuthorities authorities = securityRepository.getPortalUserAuthorities(email);
                if (authorities != null) {
                    List<GrantedAuthority> grantedAuthorities =
                        AuthorityUtils.createAuthorityList(authorities.getAuthorities().toArray(new String[authorities.getAuthorities().size()]));
                    //add granted authorities:
                    toReturn = new PortalUserDetails(email, grantedAuthorities);
                    toReturn.setEmail(email);
                    toReturn.setUserName(userName);
                } 
          } else if (user == null) { // new user
                log.debug("loadUserBySAML(), user authorities is null, for user, with username: " + userName +", email: " + email + ". Depending on property always_show_study_group, "
                    + "he could still have default access (to PUBLIC studies)");
                toReturn = new PortalUserDetails(email, getInitialEmptyAuthoritiesList());
                toReturn.setEmail(email);
                toReturn.setUserName(userName);
            } else {
                //user WAS found in DB but has been actively disabled:
                throw new UsernameNotFoundException("Error: Your user access to cBioPortal has been disabled");
            }
            return toReturn;
        }
        catch (UsernameNotFoundException unnf) {
            //throw this exception, so that the user gets redirected to the error HTML page: 
            throw unnf;
        }
        catch (Exception e) {
            //other (unexpected) errors: just throw (will result in http 500 page with error message):
            log.error(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error during authentication parsing: " + e.getMessage());
        }
    }

    /**
     * Returns an initial empty authorities list.
     * 
     * @return
     */
    private List<GrantedAuthority> getInitialEmptyAuthoritiesList()
    {
        return AuthorityUtils.createAuthorityList(new String[0]);
    }
}

