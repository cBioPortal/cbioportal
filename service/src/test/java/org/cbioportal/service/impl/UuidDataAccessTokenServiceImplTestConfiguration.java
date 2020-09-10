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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.annotation.Bean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.cbioportal.model.DataAccessToken;
import org.cbioportal.persistence.DataAccessTokenRepository;
import org.cbioportal.service.exception.TokenNotFoundException;
import org.cbioportal.service.impl.UuidDataAccessTokenServiceImpl;

@Configuration
public class UuidDataAccessTokenServiceImplTestConfiguration {

    public static String MOCK_USERNAME = "MOCK_USER";
    public static String MOCK_USERNAME_WITH_ONE_TOKEN = "MOCK_USER_WITH_ONE_TOKEN";
    public static String MOCK_USERNAME_WITH_FIVE_TOKENS = "MOCK_USER_WITH_FIVE_TOKENS";
    public static String NONEXISTENT_TOKEN_STRING = "NONEXISTENT_TOKEN_STRING";
    public static String FAIL_TO_GET_TOKEN_STRING = "FAIL_TO_GET_TOKEN_STRING";
    public static String EXPIRED_TOKEN_STRING = "EXPIRED_TOKEN_STRING";
    public static String VALID_TOKEN_STRING = "VALID_TOKEN_STRING";
    public static String OLDEST_TOKEN_UUID = "OLDEST_TOKEN_UUID";
    public static String NEWEST_TOKEN_UUID = "NEWEST_TOKEN_UUID";

    public static int MAXIMUM_TIME_DIFFERENCE_BETWEEN_CREATED_AND_EXPECTED_TOKEN = 5000;

    private List<DataAccessToken> dataAccessTokenListForMockUserWithOneToken = makeDataAccessTokenListForMockUserWithOneToken();
    private List<DataAccessToken> dataAccessTokenListForMockUserWithFiveTokens = makeDataAccessTokenListForMockUserWithFiveTokens();

    DataAccessToken addedDataAccessToken = null;
    String deletedDataAccessToken = null;

    @Bean
    public UuidDataAccessTokenServiceImpl uuidDataAccessTokenServiceImpl() {
        return new UuidDataAccessTokenServiceImpl();
    }

    @Bean
    public DataAccessTokenRepository dataAccessTokenRepository() {
        Answer<Void> dataAccessTokenRepositoryCreateTokenAnswer = new Answer<Void>() {
            public Void answer(InvocationOnMock addTokenInvocation) {
                addedDataAccessToken = (DataAccessToken)addTokenInvocation.getArguments()[0];
                return null;
            }
        };
        Answer<Void> dataAccessTokenRepositoryDeleteTokenAnswer = new Answer<Void>() {
            public Void answer(InvocationOnMock deleteTokenInvocation) {
                deletedDataAccessToken = (String)deleteTokenInvocation.getArguments()[0];
                return null;
            }
        };
        DataAccessTokenRepository dataAccessTokenRepository = Mockito.mock(DataAccessTokenRepository.class);
        Mockito.when(dataAccessTokenRepository.getDataAccessToken(FAIL_TO_GET_TOKEN_STRING)).thenThrow(new RuntimeException("Fail to get this token"));
        Mockito.when(dataAccessTokenRepository.getDataAccessToken(NONEXISTENT_TOKEN_STRING)).thenReturn(null);
        Mockito.when(dataAccessTokenRepository.getDataAccessToken(EXPIRED_TOKEN_STRING)).thenReturn(makeExpiredDataAccessToken());
        Mockito.when(dataAccessTokenRepository.getDataAccessToken(VALID_TOKEN_STRING)).thenReturn(makeValidDataAccessToken());
        Mockito.when(dataAccessTokenRepository.getAllDataAccessTokensForUsername(MOCK_USERNAME_WITH_ONE_TOKEN)).thenReturn(dataAccessTokenListForMockUserWithOneToken);
        Mockito.when(dataAccessTokenRepository.getAllDataAccessTokensForUsername(MOCK_USERNAME_WITH_FIVE_TOKENS)).thenReturn(sortByExpiration(dataAccessTokenListForMockUserWithFiveTokens));
        Mockito.doAnswer(dataAccessTokenRepositoryDeleteTokenAnswer).when(dataAccessTokenRepository).removeDataAccessToken(ArgumentMatchers.anyString());
        Mockito.doAnswer(dataAccessTokenRepositoryCreateTokenAnswer).when(dataAccessTokenRepository).addDataAccessToken(ArgumentMatchers.any(DataAccessToken.class));
        return dataAccessTokenRepository;
    }

