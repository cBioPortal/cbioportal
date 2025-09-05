package org.cbioportal.legacy.persistence.util;

import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import org.cbioportal.legacy.persistence.CacheEnabledConfig;
import org.cbioportal.legacy.persistence.StudyRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomKeyGeneratorTest {

  @InjectMocks private CustomKeyGenerator customKeyGenerator;

  @Mock private StudyRepository studyRepository;

  @Mock private CacheEnabledConfig cacheEnabledConfig;

  private String studyId1 = "test_study_1";
  private String studyId2 = "test_study_2";

  @Before
  public void setUp() throws Exception {
    when(cacheEnabledConfig.isEnabled()).thenReturn(true);
  }

  @Test
  public void testGenerateCacheDisabled() {
    when(cacheEnabledConfig.isEnabled()).thenReturn(false);
    Assert.assertEquals("", customKeyGenerator.generate(null, null));
  }

  @Test
  public void testGenerateCacheSuccessNoParams() throws Exception {
    Method functionToPass = this.getClass().getMethod("testGenerateCacheSuccessNoParams");
    Object hello = customKeyGenerator.generate(this, functionToPass);
    Assert.assertTrue(hello instanceof String);
    Assert.assertEquals(
        "CustomKeyGeneratorTest"
            + CustomKeyGenerator.CACHE_KEY_PARAM_DELIMITER
            + "testGenerateCacheSuccessNoParams"
            + CustomKeyGenerator.CACHE_KEY_PARAM_DELIMITER,
        (String) hello);
  }

  @Test
  public void testGenerateCacheSuccessWithParams() throws Exception {
    Method functionToPass = this.getClass().getMethod("testGenerateCacheSuccessNoParams");
    Object hello = customKeyGenerator.generate(this, functionToPass, "one", "two");
    Assert.assertTrue(hello instanceof String);
    StringBuilder expected = new StringBuilder();
    expected.append("CustomKeyGeneratorTest");
    expected.append(CustomKeyGenerator.CACHE_KEY_PARAM_DELIMITER);
    expected.append("testGenerateCacheSuccessNoParams");
    expected.append(CustomKeyGenerator.CACHE_KEY_PARAM_DELIMITER);
    expected.append("\"one\"");
    expected.append(CustomKeyGenerator.CACHE_KEY_PARAM_DELIMITER);
    expected.append("\"two\"");
    Assert.assertEquals(expected.toString(), (String) hello);
  }
}
