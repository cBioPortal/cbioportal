package org.cbioportal.shared;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RedisCondition implements Condition {
  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String cacheType = context.getEnvironment().getProperty("persistence.cache_type");
    String cacheTypeClickhouse =
        context.getEnvironment().getProperty("persistence.cache_type_clickhouse");
    return "redis".equalsIgnoreCase(cacheType) || "redis".equalsIgnoreCase(cacheTypeClickhouse);
  }
}
