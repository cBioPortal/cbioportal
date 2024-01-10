package org.cbioportal.persistence.util;

import org.cbioportal.persistence.util.fakeclient.MockInMemoryRedissonClient;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * This test is... not great, but it's my attempt to create a
 * runnable integration test for the CustomRedisCache that doesn't
 * have a ton of dependencies or a long runtime. Believe it or not,
 * this test did catch some bugs; so while it isn't a perfect integration
 * test by any means, I think it is worth something.
 * 
 * What I've done is create a hollow implementation of RedissionClient that
 * acts as a _very_ primitive in memory cache. This test just runs some basic
 * caching operations, namely set, get, and clear.
 */
public class CustomRedisCacheIntegrationTest {
    @Test
    public void shouldAddThenEvict() {
        MockInMemoryRedissonClient fakeClient = new MockInMemoryRedissonClient();
        CustomRedisCache subject = new CustomRedisCache("generalCache", fakeClient, 100);
        
        subject.put("key", "value");
        Object actualValue = subject.lookup("key");
        assertEquals("value", actualValue);
        
        subject.clear();
        actualValue = subject.lookup("key");
        assertNull(actualValue);        
    }
}