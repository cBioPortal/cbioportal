// package
package org.mskcc.cgds.test.dao;

// imports
import org.mskcc.cgds.model.User;
import org.mskcc.cgds.model.UserAuthorities;
import org.mskcc.cgds.dao.DaoUserAuthorities;
import org.mskcc.cgds.scripts.ResetDatabase;

import junit.framework.TestCase;
import java.util.ArrayList;
import java.util.Arrays;

public class TestDaoUserAuthorities extends TestCase {

   public void testDaoUserAuthorities() throws Exception {

      ResetDatabase.resetDatabase();

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