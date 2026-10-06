/*
 * Copyright (c) 2026 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.service.impl;

import java.lang.reflect.Field;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Verifies the default values applied to {@link UuidDataAccessTokenServiceImpl} when neither {@code
 * dat.uuid.max_number_per_user} nor {@code dat.ttl_seconds} are explicitly configured. The
 * documented default for {@code dat.uuid.max_number_per_user} is {@code 1}; a default of {@code -1}
 * leaves the service in a state where token creation would attempt to revoke an oldest token even
 * when none exists.
 */
@TestPropertySource(
    properties = {"dat.jwt.secret_key = +NbopXzb/AIQNrVEGzxzP5CF42e5drvrXTQot3gfW/s="},
    inheritLocations = false)
@ContextConfiguration(classes = UuidDataAccessTokenServiceImplTestConfiguration.class)
@RunWith(SpringRunner.class)
public class UuidDataAccessTokenServiceImplDefaultsTest {

  @Autowired
  @Qualifier("uuidDataAccessTokenServiceImpl")
  private UuidDataAccessTokenServiceImpl uuidDataAccessTokenServiceImpl;

  @Test
  public void defaultMaxNumberPerUserMatchesDocumentedValue() throws Exception {
    int actual = readPrivateInt(uuidDataAccessTokenServiceImpl, "maxNumberOfAccessTokens");
    Assert.assertEquals(
        "Default dat.uuid.max_number_per_user should match the documented value of 1.", 1, actual);
  }

  private static int readPrivateInt(Object target, String fieldName) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.getInt(target);
  }
}
