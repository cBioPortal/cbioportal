/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.application.security.token;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.cbioportal.legacy.service.DataAccessTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

/**
 * @author Manda Wilson
 */
public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

  private DataAccessTokenService tokenService;

  private static final String BEARER = "Bearer";

  private static final Logger LOG = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

  public TokenAuthenticationFilter() {
    // allow any request to contain an authorization header
    super("/**");
  }

  public TokenAuthenticationFilter(String s, AuthenticationManager authenticationManagerBean) {
    super(s, authenticationManagerBean);
  }

  public TokenAuthenticationFilter(
      String s, AuthenticationManager authenticationManager, DataAccessTokenService tokenService) {
    super(s, authenticationManager);
    this.tokenService = tokenService;
  }

  @Override
  protected boolean requiresAuthentication(
      HttpServletRequest request, HttpServletResponse response) {
    // only required if we do see an authorization header
    String param = request.getHeader(AUTHORIZATION);
    if (param == null) {
      LOG.debug(
          "attemptAuthentication(), authorization header is null, continue on to other security filters");
      return false;
    }
    return true;
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, IOException, jakarta.servlet.ServletException {

    String token = extractHeaderToken(request);

    if (token == null) {
      LOG.error("No token was found in request header.");
      throw new BadCredentialsException("No token was found in request header.");
    }

    Authentication auth = tokenService.createAuthenticationRequest(token);

    return getAuthenticationManager().authenticate(auth);
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      jakarta.servlet.FilterChain chain,
      Authentication authResult)
      throws IOException, ServletException {
    super.successfulAuthentication(request, response, chain, authResult);
    chain.doFilter(request, response);
  }

  /**
   * Extract the bearer token from a header.
   *
   * @param request
   * @return The token, or null if no authorization header was supplied
   */
  protected String extractHeaderToken(HttpServletRequest request) {
    String authorizationHeader = request.getHeader(AUTHORIZATION);
    if (authorizationHeader != null
        && !authorizationHeader.isEmpty()
        && authorizationHeader.toLowerCase().startsWith(BEARER.toLowerCase())) {
      return authorizationHeader.substring(BEARER.length()).trim();
    }
    return null;
  }
}
