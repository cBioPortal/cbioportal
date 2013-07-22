/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

// package
package org.mskcc.cbio.cgds.dao;

// imports
import org.mskcc.cbio.cgds.model.User;
import org.mskcc.cbio.cgds.model.UserAuthorities;
import org.mskcc.cbio.cgds.dao.DaoUserAuthorities;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;

import junit.framework.TestCase;
import java.util.Arrays;

/**
 * JUnit test for DaoUserAuthorities class.
 */
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