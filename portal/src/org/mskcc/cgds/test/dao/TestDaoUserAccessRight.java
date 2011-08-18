package org.mskcc.cgds.test.dao;

import java.util.ArrayList;

import org.mskcc.cgds.dao.DaoUserAccessRight;
import org.mskcc.cgds.model.UserAccessRight;
import org.mskcc.cgds.scripts.ResetDatabase;

import junit.framework.TestCase;

/**
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class TestDaoUserAccessRight extends TestCase {

   public void testDaoUserAccessRight() throws Exception {
       /* TBD: Recoded with new db schema
      ResetDatabase.resetDatabase();

      assertFalse(DaoUserAccessRight.containsUserAccessRightsByEmailAndStudy("joe@hotmail.com", 1));

      UserAccessRight userAccessRight = new UserAccessRight( "artg@gmail.com", 1);
      DaoUserAccessRight.addUserAccessRight(userAccessRight);

      userAccessRight = new UserAccessRight( "joe@hotmail.com", 2);
      DaoUserAccessRight.addUserAccessRight(userAccessRight);

      assertTrue(DaoUserAccessRight.containsUserAccessRightsByEmailAndStudy("joe@hotmail.com", 2));
      assertFalse(DaoUserAccessRight.containsUserAccessRightsByEmailAndStudy("joe@hotmail.com", 1));
      assertFalse(DaoUserAccessRight.containsUserAccessRightsByEmailAndStudy("NOTjoe@hotmail.com", 2));

      ArrayList<UserAccessRight> allUserAccessRights = DaoUserAccessRight.getAllUserAccessRights();
      assertEquals(userAccessRight, allUserAccessRights.get(1));

      try {
         DaoUserAccessRight.deleteUserAccessRight("joe@hotmail.com", 2);
         assertFalse(DaoUserAccessRight.containsUserAccessRightsByEmailAndStudy("joe@hotmail.com", 2));
      } catch (Exception e) {
         fail("Should not throw Exception " + e.getMessage());
      }

      assertTrue(DaoUserAccessRight.containsUserAccessRightsByEmailAndStudy("artg@gmail.com", 1));
      DaoUserAccessRight.deleteAllRecords();
      assertFalse(DaoUserAccessRight.containsUserAccessRightsByEmailAndStudy("artg@gmail.com", 1));
       */
   }

}