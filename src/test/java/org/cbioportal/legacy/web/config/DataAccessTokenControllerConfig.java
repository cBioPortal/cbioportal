/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.web.config;

import org.cbioportal.legacy.service.DataAccessTokenService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author ochoaa
 */
@TestConfiguration
public class DataAccessTokenControllerConfig {

  @Bean
  public DataAccessTokenService tokenService() {
    return Mockito.mock(DataAccessTokenService.class);
  }
}
