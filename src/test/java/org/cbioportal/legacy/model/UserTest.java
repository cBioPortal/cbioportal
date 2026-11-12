/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class UserTest {

  @Test
  public void usersWithEqualButDistinctStringsAreEqual() {
    // new String(...) gives separate instances, as values read from the database would be.
    User a = new User(new String("jane@example.org"), new String("Jane Doe"), true);
    User b = new User(new String("jane@example.org"), new String("Jane Doe"), true);

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void usersWithDifferentEmailOrNameAreNotEqual() {
    User a = new User("jane@example.org", "Jane Doe", true);

    assertNotEquals(a, new User("john@example.org", "Jane Doe", true));
    assertNotEquals(a, new User("jane@example.org", "John Doe", true));
  }
}
