package org.cbioportal.persistence.util;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.core.serializer.support.SerializationDelegate;
import org.springframework.lang.Nullable;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CustomRedisCache extends AbstractValueAdaptingCache {
    private static final Logger LOG = LoggerFactory.getLogger(CustomRedisCache.class);
    
    private final String name;
    private long ttlMinutes;
    private final RedissonClient store;

    /**
     * Create a new ConcurrentMapCache with the specified name.
     * @param name the name of the cache
     */
    public CustomRedisCache(String name, RedissonClient client, long ttlMinutes) {
        super(true);
        this.name = name;
        this.store = client;
        this.ttlMinutes = ttlMinutes;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final RedissonClient getNativeCache() {
        return this.store;
    }

    @Override
    @Nullable
    protected Object lookup(Object key) {
        return this.store.getBucket(name + "::" + key).get();
    }

    @Override
    @Nullable
    public <T> T get(Object key, Callable<T> valueLoader) {
        T value = (T) this.store.getBucket(name + "::" + key).get();
        try {
            return value == null ? valueLoader.call() : value;
        } catch (Exception ex) {
            throw new ValueRetrievalException(key, valueLoader, ex);
        }
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        if (ttlMinutes == -1) {
            this.store.getBucket(name + "::" + key).setAsync(toStoreValue(value));
        } else {
            this.store.getBucket(name + "::" + key).setAsync(toStoreValue(value), ttlMinutes, TimeUnit.MINUTES);
        }
    }

    @Override
    @Nullable
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        Object cached = this.store.getBucket(name + "::" + key).get();
        if (cached != null) {
            return toValueWrapper(cached);
        }
        put(key, value);
        return toValueWrapper(value);
    }

    @Override
    public void evict(Object key) {
        this.store.getBucket(name + "::" + key).deleteAsync();
    }

    @Override
    public boolean evictIfPresent(Object key) {
        return this.store.getBucket(name + "::" + key).delete();
    }

    @Override
    public void clear() {
        this.store.getKeys().deleteByPattern(name + "::" + "*");
    }

    @Override
    public boolean invalidate() {
        boolean notEmpty = this.store.getKeys().count() > 0;
        clear();
        return notEmpty;
    }

    @Override
    protected Object toStoreValue(@Nullable Object userValue) {
        if (userValue == null) {
            LOG.warn("Storing null value in cache. That's probably not great.");
            return null;
        }
        
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = null;
        try {
            // serialize to byte array
            objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(userValue);
            objectOut.flush();
            byte[] uncompressedByteArray = byteOut.toByteArray();
            
            // compress byte array
            byteOut = new ByteArrayOutputStream(uncompressedByteArray.length);
            GZIPOutputStream g = new GZIPOutputStream(byteOut);
            g.write(uncompressedByteArray);
            g.close();
            return byteOut.toByteArray();
        } catch (IOException e) {
            LOG.warn("Error compressing object for cache: ", e);
            return null;
        }
    }

    @Override
    protected Object fromStoreValue(@Nullable Object storeValue) {
        if (storeValue == null) {
            return null;
        }
        
        byte[] bytes = (byte[]) storeValue;
        ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        GZIPInputStream gzipIn = null;
        try {
            // inflate to byte array
            gzipIn = new GZIPInputStream(byteIn);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) != -1) {
                byteOut.write(buffer, 0, len);
            }
            byte[] unzippedBytes = byteOut.toByteArray();
            
            // deserialize byte array to object
            byteIn = new ByteArrayInputStream(unzippedBytes);
            ObjectInputStream oi = new ObjectInputStream(byteIn);
            return oi.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOG.warn("Error inflating object from cache: ", e);
            return null;
        }
    }
}
