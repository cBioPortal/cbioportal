package org.cbioportal.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * Fallback configuration that registers the resource mapper on the primary datasource when no
 * secondary ClickHouse datasource is configured.
 *
 * <p>When spring.clickhouse.secondary.url IS set, SecondaryClickhouseConfig takes over and this
 * config is inactive (because SecondaryClickhouseConfig creates the
 * "secondaryClickhouseSqlSessionFactory" bean first).
 */
@Configuration
@ConditionalOnMissingBean(name = "secondaryClickhouseSqlSessionFactory")
@MapperScan(
    basePackages = "org.cbioportal.infrastructure.repository.clickhouse.resource",
    sqlSessionFactoryRef = "sqlSessionFactory")
public class ResourceMapperFallbackConfig {}
