/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.persistence.mybatis;

// imports
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.cbioportal.legacy.model.User;
import org.cbioportal.legacy.model.UserAuthorities;
import org.cbioportal.legacy.persistence.SecurityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SecurityMyBatisRepository implements SecurityRepository {

  private static final Logger log = LoggerFactory.getLogger(SecurityMyBatisRepository.class);

  @Autowired private SecurityMapper securityMapper;

  /**
   * Given a user id, returns a user instance. If username does not exist in db, returns null.
   *
   * @param username String
   * @return User
   */
  @Override
  public User getPortalUser(String username) {
    User user = securityMapper.getPortalUser(username);
    if (user != null) {
      log.debug("User " + username + " was found in the users table, email is " + user.getEmail());
    } else {
      log.debug("User " + username + " is null");
    }
    return user;
  }

  /**
   * Given a user id, returns a UserAuthorities instance. If username does not exist in db, returns
   * null.
   *
   * @param username String
   * @return UserAuthorities
   */
  @Override
  public UserAuthorities getPortalUserAuthorities(String username) {
    return securityMapper.getPortalUserAuthorities(username);
  }

  @Override
  public void addPortalUser(User user) {
    securityMapper.addPortalUser(user);
  }

  @Override
  public void addPortalUserAuthorities(UserAuthorities userAuthorities) {
    for (String authority : userAuthorities.getAuthorities()) {
      securityMapper.addPortalUserAuthority(userAuthorities.getEmail(), authority);
    }
  }

  /**
   * Given an internal cancer study id, returns a set of upper case cancer study group strings.
   * Returns empty set if cancer study does not exist or there are no groups.
   *
   * @param internalCancerStudyId Integer
   * @return Set<String> cancer study group strings in upper case
   */
  @Override
  public Set<String> getCancerStudyGroups(Integer internalCancerStudyId) {
    String groups = securityMapper.getCancerStudyGroups(internalCancerStudyId);
    if (groups == null) {
      return Collections.emptySet();
    }
    return new HashSet<String>(Arrays.asList(groups.toUpperCase().split(";")));
  }
}
