package org.cbioportal.persistence.util;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Component
public class RedisControllerCache {
    @Value("${redis.ttl_minutes:60}")
    long ttlMinutes;
    
    @Autowired
    RedissonClient client;
    
    public Object get(String key) {
        return client.getBucket(key).get();
    }
    
    public void set(String key, Serializable value) {
        client.getBucket(key).setAsync(value);
    }
    
    public void setWithTTL(String key, Serializable value) {
        client.getBucket(key).setAsync(value, ttlMinutes, TimeUnit.MINUTES);
    }
}
