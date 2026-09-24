package org.cbioportal.legacy.persistence.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import javax.sql.DataSource;
import org.junit.Test;

public class DynamicDatabaseDataSourceBeanPostProcessorTest {

  private final DynamicDatabaseDataSourceBeanPostProcessor postProcessor =
      new DynamicDatabaseDataSourceBeanPostProcessor();

  @Test
  public void wrapsAPlainDataSourceBean() {
    DataSource plain = mock(DataSource.class);
    Object result = postProcessor.postProcessAfterInitialization(plain, "dataSource");
    assertTrue(result instanceof DynamicDatabaseDataSource);
  }

  @Test
  public void doesNotDoubleWrapAnAlreadyWrappedDataSource() {
    DynamicDatabaseDataSource alreadyWrapped =
        new DynamicDatabaseDataSource(mock(DataSource.class));
    Object result = postProcessor.postProcessAfterInitialization(alreadyWrapped, "dataSource");
    assertSame(alreadyWrapped, result);
  }

  @Test
  public void leavesUnrelatedBeansUntouched() {
    Object other = new Object();
    Object result = postProcessor.postProcessAfterInitialization(other, "someOtherBean");
    assertEquals(other, result);
  }
}
