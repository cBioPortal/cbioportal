/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.web.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class TokenAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().write("Authentication successful");
    response.getWriter().flush();
    // We do not need to do anything extra on REST authentication success, because there is no page
    // to redirect to
  }
}
