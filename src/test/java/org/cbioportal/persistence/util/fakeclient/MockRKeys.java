package org.cbioportal.persistence.util.fakeclient;

import org.redisson.api.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MockRKeys implements RKeys {
    private final ConcurrentMap<String, RBucket> cache;
    private final ConcurrentHashMap<String, Object> valueMap;

    public MockRKeys(ConcurrentMap<String, RBucket> cache, ConcurrentHashMap<String, Object> valueMap) {
        this.cache = cache;
        this.valueMap = valueMap;
    }

    @Override
    public long deleteByPattern(String s) {
        Predicate<String> matches = Pattern.compile(s).asPredicate();
        
        valueMap.keySet().stream()
            .filter(matches)
            .forEach(valueMap::remove);
        
        return cache.keySet().stream()
            .filter(matches)
            .peek(cache::remove)
            .count();
    }

    @Override
    public Iterable<String> getKeysWithLimit(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<String> getKeysWithLimit(String s, int i) {
        throw new UnsupportedOperationException();
    }

    /*
     * Methods we don't use
     */
    @Override
    public boolean move(String s, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void migrate(String s, String s1, int i, int i1, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copy(String s, String s1, int i, int i1, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expire(String s, long l, TimeUnit timeUnit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expireAt(String s, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean clearExpire(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean renamenx(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rename(String s, String s1) {

    }

    @Override
    public long remainTimeToLive(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long touch(String... strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long countExists(String... strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RType getType(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSlot(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<String> getKeysByPattern(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<String> getKeysByPattern(String s, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<String> getKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<String> getKeys(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<String> getKeysStreamByPattern(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<String> getKeysStreamByPattern(String s, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<String> getKeysStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<String> getKeysStream(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String randomKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long delete(RObject... rObjects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long delete(String... strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long unlink(String... strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void swapdb(int i, int i1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushdb() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushdbParallel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushall() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushallParallel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> moveAsync(String s, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> migrateAsync(String s, String s1, int i, int i1, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> copyAsync(String s, String s1, int i, int i1, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> expireAsync(String s, long l, TimeUnit timeUnit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> expireAtAsync(String s, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> clearExpireAsync(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Boolean> renamenxAsync(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> renameAsync(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Long> remainTimeToLiveAsync(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Long> touchAsync(String... strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Long> countExistsAsync(String... strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<RType> getTypeAsync(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Integer> getSlotAsync(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<String> randomKeyAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Long> deleteByPatternAsync(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Long> deleteAsync(RObject... rObjects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Long> deleteAsync(String... strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Long> unlinkAsync(String... strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Long> countAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> swapdbAsync(int i, int i1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> flushdbAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> flushallAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> flushdbParallelAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RFuture<Void> flushallParallelAsync() {
        throw new UnsupportedOperationException();
    }
}
