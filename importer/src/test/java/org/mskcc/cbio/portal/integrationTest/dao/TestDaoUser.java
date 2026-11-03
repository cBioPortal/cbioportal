/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.dao;

import java.util.*;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoUser;
import org.mskcc.cbio.portal.model.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * JUnit test for DaoUser class.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestDaoUser extends IntegrationTestBase {
	
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
