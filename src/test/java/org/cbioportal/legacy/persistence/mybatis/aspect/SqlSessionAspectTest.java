package org.cbioportal.legacy.persistence.mybatis.aspect;

import static org.junit.Assert.*;

import org.apache.ibatis.session.SqlSessionFactory;
import org.cbioportal.legacy.persistence.mybatis.annotation.UseSameSqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.SqlSessionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import org.mybatis.spring.SqlSessionFactoryBean;

/**
 * Unit test for SqlSessionAspect that verifies the @UseSameSqlSession annotation
 * properly manages SqlSession lifecycle.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SqlSessionAspectTest.TestConfig.class)
public class SqlSessionAspectTest {

  @Autowired private TestService testService;

  @Autowired private SqlSessionFactory sqlSessionFactory;

  @Configuration
  @EnableAspectJAutoProxy
  static class TestConfig {

    @Bean
    public SqlSessionAspect sqlSessionAspect() {
      return new SqlSessionAspect();
    }

    @Bean
    public DataSource dataSource() {
      DriverManagerDataSource dataSource = new DriverManagerDataSource();
      dataSource.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
      dataSource.setUrl("jdbc:ch://localhost:8443/test");
      dataSource.setUsername("default");
      dataSource.setPassword("");
      return dataSource;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
      SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
      factoryBean.setDataSource(dataSource());
      return factoryBean.getObject();
    }

    @Bean
    public TestService testService() {
      return new TestService();
    }
  }

  @Component
  static class TestService {
    @Autowired private ApplicationContext applicationContext;

    private boolean sessionWasBound;
    private Object firstSessionId;
    private Object secondSessionId;

    @UseSameSqlSession
    public String methodWithAnnotation() {
      SqlSessionFactory factory = applicationContext.getBean(SqlSessionFactory.class);

      // Check if session is bound
      sessionWasBound = TransactionSynchronizationManager.hasResource(factory);

      if (sessionWasBound) {
        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(factory);
        firstSessionId = holder.getSqlSession();
      }

      // Simulate another operation that should use same session
      callAnotherOperation();

      return "success";
    }

    private void callAnotherOperation() {
      SqlSessionFactory factory = applicationContext.getBean(SqlSessionFactory.class);
      if (TransactionSynchronizationManager.hasResource(factory)) {
        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(factory);
        secondSessionId = holder.getSqlSession();
      }
    }

    public String methodWithoutAnnotation() {
      SqlSessionFactory factory = applicationContext.getBean(SqlSessionFactory.class);
      return TransactionSynchronizationManager.hasResource(factory) ? "has session" : "no session";
    }

    public boolean wasSessionBound() {
      return sessionWasBound;
    }

    public boolean didUseSameSqlSession() {
      return firstSessionId != null && firstSessionId == secondSessionId;
    }

    public void reset() {
      sessionWasBound = false;
      firstSessionId = null;
      secondSessionId = null;
    }
  }

  @Before
  public void setup() {
    // Clear any existing session bindings
    if (TransactionSynchronizationManager.hasResource(sqlSessionFactory)) {
      TransactionSynchronizationManager.unbindResource(sqlSessionFactory);
    }
    testService.reset();
  }

  @After
  public void cleanup() {
    // Ensure resources are cleaned up after each test
    if (TransactionSynchronizationManager.hasResource(sqlSessionFactory)) {
      TransactionSynchronizationManager.unbindResource(sqlSessionFactory);
    }
  }

  @Test
  public void testSqlSessionIsBoundDuringAnnotatedMethod() {
    testService.methodWithAnnotation();

    assertTrue("Session should have been bound during method execution", testService.wasSessionBound());
  }

  @Test
  public void testSqlSessionIsUnboundAfterMethod() {
    testService.methodWithAnnotation();

    assertFalse(
        "Session should be unbound after method completes",
        TransactionSynchronizationManager.hasResource(sqlSessionFactory));
  }

  @Test
  public void testSameSqlSessionIsUsedWithinMethod() {
    testService.methodWithAnnotation();

    assertTrue("Both operations should use the same SqlSession", testService.didUseSameSqlSession());
  }

  @Test
  public void testMethodWithoutAnnotationDoesNotBindSession() {
    String result = testService.methodWithoutAnnotation();

    assertEquals("Method without annotation should not have session", "no session", result);
    assertFalse(
        "Session should not be bound",
        TransactionSynchronizationManager.hasResource(sqlSessionFactory));
  }

  @Test
  public void testMultipleSequentialCalls() {
    // First call
    testService.methodWithAnnotation();
    assertFalse("Session should be unbound after first call",
        TransactionSynchronizationManager.hasResource(sqlSessionFactory));

    // Second call
    testService.reset();
    testService.methodWithAnnotation();
    assertFalse("Session should be unbound after second call",
        TransactionSynchronizationManager.hasResource(sqlSessionFactory));
  }
}