    private DataAccessToken makeDataAccessToken(String username, int offsetSeconds) {
        String uuid = UUID.randomUUID().toString();
        DataAccessToken dataAccessToken = makeDataAccessToken(uuid, username, offsetSeconds);
        return dataAccessToken;
    }

    private DataAccessToken makeDataAccessToken(String uuid, String username, int offsetSeconds) {
        Calendar calendar = Calendar.getInstance();
        Date creationDate = calendar.getTime();
        calendar.add(Calendar.SECOND, offsetSeconds);
        Date expirationDate = calendar.getTime();
        DataAccessToken dataAccessToken = new DataAccessToken(uuid, username, expirationDate, creationDate);
        return dataAccessToken;
    }

    private DataAccessToken makeExpiredDataAccessToken() {
        return makeDataAccessToken(MOCK_USERNAME, -100000);
    }

    private DataAccessToken makeValidDataAccessToken() {
        return makeDataAccessToken(MOCK_USERNAME, 100000);
    }

    private List<DataAccessToken> makeDataAccessTokenListForMockUserWithOneToken() {
        List<DataAccessToken> dataAccessTokenListForMockUserWithOneToken = new ArrayList<DataAccessToken>();
        dataAccessTokenListForMockUserWithOneToken.add(makeDataAccessToken(OLDEST_TOKEN_UUID, MOCK_USERNAME_WITH_ONE_TOKEN, -150000));
        return dataAccessTokenListForMockUserWithOneToken;
    }

    private List<DataAccessToken> makeDataAccessTokenListForMockUserWithFiveTokens() {
        List<DataAccessToken> dataAccessTokenListForMockUserWithFiveTokens = new ArrayList<DataAccessToken>();
        dataAccessTokenListForMockUserWithFiveTokens.add(makeDataAccessToken(MOCK_USERNAME_WITH_FIVE_TOKENS, 86400));
        dataAccessTokenListForMockUserWithFiveTokens.add(makeDataAccessToken(MOCK_USERNAME_WITH_FIVE_TOKENS, 172800));
        dataAccessTokenListForMockUserWithFiveTokens.add(makeDataAccessToken(MOCK_USERNAME_WITH_FIVE_TOKENS, 529200));
        dataAccessTokenListForMockUserWithFiveTokens.add(makeDataAccessToken(OLDEST_TOKEN_UUID, MOCK_USERNAME_WITH_FIVE_TOKENS, 45000));
        dataAccessTokenListForMockUserWithFiveTokens.add(makeDataAccessToken(NEWEST_TOKEN_UUID, MOCK_USERNAME_WITH_FIVE_TOKENS, 1000000));
        return dataAccessTokenListForMockUserWithFiveTokens;
    }

    private List<DataAccessToken> sortByExpiration(List<DataAccessToken> dataAccessTokenList) {
        dataAccessTokenList.sort(Comparator.comparing(DataAccessToken::getExpiration));
        return dataAccessTokenList;
    }

    public DataAccessToken getAddedDataAccessToken() {
        return addedDataAccessToken;
    }

    public void resetAddedDataAccessToken() {
        addedDataAccessToken = null;
    }

    public String getDeletedDataAccessToken() {
        return deletedDataAccessToken;
    }

    public void resetDeletedDataAccessToken() {
        deletedDataAccessToken = null;
    }
}
