/*
 * Copyright (c) 2020 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.cbioportal.legacy.utils.config;

import java.util.Map;
import java.util.stream.Stream;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.type.AnnotatedTypeMetadata;

@PropertySources({
  @PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true),
  @PropertySource(
      value = "file:///${PORTAL_HOME}/application.properties",
      ignoreResourceNotFound = true)
})
// Adapted from Spring Boot
public class PropertyCondition implements Condition {

  public PropertyCondition() {
    super();
  }

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    Map<String, Object> attributes =
        metadata.getAnnotationAttributes(ConditionalOnProperty.class.getName());
    String name = (String) attributes.get("name");
    Object requiredValue = attributes.get("havingValue");
    boolean matchIfMissing = (boolean) attributes.get("matchIfMissing");
    boolean isNot = (boolean) attributes.get("isNot");
    String actualValue = context.getEnvironment().getProperty(name);
    if (actualValue == null) return matchIfMissing;
    if (requiredValue instanceof String[]) {
      if (isNot)
        return Stream.of((String[]) requiredValue)
            .noneMatch(value -> value.equalsIgnoreCase(actualValue));
      return Stream.of((String[]) requiredValue)
          .anyMatch(value -> value.equalsIgnoreCase(actualValue));
    }
    return isNot
        ? !((String) requiredValue).equalsIgnoreCase(actualValue)
        : ((String) requiredValue).equalsIgnoreCase(actualValue);
  }
}
