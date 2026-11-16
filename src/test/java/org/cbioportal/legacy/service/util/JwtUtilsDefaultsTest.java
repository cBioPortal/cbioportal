/*
 * Copyright (c) 2026 Memorial Sloan-Kettering Cancer Center.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.service.util;

import org.cbioportal.legacy.model.DataAccessToken;
import org.cbioportal.legacy.service.exception.InvalidDataAccessTokenException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Verifies that {@link JwtUtils} applies the documented default of {@code 2592000} seconds (30
 * days) when {@code dat.ttl_seconds} is not configured. A default of {@code -1} makes every token
 * expire one second before it is created, so it fails validation immediately.
 */
@TestPropertySource(
    properties = {"dat.jwt.secret_key = +NbopXzb/AIQNrVEGzxzP5CF42e5drvrXTQot3gfW/s="},
    inheritLocations = false)
@ContextConfiguration(classes = JwtUtilsTestConfiguration.class)
@RunWith(SpringRunner.class)
public class JwtUtilsDefaultsTest {

  private static final long DOCUMENTED_DEFAULT_TTL_MILLISECONDS = 2592000L * 1000L;

  @Autowired private JwtUtils jwtUtils;

  @Test
  public void defaultTtlMatchesDocumentedValue() {
    DataAccessToken token = jwtUtils.createToken("testSubject");
    Assert.assertEquals(
        "Default dat.ttl_seconds should match the documented value of 2592000 (30 days).",
        DOCUMENTED_DEFAULT_TTL_MILLISECONDS,
        token.getExpiration().getTime() - token.getCreation().getTime());
  }

  @Test
  public void tokenCreatedWithDefaultTtlIsValid() throws InvalidDataAccessTokenException {
    String token = jwtUtils.createToken("testSubject").getToken();
    jwtUtils.validate(token); // throws if the token is already expired
  }
}
