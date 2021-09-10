package org.cbioportal.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("org.cbioportal.persistence")
public class PersistenceConfig {


}
