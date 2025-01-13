package org.cbioportal.persistence.util.fakeclient;

import org.redisson.api.ObjectListener;
import org.redisson.api.RBucket;
import org.redisson.api.RFuture;
import org.redisson.client.codec.Codec;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MockRBucket implements RBucket {
    private final ConcurrentHashMap<String, Object> rBucketCache;
    private final String key;
    
    public MockRBucket(ConcurrentHashMap<String, Object> rBucketCache, String key) {
        this.rBucketCache = rBucketCache;
        this.key = key;
    }

    @Override
    public Object get() {
        return rBucketCache.get(key);
    }

    @Override
    public void set(Object o) {
        rBucketCache.put(key, o);
    }

    @Override
    public void set(Object o, long l, TimeUnit timeUnit) {
        // This cache is really primitive, so ttl isn't fully implemented
        // Instead, if the ttl is 0, the object isn't added, but for any other value, it is
        // in the cache forever.
        if (l == 0) {
            set(null);
        } else {
            set(o);
        }
    }

    @Override
    public void setAndKeepTTL(Object o) {

    }

    @Override
    public RFuture<Void> setAsync(Object o) {
        set(o);
        return null;
    }

    @Override
    public RFuture<Void> setAsync(Object o, long l, TimeUnit timeUnit) {
        set(o, l, timeUnit);
        return null;
    }

    @Override
    public RFuture<Void> setAndKeepTTLAsync(Object o) {
        return null;
    }

    @Override
    public RFuture<Boolean> expireAsync(long l, TimeUnit timeUnit) {
        // This cache is really primitive, so ttl isn't fully implemented
        // Instead, if the ttl is 0, the object removed, but for any other value,
        // it isn't touched.
        if (l == 0) {
            set(null);
        }
        return null;
    }

    /*
     * Methods we don't use
     */
    @Override
    public long size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAndDelete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean trySet(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean trySet(Object o, long l, TimeUnit timeUnit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setIfAbsent(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setIfAbsent(Object o, Duration duration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setIfExists(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setIfExists(Object o, long l, TimeUnit timeUnit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean compareAndSet(Object o, Object v1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAndSet(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAndSet(Object o, long l, TimeUnit timeUnit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAndExpire(Duration duration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAndExpire(Instant instant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAndClearExpire() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getIdleTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long sizeInMemory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void restore(byte[] bytes) {

    }

    @Override
    public void restore(byte[] bytes, long l, TimeUnit timeUnit) {

    }

    @Override
    public void restoreAndReplace(byte[] bytes) {

    }

    @Override
    public void restoreAndReplace(byte[] bytes, long l, TimeUnit timeUnit) {

    }

    @Override
    public byte[] dump() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean touch() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void migrate(String s, int i, int i1, long l) {

    }

    @Override
    public void copy(String s, int i, int i1, long l) {

    }

    @Override
    public boolean move(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean delete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean unlink() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rename(String s) {

    }

    @Override
    public boolean renamenx(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isExists() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Codec getCodec() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int addListener(ObjectListener objectListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeListener(int i) {

    }

    @Override
    public RFuture<Long> sizeAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture getAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture getAndDeleteAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> trySetAsync(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> trySetAsync(Object o, long l, TimeUnit timeUnit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> setIfAbsentAsync(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> setIfAbsentAsync(Object o, Duration duration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> setIfExistsAsync(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> setIfExistsAsync(Object o, long l, TimeUnit timeUnit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> compareAndSetAsync(Object o, Object v1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture getAndSetAsync(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture getAndSetAsync(Object o, long l, TimeUnit timeUnit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture getAndExpireAsync(Duration duration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture getAndExpireAsync(Instant instant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture getAndClearExpireAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expire(long l, TimeUnit timeUnit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expireAt(long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expireAt(Date date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expire(Instant instant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expireIfSet(Instant instant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expireIfNotSet(Instant instant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expireIfGreater(Instant instant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expireIfLess(Instant instant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expire(Duration duration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expireIfSet(Duration duration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expireIfNotSet(Duration duration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expireIfGreater(Duration duration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expireIfLess(Duration duration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean clearExpire() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long remainTimeToLive() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getExpireTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> expireAtAsync(Date date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> expireAtAsync(long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> expireAsync(Instant instant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> expireIfSetAsync(Instant instant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> expireIfNotSetAsync(Instant instant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> expireIfGreaterAsync(Instant instant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> expireIfLessAsync(Instant instant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> expireAsync(Duration duration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> expireIfSetAsync(Duration duration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> expireIfNotSetAsync(Duration duration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> expireIfGreaterAsync(Duration duration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> expireIfLessAsync(Duration duration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> clearExpireAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Long> remainTimeToLiveAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Long> getExpireTimeAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Long> getIdleTimeAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Long> sizeInMemoryAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> restoreAsync(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> restoreAsync(byte[] bytes, long l, TimeUnit timeUnit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> restoreAndReplaceAsync(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> restoreAndReplaceAsync(byte[] bytes, long l, TimeUnit timeUnit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<byte[]> dumpAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> touchAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> migrateAsync(String s, int i, int i1, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> copyAsync(String s, int i, int i1, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> moveAsync(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> deleteAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> unlinkAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> renameAsync(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> renamenxAsync(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> isExistsAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Integer> addListenerAsync(ObjectListener objectListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> removeListenerAsync(int i) {
        throw new UnsupportedOperationException();
    }
}
