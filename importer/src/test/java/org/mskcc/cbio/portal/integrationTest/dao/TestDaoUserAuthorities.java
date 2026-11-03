/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.dao;

import java.util.*;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoUserAuthorities;
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * JUnit test for DaoUserAuthorities class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestDaoUserAuthorities extends IntegrationTestBase {

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
