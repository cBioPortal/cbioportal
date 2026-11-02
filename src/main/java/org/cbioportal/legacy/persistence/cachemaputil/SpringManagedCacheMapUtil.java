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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
// Instantiate when user authorization is active and spring-managed implementation is needed
@ConditionalOnExpression(
    "{'oauth2','saml','saml_plus_basic'}.contains('${authenticate}') or ('optional_oauth2' eq '${authenticate}' and 'true' eq '${security.method_authorization_enabled}')")
@ConditionalOnProperty(value = "cache.cache-map-utils.spring-managed", havingValue = "true")
public class SpringManagedCacheMapUtil implements CacheMapUtil {

  private static final Logger LOG = LoggerFactory.getLogger(SpringManagedCacheMapUtil.class);

  @Value("${persistence.cache_type:no-cache}")
  private String cacheType;

  @Value("${persistence.cache_type_clickhouse:no-cache}")
  private String cacheTypeClickhouse;

  @Value("${cache.cache-map-utils.spring-managed}")
  private boolean springManagedCacheMapUtils;

  @Autowired private CacheMapBuilder cacheMapBuilder;

  // Cancer-study permissions have their own short-TTL cache, decoupled from the general
  // Spring-managed cache's much longer TTL -- see CancerStudyPermissionCache.
  private final CancerStudyPermissionCache cancerStudyPermissionCache;

  public SpringManagedCacheMapUtil(CancerStudyPermissionCache cancerStudyPermissionCache) {
    this.cancerStudyPermissionCache = cancerStudyPermissionCache;
  }

  @PostConstruct
  public void init() {
    // Make sure the user does not have a conflicting configuration. Explode if there is.
    if (cacheType.equals("no-cache")
        && cacheTypeClickhouse.equals("no-cache")
        && springManagedCacheMapUtils) {
      throw new RuntimeException(
          "cache.cache-map-utils.spring-managed property is set to 'true' but the portal is not "
              + "configured with a cache-implementation (persistence.cache_type property is 'no-cache'). Please set to 'false'"
              + " or configure the cache.");
    }
  }

  // This implementation of the CacheMapUtils does not keep a locally cached/referenced HashMap
  // but retrieves the HashMaps from the active Spring caching solution.

  @Override
  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  public Map<String, MolecularProfile> getMolecularProfileMap() {
    LOG.debug("Building molecularProfileMap (cache miss)");
    return cacheMapBuilder.buildMolecularProfileMap();
  }

  @Override
  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  public Map<String, SampleList> getSampleListMap() {
    LOG.debug("Building sampleListMap (cache miss)");
    return cacheMapBuilder.buildSampleListMap();
  }

  @Override
  public Map<String, CancerStudy> getCancerStudyPermissionMap() {
    return cancerStudyPermissionCache.getCancerStudyPermissionMap();
  }

  //  bean is only instantiated when there is user authorization
  @Override
  public boolean hasCacheEnabled() {
    return true;
  }
}
