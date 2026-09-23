package org.cbioportal.legacy.persistence.util.fakeclient;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.redisson.api.*;
import org.redisson.api.keys.MigrateArgs;

public class MockRKeys implements RKeys {
  private final ConcurrentMap<String, RBucket> cache;
  private final ConcurrentHashMap<String, Object> valueMap;

  public MockRKeys(
      ConcurrentMap<String, RBucket> cache, ConcurrentHashMap<String, Object> valueMap) {
    this.cache = cache;
    this.valueMap = valueMap;
  }

  @Override
  public long deleteByPattern(String s) {
    Predicate<String> matches = Pattern.compile(s).asPredicate();

    valueMap.keySet().stream().filter(matches).forEach(valueMap::remove);

    return cache.keySet().stream().filter(matches).peek(cache::remove).count();
  }

  @Override
  public Iterable<String> getKeysWithLimit(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeListener(int i) {}

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
  public void rename(String s, String s1) {}

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

  /*
   * Methods we don't use (Redisson 4.x additions)
   */
  @Override
  public void migrate(MigrateArgs p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long expire(Duration p1, String... p2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long unlinkByPattern(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int addListener(ObjectListener p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterable<String> getKeys(org.redisson.api.options.KeysScanOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Stream<String> getKeysStream(org.redisson.api.options.KeysScanOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long expireAt(java.time.Instant instant, String... strings) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RFuture<Void> removeListenerAsync(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RFuture<Void> migrateAsync(org.redisson.api.keys.MigrateArgs q1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RFuture<Long> expireAsync(java.time.Duration q1, String... q2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RFuture<Long> expireAtAsync(java.time.Instant q1, String... q2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public org.redisson.api.AsyncIterator<String> getKeysAsync() {
    throw new UnsupportedOperationException();
  }

  @Override
  public org.redisson.api.AsyncIterator<String> getKeysAsync(
      org.redisson.api.options.KeysScanOptions q1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RFuture<Long> unlinkByPatternAsync(String q1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RFuture<Integer> addListenerAsync(ObjectListener q1) {
    throw new UnsupportedOperationException();
  }
}
