package org.cbioportal.persistence.util;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.persistence.CacheEnabledConfig;
import org.cbioportal.persistence.StudyRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomKeyGeneratorTest {

    @InjectMocks
    private CustomKeyGenerator customKeyGenerator;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private CacheEnabledConfig cacheEnabledConfig;
    
    private String studyId1 = "test_study_1";
    private String studyId2 = "test_study_2";

    @Before
    public void setUp() throws Exception {
        when(cacheEnabledConfig.isEnabled()).thenReturn(true);
        CancerStudy cancerStudy1 = mock(CancerStudy.class);
        when(cancerStudy1.getCancerStudyIdentifier()).thenReturn(studyId1);
        CancerStudy cancerStudy2 = mock(CancerStudy.class);
        when(cancerStudy2.getCancerStudyIdentifier()).thenReturn(studyId2);
        when(studyRepository.getAllStudies(any(), any(), any(), any(), any(), any())).thenReturn(Arrays.asList(cancerStudy1, cancerStudy2));
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
        Assert.assertEquals("CustomKeyGeneratorTest" + CustomKeyGenerator.CACHE_KEY_PARAM_DELIMITER + "testGenerateCacheSuccessNoParams" + CustomKeyGenerator.CACHE_KEY_PARAM_DELIMITER, (String) hello);
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
    
    // Make sure that the study ids are extracted into the key name
    // when hashing is active due to long params. This is to ensure
    // that cache eviction for specific studies can occur.
    @Test
    public void testGenerateCacheSuccessWithLongParam() throws Exception {
        Method functionToPass = this.getClass().getMethod("testGenerateCacheSuccessNoParams");
        
        StringBuilder requestParams = new StringBuilder();
        requestParams.append("-----");
        requestParams.append(studyId1);
        requestParams.append("-----");
        requestParams.append(studyId2);
        requestParams.append("-----");
        for (int i = CustomKeyGenerator.PARAM_LENGTH_HASH_LIMIT + 100; i > 0; i--) {
            requestParams.append("-");
        }
        Object hello = customKeyGenerator.generate(this, functionToPass, "one", requestParams.toString());
        
        Assert.assertTrue(hello instanceof String);
        Assert.assertTrue(((String) hello).contains("test_study_1_test_study_2_22cc100378d5dc33c03fb0f39a61c692"));
    }
}