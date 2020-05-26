/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.dao;

// imports
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * JUnit test for DaoUserAuthorities class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestDaoUserAuthorities {

   @Test
   public void testDaoUserAuthorities() throws Exception {

      User userJoe = new User("joe@goggle.com", "Joe User", true);
      User userJane = new User("jane@hotmail.com", "Jane User", true);
      UserAuthorities authorities = DaoUserAuthorities.getUserAuthorities(userJoe);
      assertEquals(authorities.getAuthorities().size(), 0);

      authorities = new UserAuthorities(userJane.getEmail(), Arrays.asList("ROLE_USER"));
      DaoUserAuthorities.addUserAuthorities(authorities);

      authorities = new UserAuthorities(userJoe.getEmail(), Arrays.asList("ROLE_MANAGER", "ROLE_USER"));
      DaoUserAuthorities.addUserAuthorities(authorities);

      assertTrue(DaoUserAuthorities.getUserAuthorities(userJoe).getAuthorities().contains("ROLE_MANAGER"));
      assertFalse(DaoUserAuthorities.getUserAuthorities(userJane).getAuthorities().contains("ROLE_MANAGER"));

      try {
          DaoUserAuthorities.removeUserAuthorities(userJane);
          assertFalse(DaoUserAuthorities.getUserAuthorities(userJane).getAuthorities().contains("ROLE_USER"));
      } catch (Exception e) {
         fail("Should not throw Exception " + e.getMessage());
      }

      assertTrue(DaoUserAuthorities.getUserAuthorities(userJoe) != null);
      DaoUserAuthorities.deleteAllRecords();
      assertEquals(DaoUserAuthorities.getUserAuthorities(userJoe).getAuthorities().size(), 0);
   }
}
