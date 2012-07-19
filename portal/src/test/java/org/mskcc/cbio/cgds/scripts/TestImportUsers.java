// package
package org.mskcc.cbio.cgds.test.scripts;

// imports
import org.mskcc.cgds.model.User;
import org.mskcc.cgds.dao.DaoUser;
import org.mskcc.cgds.model.UserAuthorities;
import org.mskcc.cgds.dao.DaoUserAuthorities;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.scripts.ImportUsers;

import junit.framework.TestCase;

/**
 * JUnit test for ImportUsers class.
 */
public class TestImportUsers extends TestCase {
   
   public void testImportUsers() throws Exception{

      ResetDatabase.resetDatabase();
      
      String args[] = {"./test_data/test-users.txt"};
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
