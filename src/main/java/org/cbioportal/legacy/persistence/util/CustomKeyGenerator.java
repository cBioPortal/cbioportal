/*
 * Copyright (c) 2019 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.persistence.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.util.Select;
import org.cbioportal.legacy.persistence.CacheEnabledConfig;
import org.cbioportal.legacy.persistence.StudyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.util.DigestUtils;

public class CustomKeyGenerator implements KeyGenerator {
  public static final String CACHE_KEY_PARAM_DELIMITER = "_";
  public static final int PARAM_LENGTH_HASH_LIMIT = 1024;

  @Autowired private CacheEnabledConfig cacheEnabledConfig;

  @Autowired private StudyRepository studyRepository;

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final Logger LOG = LoggerFactory.getLogger(CustomKeyGenerator.class);

  public Object generate(Object target, Method method, Object... params) {
    if (!cacheEnabledConfig.isEnabled() && !cacheEnabledConfig.isEnabledClickhouse()) {
      return "";
    }
    String key =
        target.getClass().getSimpleName()
            + CACHE_KEY_PARAM_DELIMITER
            + method.getName()
            + CACHE_KEY_PARAM_DELIMITER
            + Arrays.stream(params)
                .map(this::exceptionlessWrite)
                .collect(Collectors.joining(CACHE_KEY_PARAM_DELIMITER));
    LOG.debug("Created key: " + key);
    return key;
  }

  private String exceptionlessWrite(Object toSerialize) {
    if (toSerialize instanceof Select && ((Select) toSerialize).hasAll()) {
      // Select implements Iterable, but Select.All throws an exception
      // when you call iterator(), which breaks Jackson, so we need some custom logic
      return "Select.ALL";
    }
    try {
      String json = mapper.writeValueAsString(toSerialize);

      if (json.length() > PARAM_LENGTH_HASH_LIMIT) {
        // hash long keys to avoid redis key length limit
        return DigestUtils.md5DigestAsHex(json.getBytes());
      } else {
        // leave short keys intact, but remove semicolons to make things look cleaner in redis
        return json.replaceAll(":", CACHE_KEY_PARAM_DELIMITER);
      }
    } catch (JsonProcessingException e) {
      LOG.error("Could not serialize param to string: ", e);
      return "";
    }
  }
}
