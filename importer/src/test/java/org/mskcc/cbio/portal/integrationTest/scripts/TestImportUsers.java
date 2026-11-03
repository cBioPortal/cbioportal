/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.scripts;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoUser;
import org.mskcc.cbio.portal.dao.DaoUserAuthorities;
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.mskcc.cbio.portal.scripts.ImportUsers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * JUnit test for ImportUsers class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestImportUsers extends IntegrationTestBase {

   @Test
   public void testImportUsers() throws Exception{

      // TBD: change this to use getResourceAsStream()
      String args[] = {"src/test/resources/test-users.txt"};
      ImportUsers.main(args);

      User user = DaoUser.getUserByEmail("Dhorak@yahoo.com");
      assertTrue(user != null);
      assertTrue(user.isEnabled());
      UserAuthorities authorities = DaoUserAuthorities.getUserAuthorities(user);
      assertTrue(authorities.getAuthorities().contains("ROLE_MANAGER"));

      user = DaoUser.getUserByEmail("Lonnie@openid.org");
      assertTrue(user != null);
      assertFalse(user.isEnabled());
      authorities = DaoUserAuthorities.getUserAuthorities(user);
      assertEquals(authorities.getAuthorities().size(), 1);
      DaoUserAuthorities.removeUserAuthorities(user);
      assertEquals(DaoUserAuthorities.getUserAuthorities(user).getAuthorities().size(), 0);
   }
}
