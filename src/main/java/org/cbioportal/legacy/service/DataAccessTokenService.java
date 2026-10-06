/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.service;

import java.util.Date;
import java.util.List;
import org.cbioportal.legacy.model.DataAccessToken;
import org.springframework.security.core.Authentication;

public interface DataAccessTokenService {

  public DataAccessToken createDataAccessToken(String username);

  public List<DataAccessToken> getAllDataAccessTokens(String username);

  public DataAccessToken getDataAccessToken(String username);

  public DataAccessToken getDataAccessTokenInfo(String token);

  public void revokeAllDataAccessTokens(String username);

  public void revokeDataAccessToken(String token);

  public String getUsername(String token);

  public Date getExpiration(String token);

  public Authentication createAuthenticationRequest(String token);

  /**
   * Tests token validity. Token is valid if: - not yet expired and - not revoked and - can be
   * verified as issued through this service (maybe via signature)
   *
   * @param token
   * @return
   */
  public Boolean isValid(String token);
}
