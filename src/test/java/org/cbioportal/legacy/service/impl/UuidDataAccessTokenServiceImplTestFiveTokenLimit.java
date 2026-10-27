/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
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

package org.cbioportal.legacy.service.impl;

// TODO package org.cbioportal.security.spring.authentication.token;

import java.util.*;
import org.cbioportal.legacy.model.DataAccessToken;
import org.cbioportal.legacy.persistence.DataAccessTokenRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@TestPropertySource(
    properties = {
      "dat.jwt.secret_key = +NbopXzb/AIQNrVEGzxzP5CF42e5drvrXTQot3gfW/s=",
      "dat.uuid.max_number_per_user = 5",
      "dat.ttl_seconds = 2"
    },
    inheritLocations = false)
@ContextConfiguration(classes = UuidDataAccessTokenServiceImplTestConfiguration.class)
@RunWith(SpringRunner.class)
public class UuidDataAccessTokenServiceImplTestFiveTokenLimit {

  @Autowired
  private UuidDataAccessTokenServiceImplTestConfiguration
      uuidDataAccessTokenServiceImplTestConfiguration;

  @Autowired private DataAccessTokenRepository dataAccessTokenRepository;

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
    DataAccessToken newDataAccessToken =
        uuidDataAccessTokenServiceImpl.createDataAccessToken(
            UuidDataAccessTokenServiceImplTestConfiguration.MOCK_USERNAME_WITH_FIVE_TOKENS);
    Date expectedExpirationDate = getExpectedExpirationDate();
    String deletedDataAccessToken =
        uuidDataAccessTokenServiceImplTestConfiguration.getDeletedDataAccessToken();
    DataAccessToken createdDataAccessToken =
        uuidDataAccessTokenServiceImplTestConfiguration.getAddedDataAccessToken();
    if (createdDataAccessTokenWithWrongInformation(
        createdDataAccessToken,
        UuidDataAccessTokenServiceImplTestConfiguration.MOCK_USERNAME_WITH_FIVE_TOKENS,
        expectedExpirationDate)) {
      Assert.fail(
          "Created token (Username: "
              + createdDataAccessToken.getUsername()
              + ", Expiration: "
              + createdDataAccessToken.getExpiration()
              + ") differs from expected (Username: "
              + UuidDataAccessTokenServiceImplTestConfiguration.MOCK_USERNAME_WITH_FIVE_TOKENS
              + ", Expiration: "
              + expectedExpirationDate.toString()
              + ")");
    }
    if (deletedDataAccessToken
        != uuidDataAccessTokenServiceImplTestConfiguration.OLDEST_TOKEN_UUID) {
      Assert.fail(
          "Expired token: "
              + deletedDataAccessToken
              + ", expected to expire token:"
              + uuidDataAccessTokenServiceImplTestConfiguration.OLDEST_TOKEN_UUID);
    }
  }

  private Date getExpectedExpirationDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, datTtlSeconds);
    Date expectedExpirationDate = calendar.getTime();
    return expectedExpirationDate;
  }

  private boolean createdDataAccessTokenWithWrongInformation(
      DataAccessToken createdDataAccessToken,
      String expectedUsername,
      Date expectedExpirationDate) {
    boolean createdDataAccessTokenWithWrongInformation = false;
    if (createdDataAccessToken.getUsername() != expectedUsername) {
      createdDataAccessTokenWithWrongInformation = true;
    }
    if (Math.abs(
            createdDataAccessToken.getExpiration().getTime() - expectedExpirationDate.getTime())
        > UuidDataAccessTokenServiceImplTestConfiguration
            .MAXIMUM_TIME_DIFFERENCE_BETWEEN_CREATED_AND_EXPECTED_TOKEN) {
      createdDataAccessTokenWithWrongInformation = true;
    }
    return createdDataAccessTokenWithWrongInformation;
  }
}
