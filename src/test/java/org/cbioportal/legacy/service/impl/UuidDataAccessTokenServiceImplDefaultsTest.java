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
