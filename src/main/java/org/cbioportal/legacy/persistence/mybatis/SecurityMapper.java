/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.persistence.mybatis;

// imports
import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.User;
import org.cbioportal.legacy.model.UserAuthorities;

/** Interface to use to retrieve portal user information. */
public interface SecurityMapper {

  /**
   * Given a user id, returns a user instance. If username does not exist in db, returns null.
   *
   * @param username String
   * @return User
   */
  User getPortalUser(@Param("username") String username);

  /**
   * Given a user id, returns a UserAuthorities instance. If username does not exist in db, returns
   * null.
   *
   * @param username String
   * @return User
   */
  UserAuthorities getPortalUserAuthorities(@Param("username") String username);

  void addPortalUser(User user);

  void addPortalUserAuthority(@Param("email") String email, @Param("authority") String authority);

  /**
   * Given an internal cancer study id, returns groups string. Returns null if cancer study does not
   * exist.
   *
   * @param internalCancerStudyId Integer
   * @return String groups
   */
  String getCancerStudyGroups(Integer internalCancerStudyId);
}
