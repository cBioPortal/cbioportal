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

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cbioportal.model.DataAccessToken;
import org.cbioportal.service.DataAccessTokenService;
import org.cbioportal.service.exception.InvalidDataAccessTokenException;
import org.cbioportal.service.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtDataAccessTokenServiceImpl implements DataAccessTokenService {

    @Autowired
    private JwtUtils jwtUtils;

    private static final Log log = LogFactory.getLog(JwtDataAccessTokenServiceImpl.class);

    private Set<String> usersWhoRevoked = new HashSet<>();

    @Override
    public String getDataAccessToken(String username) {
        return jwtUtils.createToken(username);
    }

    @Override
    public List<String> getAllDataAccessTokens(String username) {
        List<String> tokenList = new ArrayList<>();
        tokenList.add(getDataAccessToken(username));
        return tokenList;
    }

    @Override
    public void revokeAllDataAccessTokens(String username) {
        usersWhoRevoked.add(username); 
    }

    @Override
    public Boolean isValid(String dataAccessToken) {
        try {
            jwtUtils.validate(dataAccessToken);
        } catch (InvalidDataAccessTokenException idate) {
            log.error("isValid(), " + idate);
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
}
