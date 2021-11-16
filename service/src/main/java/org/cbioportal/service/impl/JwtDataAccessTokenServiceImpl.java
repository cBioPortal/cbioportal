/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cbioportal.model.DataAccessToken;
import org.cbioportal.service.DataAccessTokenService;
import org.cbioportal.service.exception.InvalidDataAccessTokenException;
import org.cbioportal.service.util.JwtUtils;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "dat.method", havingValue = "jwt")
public class JwtDataAccessTokenServiceImpl implements DataAccessTokenService {

    @Autowired
    private JwtUtils jwtUtils;

    private static final Logger LOG = LoggerFactory.getLogger(JwtDataAccessTokenServiceImpl.class);

    //TODO : we could add a persistence layer to store pairs of <username, revokeDate> ... then a user can revoke all thier tokens before a particular date and we would only need to store the most recent revoke date for that user.  But it would have to be persisted, or else a restart of the server would lose the memory of revocation

    @Override
    public DataAccessToken createDataAccessToken(String username) {
        return jwtUtils.createToken(username);
    }

    @Override
    public List<DataAccessToken> getAllDataAccessTokens(String username) {
        throw new UnsupportedOperationException("this implementation of (pure) JWT Data Access Tokens does not allow retrieval of stored tokens");
    }

    @Override
    public DataAccessToken getDataAccessToken(String username) {
        throw new UnsupportedOperationException("this implementation of (pure) JWT Data Access Tokens does not allow retrieval of stored tokens");
    }

    @Override
    public DataAccessToken getDataAccessTokenInfo(String token) {
        throw new UnsupportedOperationException("this implementation of (pure) JWT Data Access Tokens does not allow this operation");
    }

    @Override
    public void revokeAllDataAccessTokens(String username) {
        throw new UnsupportedOperationException("this implementation of (pure) JWT Data Access Tokens does not allow revocation of tokens");
    }

    @Override
    public void revokeDataAccessToken(String token) {
        throw new UnsupportedOperationException("this implementation of (pure) JWT Data Access Tokens does not allow revocation of tokens");
    }

    @Override
    public Boolean isValid(String token) {
        try {
            jwtUtils.validate(token);
        } catch (InvalidDataAccessTokenException idate) {
            LOG.error("isValid(), " + idate);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public String getUsername(String token) {
        try {
            return jwtUtils.extractSubject(token);
        } catch (InvalidDataAccessTokenException idate) {
            // TODO is this what we want to do?
            return null;
        }
    }

    @Override
    public Date getExpiration(String token) {
        try {
            return jwtUtils.extractExpirationDate(token);
        } catch (InvalidDataAccessTokenException idate) {
            return null;
        }
    }

    @Override
    public Authentication createAuthenticationRequest(String token) {

        if (!isValid(token)) {
            LOG.error("invalid token = " + token);
            throw new BadCredentialsException("Invalid access token");
        }
        String userName = getUsername(token);

        // When DaoAuthenticationProvider does authentication on user returned by PortalUserDetailsService
        // which has password "unused", this password won't match, and then there is a BadCredentials exception thrown
        // this is a good way to catch that the wrong authetication provider is being used.
        return new UsernamePasswordAuthenticationToken(userName, "does not match unused");

    }

}
