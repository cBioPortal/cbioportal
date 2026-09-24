package org.cbioportal.legacy.persistence.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Wraps every {@link DataSource} bean in the context -- the main one (whether from the standard
 * {@code spring.datasource.*}-driven autoconfiguration or the legacy {@code
 * spring.datasource.clickhouse.*} backward-compatibility path), and the file-export feature's
 * separate {@code exportDataSource} -- in a {@link DynamicDatabaseDataSource}. This way every
 * consumer of a {@link DataSource} bean transparently gets the switchable version, and switching
 * the active database (see {@code DatabaseSwitchServiceImpl}) affects the whole running instance
 * consistently, exports included.
 */
@Component
public class DynamicDatabaseDataSourceBeanPostProcessor implements BeanPostProcessor {

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    if (bean instanceof DataSource dataSource && !(bean instanceof DynamicDatabaseDataSource)) {
      return new DynamicDatabaseDataSource(dataSource);
    }
    return bean;
  }
}
