/*
 * Copyright (c) 2019 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.persistence.util;

import org.ehcache.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheEventLogger implements CacheEventListener<Object, Object> {

  private static final Logger log = LoggerFactory.getLogger(CacheEventLogger.class);

  // this is to allow spring to inject EhcacheStatistics via MethodInvokingFactoryBean
  private static EhcacheStatistics ehcacheStatistics;

  public static void setCacheStatistics(EhcacheStatistics ecs) {
    ehcacheStatistics = ecs;
  }

  @Override
  public void onEvent(CacheEvent<? extends Object, ? extends Object> cacheEvent) {
    if (log.isDebugEnabled()) {
      log.debug(
          "CACHE_EVENT:\n"
              + "\tTYPE: "
              + cacheEvent.getType()
              + "\n"
              + "\tKEY: "
              + cacheEvent.getKey()
              + "\n"
              + "\tVALUE: "
              + cacheEvent.getNewValue()
              + "\n"
              + "CACHE_EVENT<>\n");
      log.debug(ehcacheStatistics.getCacheStatistics());
    }
  }
}
