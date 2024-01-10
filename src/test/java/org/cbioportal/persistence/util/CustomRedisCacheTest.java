package org.cbioportal.persistence.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CustomRedisCacheTest {

    @Mock
    RedissonClient client;
    
    private RKeys mockKeys;

    @Before
    public void setUp() throws Exception {

        List<String> keys = Arrays .asList("subject_1_key_1", "subject_1_key_2", "subject_2_key_1", "subject_2_key_2");
        mockKeys = mock(RKeys.class);
        when(client.getKeys()).thenReturn(mockKeys);
        when(mockKeys.getKeysStream()).thenReturn(keys.stream());
        
        
    }

    @Test
    public void shouldHaveGetters() {
        CustomRedisCache subject = new CustomRedisCache("subject", client, -1);
        
        assertEquals("subject", subject.getName());
        assertEquals(client, subject.getNativeCache());
    }

    @Test
    public void shouldLookupObjectThatDoesNotExist() {
        RBucket bucket = Mockito.mock(RBucket.class);
        when(bucket.get()).thenReturn(null);
        when(client.getBucket("subject:57_onions"))
            .thenReturn(bucket);
        
        CustomRedisCache subject = new CustomRedisCache("subject", client, -1);
        Object actual = subject.lookup("57_onions");

        assertNull(actual);
    }

    @Test
    public void shouldLookupObjectThatDoesExistForStaticCache() {
        RBucket bucket = Mockito.mock(RBucket.class);
        when(bucket.get()).thenReturn(toStoreValue("success"));
        when(client.getBucket("subject:57_onions"))
            .thenReturn(bucket);

        CustomRedisCache subject = new CustomRedisCache("subject", client, -1);
        Object actual = subject.lookup("57_onions");

        assertEquals("success", actual);
        // cache is static, so don't refresh
        verify(bucket, times(0)).expireAsync(-1, TimeUnit.MINUTES);
    }

    @Test
    public void shouldLookupObjectThatDoesExistForGeneralCache() {
        RBucket bucket = Mockito.mock(RBucket.class);
        when(bucket.get()).thenReturn(toStoreValue("success"));
        when(client.getBucket("subject:57_onions"))
            .thenReturn(bucket);

        CustomRedisCache subject = new CustomRedisCache("subject", client, 100);
        Object actual = subject.lookup("57_onions");

        assertEquals("success", actual);
        // cache is not static, so refresh
        verify(bucket, times(1)).expireAsync(100, TimeUnit.MINUTES);
    }

    @Test
    public void shouldGetObjectThatDoesNotExist() {
        String defaultReturn = "this gets returned if the object DNE in cache";
        RBucket bucket = Mockito.mock(RBucket.class);
        when(bucket.get()).thenReturn(null);
        when(client.getBucket("subject:57_onions"))
            .thenReturn(bucket);

        CustomRedisCache subject = new CustomRedisCache("subject", client, -1);
        Object actual = subject.get("57_onions", () -> defaultReturn);

        assertEquals(defaultReturn, actual);
    }

    @Test
    public void shouldGetObjectThatDoesExistForStaticCache() {
        String defaultReturn = "this gets returned if the object DNE in cache";
        RBucket bucket = Mockito.mock(RBucket.class);
        when(bucket.get()).thenReturn(toStoreValue("success"));
        when(client.getBucket("subject:57_onions"))
            .thenReturn(bucket);

        CustomRedisCache subject = new CustomRedisCache("subject", client, -1);
        Object actual = subject.get("57_onions", () -> defaultReturn);

        assertEquals("success", actual);
        // cache is static, so don't refresh
        verify(bucket, times(0)).expireAsync(-1, TimeUnit.MINUTES);
    }

    @Test
    public void shouldGetObjectThatDoesExistForGeneralCache() {
        String defaultReturn = "this gets returned if the object DNE in cache";
        RBucket bucket = Mockito.mock(RBucket.class);
        when(bucket.get()).thenReturn(toStoreValue("success"));
        when(client.getBucket("subject:57_onions"))
            .thenReturn(bucket);

        CustomRedisCache subject = new CustomRedisCache("subject", client, 100);
        Object actual = subject.get("57_onions", () -> defaultReturn);

        assertEquals("success", actual);
        // cache is not static, so refresh
        verify(bucket, times(1)).expireAsync(100, TimeUnit.MINUTES);
    }


    @Test(expected = Cache.ValueRetrievalException.class)
    public void shouldPropagateValueLoaderException() {
        String defaultReturn = "this gets returned if the object DNE in cache";
        RBucket bucket = Mockito.mock(RBucket.class);
        when(bucket.get()).thenReturn(null);
        when(client.getBucket("subject:57_onions"))
            .thenReturn(bucket);

        CustomRedisCache subject = new CustomRedisCache("subject", client, -1);
        subject.get("57_onions", () -> {throw new RuntimeException("uh oh");});
    }

    @Test
    public void shouldPutObjectInStaticCache() {
        RBucket bucket = Mockito.mock(RBucket.class);
        when(client.getBucket("subject:57_onions"))
            .thenReturn(bucket);

        CustomRedisCache subject = new CustomRedisCache("subject", client, -1);
        subject.put("57_onions", "success");

        // this is tricky to verify, because the value is compressed before
        // it gets added to the cache
        verify(bucket, times(1)).setAsync(any());
        verify(bucket, times(0)).setAsync(any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void shouldPutObjectInGeneralCache() {
        RBucket bucket = Mockito.mock(RBucket.class);
        when(client.getBucket("subject:57_onions"))
            .thenReturn(bucket);

        CustomRedisCache subject = new CustomRedisCache("subject", client, 100);
        subject.put("57_onions", "success");

        // this is tricky to verify, because the value is compressed before
        // it gets added to the cache
        verify(bucket, times(0)).setAsync(any());
        verify(bucket, times(1)).setAsync(any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void shouldPutAbsentObject() {
        RBucket bucket = Mockito.mock(RBucket.class);
        when(bucket.get()).thenReturn(null);
        when(client.getBucket("subject:57_onions"))
            .thenReturn(bucket);

        CustomRedisCache subject = new CustomRedisCache("subject", client, 100);
        Cache.ValueWrapper actual = subject.putIfAbsent("57_onions", "success");

        // the value is absent, so there should be a put
        verify(bucket, times(1)).setAsync(any(), anyLong(), any(TimeUnit.class));
        assertEquals("success", actual.get());
    }

    @Test
    public void shouldNotPutPresentObject() {
        RBucket bucket = Mockito.mock(RBucket.class);
        when(bucket.get()).thenReturn(toStoreValue("success"));
        when(client.getBucket("subject:57_onions"))
            .thenReturn(bucket);

        CustomRedisCache subject = new CustomRedisCache("subject", client, 100);
        Cache.ValueWrapper actual = subject.putIfAbsent("57_onions", "success");

        // the value is absent, so there should be a put
        verify(bucket, times(0)).setAsync(any(), anyLong(), any(TimeUnit.class));
        assertEquals("success", actual.get());
    }
    
    @Test
    public void evictIfPresentNoStringPattern() {
        CustomRedisCache subject = new CustomRedisCache("subject", client, 100);
        subject.evictIfPresent(new HashMap<String,String>());
        subject.evictIfPresent(new HashMap<String,String>());
        verify(mockKeys, never()).delete(anyString());
    }

    @Test
    public void evictIfPresentNullPattern() {
        CustomRedisCache subject = new CustomRedisCache("subject", client, 100);
        subject.evictIfPresent(null);
        verify(mockKeys, never()).delete(anyString());
    }

    @Test
    public void evictIfPresentSuccess() {
        CustomRedisCache subject = new CustomRedisCache("subject_1", client, 100);
        subject.evictIfPresent(".*key_2.*");
        verify(mockKeys, times(1)).delete(eq("subject_1_key_2"));
    }

    @Test
    public void evictNoStringPattern() {
        CustomRedisCache subject = new CustomRedisCache("subject", client, 100);
        subject.evict(new HashMap<String,String>());
        verify(mockKeys, never()).delete(anyString());
    }

    @Test
    public void evictNullPattern() {
        CustomRedisCache subject = new CustomRedisCache("subject", client, 100);
        subject.evict(null);
        verify(mockKeys, never()).delete(anyString());
    }
    
    @Test
    public void evictSuccess() {
        CustomRedisCache subject = new CustomRedisCache("subject_1", client, 100);
        subject.evict(".*key_2.*");
        verify(mockKeys, times(1)).delete(eq("subject_1_key_2"));
    }
    
    @Test
    public void shouldClear() {
        RKeys allKeys = mock(RKeys.class);
        when(client.getKeys()).thenReturn(allKeys);
        CustomRedisCache subject = new CustomRedisCache("subject", client, 100);
        subject.clear();

        // evict is a no op, so there should be no calls to the client
        verify(client, times(1)).getKeys();
        verify(allKeys, times(1)).deleteByPattern("subject:*");
    }

    @Test
    public void shouldInvalidateEmptyCache() {
        RKeys allKeys = mock(RKeys.class);
        when(allKeys.deleteByPattern("subject:*")).thenReturn(0L);
        when(client.getKeys()).thenReturn(allKeys);
        CustomRedisCache subject = new CustomRedisCache("subject", client, 100);
        boolean actual = subject.invalidate();

        // evict is a no op, so there should be no calls to the client
        verify(client, times(1)).getKeys();
        verify(allKeys, times(1)).deleteByPattern("subject:*");
        assertFalse(actual);
    }

    @Test
    public void shouldRoundTripObject() {
        String toRoundTrip = "The quick brown fox jumped over the lazy dog";
        CustomRedisCache subject = new CustomRedisCache("subject", client, -1);

        Object compressed = subject.toStoreValue(toRoundTrip);
        String roundTripped = (String) subject.fromStoreValue(compressed);
        
        assertEquals(toRoundTrip, roundTripped);
    }

    @Test
    public void shouldRoundTripPrimitive() {
        int toRoundTrip = Integer.MAX_VALUE;
        CustomRedisCache subject = new CustomRedisCache("subject", client, -1);

        Object compressed = subject.toStoreValue(toRoundTrip);
        int roundTripped = (int) subject.fromStoreValue(compressed);

        assertEquals(toRoundTrip, roundTripped);
    }

    @Test
    public void shouldRoundTripNull() {
        Object toRoundTrip = null;
        CustomRedisCache subject = new CustomRedisCache("subject", client, -1);

        Object compressed = subject.toStoreValue(toRoundTrip);
        Object roundTripped = subject.fromStoreValue(compressed);

        assertEquals(toRoundTrip, roundTripped);
    }
    
    private Object toStoreValue(Object rawValue) {
        CustomRedisCache converter = new CustomRedisCache("", client, -1);
        return converter.toStoreValue(rawValue);
    }
}