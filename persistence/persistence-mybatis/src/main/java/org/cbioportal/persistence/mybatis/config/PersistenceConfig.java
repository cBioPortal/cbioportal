package org.cbioportal.persistence.mybatis.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@MapperScan("org.cbioportal.persistence.mybatis")
@Configuration
public class PersistenceConfig {
}
