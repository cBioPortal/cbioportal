/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.persistence;

// imports
import java.util.Set;
import org.cbioportal.legacy.model.User;
import org.cbioportal.legacy.model.UserAuthorities;

/** Interface to use to retrieve portal user information. */
public interface SecurityRepository {

  /**
   * Given a user id, returns a user instance. If username does not exist in db, returns null.
   *
   * @param username String
   * @return User
   */
  User getPortalUser(String username);

  /**
   * Given a user id, returns a UserAuthorities instance. If username does not exist in db, returns
   * null.
   *
   * @param username String
   * @return UserAuthorities
   */
  UserAuthorities getPortalUserAuthorities(String username);

  void addPortalUser(User user);

  void addPortalUserAuthorities(UserAuthorities userAuthorities);

  /**
   * Given an internal cancer study id, returns a set of upper case cancer study group strings.
   * Returns empty set if cancer study does not exist or there are no groups.
   *
   * @param internalCancerStudyId Integer
   * @return Set<String> cancer study group strings in upper case
   */
  Set<String> getCancerStudyGroups(Integer internalCancerStudyId);
}
