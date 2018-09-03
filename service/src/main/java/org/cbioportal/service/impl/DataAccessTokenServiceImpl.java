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
import org.cbioportal.model.DataAccessToken;
//import org.cbioportal.persistence.DataAccessTokenRepository; // Maybe this is a different package? we do not plan to persist in database .. so need to configure another bean maybe
import org.cbioportal.service.DataAccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataAccessTokenServiceImpl implements DataAccessTokenService {

//    @Autowired
//    private DataAccessTokenRepository dataAccessTokenRepository;

    public final String VALID_DATA_ACCESS_TOKEN_USER1 = "d12b5d23-72ce-4c9d-a0d3-04297b7b8811";
    public final String VALID_DATA_ACCESS_TOKEN_USER2 = "8ff83cb6-d691-4c24-b974-18f627c2c979";
    public final String VALID_DATA_ACCESS_TOKEN_NEW = "6716ef71-fbbc-4a6a-b1c7-c02b7cb67e48";
    public final String UNKNOWN_DATA_ACCESS_TOKEN = "7d2c2fa7-f949-4a38-b8dc-9ab2a6b1a488";
    public final String EXPIRED_DATA_ACCESS_TOKEN = "ad70d755-41d1-42c5-a9d0-28961aa72326";
    public final String REVOKED_DATA_ACCESS_TOKEN = "9cbdd669-fc9a-458d-8202-e8641db06b71";

    private Set<String> usersWhoRevoked = new HashSet<>();


    @Override
    public String getDataAccessToken(String username) {
    // old args: (String username, Boolean allowNewlyGeneratedToken, Boolean allowRevocationOfOtherTokens, Long minimumTimeBeforeExpiration) {
        if ("user1".equals(username)) {
            return VALID_DATA_ACCESS_TOKEN_USER1;
        }
        if ("user2".equals(username)) {
            return VALID_DATA_ACCESS_TOKEN_USER2;
        }
        // no need to check if user is "valid" ... a valid session means a valid user ... some users are not in the users table
        return VALID_DATA_ACCESS_TOKEN_NEW;
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
        if (!isAuthenticAndUnrevoked(dataAccessToken)) {
            return Boolean.FALSE;
        }
        if (hasExpired(dataAccessToken)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private boolean isAuthenticAndUnrevoked(String dataAccessToken) {
        if (UNKNOWN_DATA_ACCESS_TOKEN.equals(dataAccessToken)) {
            return false;
        }
        if (REVOKED_DATA_ACCESS_TOKEN.equals(dataAccessToken)) {
            return false;
        }
        if (VALID_DATA_ACCESS_TOKEN_USER1.equals(dataAccessToken)) {
            return !usersWhoRevoked.contains("user1");
        }
        if (VALID_DATA_ACCESS_TOKEN_USER2.equals(dataAccessToken)) {
            return !usersWhoRevoked.contains("user2");
        }
        if (REVOKED_DATA_ACCESS_TOKEN.equals(dataAccessToken)) {
            Set<String> otherUsers = new HashSet<>(usersWhoRevoked);
            otherUsers.remove("user1");
            otherUsers.remove("user2");
            return otherUsers.isEmpty();
        }
        return true; // replace with a call to the repository layer (and maybe a crytographic check)
    }

    private boolean hasExpired(String dataAccessToken) {
        if (EXPIRED_DATA_ACCESS_TOKEN.equals(dataAccessToken)) {
            return true;
        }
        return false;
        //Date now = new Date();
        //return now.before(dataAccessToken.getExpiresAt());
    }

}
