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
import org.cbioportal.persistence.DataAccessTokenRepository;
import org.cbioportal.service.DataAccessTokenService;
import org.cbioportal.service.exception.MaxNumberTokensExceededException;
import org.cbioportal.service.exception.TokenNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Service
@Component("uuid")
public class UuidDataAccessTokenServiceImpl implements DataAccessTokenService {

    @Autowired
    private DataAccessTokenRepository dataAccessTokenRepository;

    @Value("${dat.ttl_seconds:-1}")
    private int datTtlSeconds;

    @Value("${dat.uuid.max_number_per_user:-1}")
    private int maxNumberOfAccessTokens;

    @Value("${dat.uuid.revoke_other_tokens:true}")
    private boolean revokeOtherTokens;

    private static final Log log = LogFactory.getLog(UuidDataAccessTokenServiceImpl.class);

    @Override
    public DataAccessToken createDataAccessToken(String username) {
        return createDataAccessToken(username, revokeOtherTokens);
    }

    // create a data access token (randomly generated UUID) and insert corresponding record into table with parts:
    // username
    // uuid
    // expiration date (current time + 1 month)
    @Override
    public DataAccessToken createDataAccessToken(String username, boolean allowRevocationOfOtherTokens) {
        if (username == null || username.trim().length() == 0) {
            throw new IllegalArgumentException("username cannot be empty");
        }
        if (getNumberOfTokensForUsername(username) >= maxNumberOfAccessTokens) {
            if (allowRevocationOfOtherTokens) {
                revokeOldestDataAccessTokenForUsername(username);
            } else {
                throw new MaxNumberTokensExceededException("User has reached max number of tokens allowed (" + maxNumberOfAccessTokens + "). An existing token must expire or be revoked before another token can be assigned.");
            }
        }
        String uuid = UUID.randomUUID().toString();
        Calendar calendar = Calendar.getInstance();
        Date creationDate = calendar.getTime();
        calendar.add(Calendar.SECOND, datTtlSeconds);
        Date expirationDate = calendar.getTime();

        DataAccessToken dataAccessToken = new DataAccessToken(uuid, username, expirationDate, creationDate);
        dataAccessTokenRepository.addDataAccessToken(dataAccessToken);
        return dataAccessToken;
    }

    // get all user tokens/uuids sorted from oldest to newest
    @Override
    public List<DataAccessToken> getAllDataAccessTokens(String username) {
        List<DataAccessToken> allDataAccessTokens = dataAccessTokenRepository.getAllDataAccessTokensForUsername(username);
        return allDataAccessTokens;
    }

    // get newest data access token for a given username
    @Override
    public DataAccessToken getDataAccessToken(String username) {
        List<DataAccessToken> allDataAccessTokens = dataAccessTokenRepository.getAllDataAccessTokensForUsername(username);
        DataAccessToken newestDataAccessToken = allDataAccessTokens.get(allDataAccessTokens.size() - 1);
        return newestDataAccessToken;
    }

    @Override
    public DataAccessToken getDataAccessTokenInfo(String token) {
        DataAccessToken dataAccessToken = dataAccessTokenRepository.getDataAccessToken(token);
        if (dataAccessToken == null) {
            throw new TokenNotFoundException("Specified token " + token + " does not exist");
        }
        return dataAccessToken;
    }

    @Override
    public void revokeAllDataAccessTokens(String username) {
        dataAccessTokenRepository.removeAllDataAccessTokensForUsername(username);
    }

    @Override
    public void revokeDataAccessToken(String token) {
        DataAccessToken dataAccessToken = dataAccessTokenRepository.getDataAccessToken(token);
        if (dataAccessToken == null) {
            throw new TokenNotFoundException("Specified token " + token + " does not exist");
        }
        dataAccessTokenRepository.removeDataAccessToken(token);
    }

    @Override
    public String getUsername(String token) {
        DataAccessToken dataAccessToken = dataAccessTokenRepository.getDataAccessToken(token);
        return dataAccessToken.getUsername();
    }

    @Override
    public Date getExpiration(String token) {
        DataAccessToken dataAccessToken = dataAccessTokenRepository.getDataAccessToken(token);
        return dataAccessToken.getExpiration();
    }

    @Override
    public Boolean isValid(String dataAccessToken) {
        DataAccessToken storedDataAccessToken = null;
        try {
            storedDataAccessToken = dataAccessTokenRepository.getDataAccessToken(dataAccessToken);
        } catch (Exception e) {
            log.error("Error retrieving data access token, " + dataAccessToken + " from token store");
            return Boolean.FALSE;
        }
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        if (storedDataAccessToken.getExpiration().before(currentDate)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private int getNumberOfTokensForUsername(String username) {
        List<DataAccessToken> allDataAccessTokens = dataAccessTokenRepository.getAllDataAccessTokensForUsername(username);
        return allDataAccessTokens.size();
    }

    // revokes oldest token in token management system for a user
    private void revokeOldestDataAccessTokenForUsername(String username) {
        List<DataAccessToken> allDataAccessTokens = dataAccessTokenRepository.getAllDataAccessTokensForUsername(username);
        DataAccessToken oldestDataAccessToken = allDataAccessTokens.get(0);
        dataAccessTokenRepository.removeDataAccessToken(oldestDataAccessToken.getToken());
    }
}
