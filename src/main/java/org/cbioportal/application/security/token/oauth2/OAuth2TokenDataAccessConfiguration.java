/*
 * Copyright (c) 2020 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.application.security.token.oauth2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OAuth2TokenDataAccessConfiguration {

  @Bean
  // This bean is defined here so
  // that it can be stubbed in tests.
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
