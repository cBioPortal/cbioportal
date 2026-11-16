/*
 * Copyright (c) 2018 - 2019 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.persistence.cachemaputil;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.SampleList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
// Instantiate when user authorization is active and spring-managed implementation is not needed
@ConditionalOnExpression(
    "{'oauth2','saml','saml_plus_basic'}.contains('${authenticate}') or ('optional_oauth2' eq '${authenticate}' and 'true' eq '${security.method_authorization_enabled}')")
@ConditionalOnProperty(
    value = "cache.cache-map-utils.spring-managed",
    havingValue = "false",
    matchIfMissing = true)
public class StaticRefCacheMapUtil implements CacheMapUtil {

  private static final Logger LOG = LoggerFactory.getLogger(StaticRefCacheMapUtil.class);

  @Autowired private CacheMapBuilder cacheMapBuilder;

  // Cancer-study permissions have their own short-TTL cache, decoupled from the rest of this
  // class's forever-cached maps -- see CancerStudyPermissionCache.
  private final CancerStudyPermissionCache cancerStudyPermissionCache;

  public StaticRefCacheMapUtil(CancerStudyPermissionCache cancerStudyPermissionCache) {
    this.cancerStudyPermissionCache = cancerStudyPermissionCache;
  }

  // This implementation of the CacheMapUtils keeps a locally cached/referenced HashMap and does
  // not defer to any Spring managed caching solution.

  // maps used to cache required relationships - in all maps stable ids are key
  // Fields are static because the proxying mechanism of the CancerStudyPermissionEvaluator
  // appears to perturb the Singleton scope of the CacheMapUtils bean. When debugging
  // two version appeared to exist in context. A mechanism with bean injection did not work here.
  static Map<String, MolecularProfile> molecularProfileCache;
  static Map<String, SampleList> sampleListCache;

  @PostConstruct
  private void init() {
    initializeCacheMemory();
  }

  public synchronized void initializeCacheMemory() {
    LOG.debug("creating cache maps for authorization");
    molecularProfileCache = cacheMapBuilder.buildMolecularProfileMap();
    sampleListCache = cacheMapBuilder.buildSampleListMap();
  }

  @Override
  public Map<String, MolecularProfile> getMolecularProfileMap() {
    return molecularProfileCache;
  }

  @Override
  public Map<String, SampleList> getSampleListMap() {
    return sampleListCache;
  }

  @Override
  public Map<String, CancerStudy> getCancerStudyPermissionMap() {
    return cancerStudyPermissionCache.getCancerStudyPermissionMap();
  }

  @Override
  public boolean hasCacheEnabled() {
    return true;
  }
}
