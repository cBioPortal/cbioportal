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
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
//TODO package org.cbioportal.security.spring.authentication.token;

import java.util.*;
import org.apache.commons.logging.*;
import org.cbioportal.service.exception.InvalidDataAccessTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.cbioportal.model.DataAccessToken;
import org.cbioportal.persistence.DataAccessTokenRepository;
import org.cbioportal.service.impl.UuidDataAccessTokenServiceImpl;

@TestPropertySource(
    properties = { "dat.jwt.secret_key = +NbopXzb/AIQNrVEGzxzP5CF42e5drvrXTQot3gfW/s=",
                    "dat.uuid.max_number_per_user = 5",
                    "dat.ttl_seconds = 2"
    },
    inheritLocations = false
)
@ContextConfiguration(classes=UuidDataAccessTokenServiceImplTestConfiguration.class)
@RunWith(SpringRunner.class)
public class UuidDataAccessTokenServiceImplTestFiveTokenLimit {

    @Autowired
    private UuidDataAccessTokenServiceImplTestConfiguration uuidDataAccessTokenServiceImplTestConfiguration;

    @Autowired
    private DataAccessTokenRepository dataAccessTokenRepository;

    @Autowired
    @Qualifier("uuidDataAccessTokenServiceImpl")
    private UuidDataAccessTokenServiceImpl uuidDataAccessTokenServiceImpl;

    @Value("${dat.ttl_seconds}") 
    private int datTtlSeconds;
    
    /* Test for creating a token when autoexpire is on
     * tests that new token is created/MaxNumberTokensExcceededException is not thrown
     * deletedToken should be the oldest token
     */
    @Test
    public void testAutoExpireCreateTokenWhenLimitReached() {
        uuidDataAccessTokenServiceImplTestConfiguration.resetAddedDataAccessToken();
        uuidDataAccessTokenServiceImplTestConfiguration.resetDeletedDataAccessToken();
        // Testing for service with limit 5 tokens
        DataAccessToken newDataAccessToken = uuidDataAccessTokenServiceImpl.createDataAccessToken(UuidDataAccessTokenServiceImplTestConfiguration.MOCK_USERNAME_WITH_FIVE_TOKENS);
        Date expectedExpirationDate = getExpectedExpirationDate();
        String deletedDataAccessToken = uuidDataAccessTokenServiceImplTestConfiguration.getDeletedDataAccessToken();
        DataAccessToken createdDataAccessToken = uuidDataAccessTokenServiceImplTestConfiguration.getAddedDataAccessToken();
        if (createdDataAccessTokenWithWrongInformation(createdDataAccessToken, UuidDataAccessTokenServiceImplTestConfiguration.MOCK_USERNAME_WITH_FIVE_TOKENS, expectedExpirationDate)) {
            Assert.fail("Created token (Username: " + createdDataAccessToken.getUsername() + ", Expiration: " + createdDataAccessToken.getExpiration() + ") differs from expected (Username: " + UuidDataAccessTokenServiceImplTestConfiguration.MOCK_USERNAME_WITH_FIVE_TOKENS + ", Expiration: " + expectedExpirationDate.toString() + ")");
        }
        if (deletedDataAccessToken != uuidDataAccessTokenServiceImplTestConfiguration.OLDEST_TOKEN_UUID) {
            Assert.fail("Expired token: " + deletedDataAccessToken + ", expected to expire token:" + uuidDataAccessTokenServiceImplTestConfiguration.OLDEST_TOKEN_UUID);
        }
    }

    private Date getExpectedExpirationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, datTtlSeconds);
        Date expectedExpirationDate = calendar.getTime();
        return expectedExpirationDate;
    }

    private boolean createdDataAccessTokenWithWrongInformation(DataAccessToken createdDataAccessToken, String expectedUsername, Date expectedExpirationDate) {
        boolean createdDataAccessTokenWithWrongInformation = false;
        if (createdDataAccessToken.getUsername() != expectedUsername) {
            createdDataAccessTokenWithWrongInformation = true;
        }
        if (Math.abs(createdDataAccessToken.getExpiration().getTime() - expectedExpirationDate.getTime()) > UuidDataAccessTokenServiceImplTestConfiguration.MAXIMUM_TIME_DIFFERENCE_BETWEEN_CREATED_AND_EXPECTED_TOKEN) {
            createdDataAccessTokenWithWrongInformation = true;
        }
        return createdDataAccessTokenWithWrongInformation;
    }
}
