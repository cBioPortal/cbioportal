/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.service.impl;

import java.util.Date;
import java.util.List;
import org.cbioportal.legacy.model.DataAccessToken;
import org.cbioportal.legacy.service.DataAccessTokenService;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @author ochoaa
 */
@Component
@ConditionalOnProperty(name = "dat.method", havingValue = "none", matchIfMissing = true)
public class UnauthDataAccessTokenServiceImpl implements DataAccessTokenService {

  @Override
  public DataAccessToken createDataAccessToken(String username) {
    throw new AccessDeniedException(
        "Data Access Tokens are not supported for unauthenticated portals.");
  }

  @Override
  public List<DataAccessToken> getAllDataAccessTokens(String username) {
    throw new AccessDeniedException(
        "Data Access Tokens are not supported for unauthenticated portals.");
  }

  @Override
  public DataAccessToken getDataAccessToken(String username) {
    throw new AccessDeniedException(
        "Data Access Tokens are not supported for unauthenticated portals.");
  }

  @Override
  public DataAccessToken getDataAccessTokenInfo(String token) {
    throw new AccessDeniedException(
        "Data Access Tokens are not supported for unauthenticated portals.");
  }

  @Override
  public void revokeAllDataAccessTokens(String username) {
    throw new AccessDeniedException(
        "Data Access Tokens are not supported for unauthenticated portals.");
  }

  @Override
  public void revokeDataAccessToken(String token) {
    throw new AccessDeniedException(
        "Data Access Tokens are not supported for unauthenticated portals.");
  }

  @Override
  public String getUsername(String token) {
    throw new AccessDeniedException(
        "Data Access Tokens are not supported for unauthenticated portals.");
  }

  @Override
  public Date getExpiration(String token) {
    throw new AccessDeniedException(
        "Data Access Tokens are not supported for unauthenticated portals.");
  }

  @Override
  public Boolean isValid(String token) {
    throw new AccessDeniedException(
        "Data Access Tokens are not supported for unauthenticated portals.");
  }

  @Override
  public Authentication createAuthenticationRequest(String token) {
    return null;
  }
}
