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

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoUser;
import org.mskcc.cbio.portal.model.User;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * JUnit test for DaoUser class.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestDaoUser {
	
	@Test
	public void testDaoSeededUsers() throws Exception {
	    ArrayList<User> allUsers = DaoUser.getAllUsers();
	    assertEquals(3, allUsers.size());
	}

	@Test
	public void testDaoFindUser() throws Exception {
		
		User user = DaoUser.getUserByEmail("Lonnie@openid.org");
	    assertThat(user.getEmail(), is("Lonnie@openid.org"));
	}
	
	
	@Test
	public void testDaoAddUser() throws Exception {

		User user = new User("joe@mail.com", "Joe Smith", false);
		DaoUser.addUser(user);

		assertEquals(null, DaoUser.getUserByEmail("foo"));
		assertEquals(user, DaoUser.getUserByEmail("joe@mail.com"));
		assertFalse(user.isEnabled());
		
	    ArrayList<User> allUsers = DaoUser.getAllUsers();
	    assertEquals(4, allUsers.size());
	    
	    User foundUser = DaoUser.getUserByEmail("joe@mail.com");
	    assertThat(foundUser, is(notNullValue()));
	    assertThat(user.getEmail(), is("joe@mail.com"));
	}
	
	@Test
	public void testDaoRemoveUser() throws Exception {
		
		DaoUser.deleteUser("Lonnie@openid.org");
		
	    ArrayList<User> allUsers = DaoUser.getAllUsers();
	    assertEquals(2, allUsers.size());
	    
	    for(User user : allUsers) {
	    	assertThat(user.getEmail(), is(not("Lonnie@openid.org")));
	    }
	}

}
