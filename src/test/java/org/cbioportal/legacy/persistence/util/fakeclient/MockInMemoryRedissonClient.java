package org.cbioportal.legacy.persistence.util.fakeclient;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.redisson.api.*;
import org.redisson.api.bucket.*;
import org.redisson.api.keys.*;
import org.redisson.api.options.ClientSideCachingOptions;
import org.redisson.api.options.CommonOptions;
import org.redisson.api.options.JsonBucketOptions;
import org.redisson.api.options.KeysOptions;
import org.redisson.api.options.LiveObjectOptions;
import org.redisson.api.options.OptionalOptions;
import org.redisson.api.options.PatternTopicOptions;
import org.redisson.api.options.PlainOptions;
import org.redisson.api.redisnode.BaseRedisNodes;
import org.redisson.api.redisnode.RedisNodes;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonCodec;
import org.redisson.config.Config;

public class MockInMemoryRedissonClient implements RedissonClient {
  private final ConcurrentHashMap<String, RBucket> rBucketMap;
  private final ConcurrentHashMap<String, Object> valueMap;

  public MockInMemoryRedissonClient() {
    rBucketMap = new ConcurrentHashMap<>();
    valueMap = new ConcurrentHashMap<>();
  }

  @Override
  public <V> RBucket<V> getBucket(String s) {
    return rBucketMap.computeIfAbsent(s, (key) -> new MockRBucket(valueMap, key));
  }

  /*
   * Methods we don't use
   */
  @Override
  public RKeys getKeys() {
    return new MockRKeys(rBucketMap, valueMap);
  }

  @Override
  public <V, L> RTimeSeries<V, L> getTimeSeries(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V, L> RTimeSeries<V, L> getTimeSeries(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RStream<K, V> getStream(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RStream<K, V> getStream(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RSearch getSearch() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RSearch getSearch(Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RSearch getSearch(OptionalOptions options) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RRateLimiter getRateLimiter(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RBinaryStream getBinaryStream(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RGeo<V> getGeo(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RGeo<V> getGeo(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RSetCache<V> getSetCache(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RSetCache<V> getSetCache(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMapCache<K, V> getMapCache(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMapCache<K, V> getMapCache(
      String s, Codec codec, MapCacheOptions<K, V> mapOptions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMapCache<K, V> getMapCache(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMapCache<K, V> getMapCache(
      org.redisson.api.options.MapCacheOptions<K, V> options) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMapCache<K, V> getMapCache(String s, MapCacheOptions<K, V> mapOptions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBucket<V> getBucket(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RBuckets getBuckets() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RBuckets getBuckets(Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RBuckets getBuckets(OptionalOptions options) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RJsonBucket<V> getJsonBucket(String name, JsonCodec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RHyperLogLog<V> getHyperLogLog(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RHyperLogLog<V> getHyperLogLog(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RList<V> getList(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RList<V> getList(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RListMultimap<K, V> getListMultimap(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RListMultimap<K, V> getListMultimap(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RListMultimapCache<K, V> getListMultimapCache(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RListMultimapCache<K, V> getListMultimapCache(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(
      String s, LocalCachedMapOptions<K, V> localCachedMapOptions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(
      String s, Codec codec, LocalCachedMapOptions<K, V> localCachedMapOptions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMap<K, V> getMap(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMap<K, V> getMap(org.redisson.api.options.MapOptions<K, V> options) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMap<K, V> getMap(String s, MapOptions<K, V> mapOptions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMap<K, V> getMap(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMap<K, V> getMap(String s, Codec codec, MapOptions<K, V> mapOptions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RSetMultimap<K, V> getSetMultimap(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RSetMultimap<K, V> getSetMultimap(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RSemaphore getSemaphore(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RPermitExpirableSemaphore getPermitExpirableSemaphore(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLock getLock(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLock getSpinLock(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLock getSpinLock(String s, LockOptions.BackOff backOff) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RFencedLock getFencedLock(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLock getMultiLock(RLock... rLocks) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param rLocks
   * @deprecated
   */
  @Override
  @Deprecated
  public RLock getRedLock(RLock... rLocks) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLock getFairLock(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RReadWriteLock getReadWriteLock(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RSet<V> getSet(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RSet<V> getSet(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RSortedSet<V> getSortedSet(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RSortedSet<V> getSortedSet(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RScoredSortedSet<V> getScoredSortedSet(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RScoredSortedSet<V> getScoredSortedSet(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLexSortedSet getLexSortedSet(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RShardedTopic getShardedTopic(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RShardedTopic getShardedTopic(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RTopic getTopic(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RTopic getTopic(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RReliableTopic getReliableTopic(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RReliableTopic getReliableTopic(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RPatternTopic getPatternTopic(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RPatternTopic getPatternTopic(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RPatternTopic getPatternTopic(PatternTopicOptions options) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RQueue<V> getQueue(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RTransferQueue<V> getTransferQueue(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RTransferQueue<V> getTransferQueue(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RDelayedQueue<V> getDelayedQueue(RQueue<V> rQueue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RQueue<V> getQueue(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RRingBuffer<V> getRingBuffer(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RRingBuffer<V> getRingBuffer(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RPriorityQueue<V> getPriorityQueue(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RPriorityQueue<V> getPriorityQueue(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RPriorityDeque<V> getPriorityDeque(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RPriorityDeque<V> getPriorityDeque(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBlockingQueue<V> getBlockingQueue(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBlockingQueue<V> getBlockingQueue(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RDeque<V> getDeque(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RDeque<V> getDeque(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBlockingDeque<V> getBlockingDeque(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBlockingDeque<V> getBlockingDeque(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RAtomicLong getAtomicLong(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RAtomicDouble getAtomicDouble(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLongAdder getLongAdder(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RDoubleAdder getDoubleAdder(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RCountDownLatch getCountDownLatch(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RBitSet getBitSet(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBloomFilter<V> getBloomFilter(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBloomFilter<V> getBloomFilter(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RIdGenerator getIdGenerator(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RFunction getFunction() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RFunction getFunction(Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RScript getScript() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RScript getScript(Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RScheduledExecutorService getExecutorService(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RScheduledExecutorService getExecutorService(String s, ExecutorOptions executorOptions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RScheduledExecutorService getExecutorService(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RScheduledExecutorService getExecutorService(
      String s, Codec codec, ExecutorOptions executorOptions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RRemoteService getRemoteService() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RRemoteService getRemoteService(Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RRemoteService getRemoteService(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RRemoteService getRemoteService(String s, Codec codec) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RTransaction createTransaction(TransactionOptions transactionOptions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RBatch createBatch(BatchOptions batchOptions) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RBatch createBatch() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLiveObjectService getLiveObjectService() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RedissonRxClient rxJava() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RedissonReactiveClient reactive() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void shutdown() {}

  @Override
  public void shutdown(long l, long l1, TimeUnit timeUnit) {}

  @Override
  public Config getConfig() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends BaseRedisNodes> T getRedisNodes(RedisNodes<T> redisNodes) {
    throw new UnsupportedOperationException();
  }

  @Override
  public java.util.concurrent.CompletionStage<Void> shutdownAsync() {
    throw new UnsupportedOperationException();
  }

  @Override
  public java.util.concurrent.CompletionStage<Void> shutdownAsync(
      java.time.Duration quietPeriod, java.time.Duration timeout) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isShutdown() {
    return false;
  }

  @Override
  public boolean isShuttingDown() {
    return false;
  }

  @Override
  public String getId() {
    throw new UnsupportedOperationException();
  }

  /*
   * Methods we don't use (Redisson 4.x additions)
   */
  @Override
  public <V> RArray<V> getArray(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RArray<V> getArray(String p1, Codec p2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RArray<V> getArray(PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RGcra getGcra(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RGcra getGcra(CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMaps<K, V> getMaps() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMaps<K, V> getMaps(Codec p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMaps<K, V> getMaps(OptionalOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RJsonBucket<V> getJsonBucket(JsonBucketOptions<V> p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RJsonBuckets getJsonBuckets(JsonCodec p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RLocalCachedMapCache<K, V> getLocalCachedMapCache(
      String p1, LocalCachedMapCacheOptions<K, V> p2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RLocalCachedMapCache<K, V> getLocalCachedMapCache(
      String p1, Codec p2, LocalCachedMapCacheOptions<K, V> p3) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RListMultimapCacheNative<K, V> getListMultimapCacheNative(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RListMultimapCacheNative<K, V> getListMultimapCacheNative(String p1, Codec p2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RListMultimapCacheNative<K, V> getListMultimapCacheNative(PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(
      org.redisson.api.options.LocalCachedMapOptions<K, V> p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMapCacheNative<K, V> getMapCacheNative(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMapCacheNative<K, V> getMapCacheNative(String p1, Codec p2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RMapCacheNative<K, V> getMapCacheNative(
      org.redisson.api.options.MapOptions<K, V> p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RSetMultimapCacheNative<K, V> getSetMultimapCacheNative(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RSetMultimapCacheNative<K, V> getSetMultimapCacheNative(String p1, Codec p2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RSetMultimapCacheNative<K, V> getSetMultimapCacheNative(PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLock getNonReentrantLock(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLock getNonReentrantLock(CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLock getMultiLock(String p1, Collection<Object> p2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLock getNonReentrantFairLock(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLock getNonReentrantFairLock(CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RReliablePubSubTopic<V> getReliablePubSubTopic(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RReliablePubSubTopic<V> getReliablePubSubTopic(String p1, Codec p2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RReliablePubSubTopic<V> getReliablePubSubTopic(PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RReliableQueue<V> getReliableQueue(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RReliableQueue<V> getReliableQueue(String p1, Codec p2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RReliableQueue<V> getReliableQueue(PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RCircularBuffer<V> getCircularBuffer(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RCircularBuffer<V> getCircularBuffer(String p1, Codec p2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RCircularBuffer<V> getCircularBuffer(PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K> RBitVectorStore<K> getBitVectorStore(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K> RBitVectorStore<K> getBitVectorStore(String p1, Codec p2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K> RBitVectorStore<K> getBitVectorStore(PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBloomFilterNative<V> getBloomFilterNative(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBloomFilterNative<V> getBloomFilterNative(String p1, Codec p2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBloomFilterNative<V> getBloomFilterNative(PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RCuckooFilter<V> getCuckooFilter(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RCuckooFilter<V> getCuckooFilter(String p1, Codec p2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RCuckooFilter<V> getCuckooFilter(PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RTopK<V> getTopK(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RTopK<V> getTopK(String p1, Codec p2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RTopK<V> getTopK(PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RTDigest getTDigest(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RTDigest getTDigest(PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RVectorSet getVectorSet(String p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RVectorSet getVectorSet(CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RKeys getKeys(KeysOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLiveObjectService getLiveObjectService(LiveObjectOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RClientSideCaching getClientSideCaching(ClientSideCachingOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RRemoteService getRemoteService(PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RScheduledExecutorService getExecutorService(org.redisson.api.options.ExecutorOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RScript getScript(org.redisson.api.options.OptionalOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RFunction getFunction(org.redisson.api.options.OptionalOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RIdGenerator getIdGenerator(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBloomFilter<V> getBloomFilter(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RBitSet getBitSet(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RRateLimiter getRateLimiter(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RBinaryStream getBinaryStream(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RSemaphore getSemaphore(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RPermitExpirableSemaphore getPermitExpirableSemaphore(
      org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLock getLock(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RFencedLock getFencedLock(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLock getFairLock(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RReadWriteLock getReadWriteLock(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLexSortedSet getLexSortedSet(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RAtomicLong getAtomicLong(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RAtomicDouble getAtomicDouble(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RLongAdder getLongAdder(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RDoubleAdder getDoubleAdder(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RCountDownLatch getCountDownLatch(org.redisson.api.options.CommonOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V, L> RTimeSeries<V, L> getTimeSeries(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RStream<K, V> getStream(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RGeo<V> getGeo(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RSetCache<V> getSetCache(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBucket<V> getBucket(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RHyperLogLog<V> getHyperLogLog(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RList<V> getList(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RListMultimap<K, V> getListMultimap(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RListMultimapCache<K, V> getListMultimapCache(
      org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RSetMultimap<K, V> getSetMultimap(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(
      org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RSet<V> getSet(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RSortedSet<V> getSortedSet(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RScoredSortedSet<V> getScoredSortedSet(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RShardedTopic getShardedTopic(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RTopic getTopic(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RReliableTopic getReliableTopic(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RTransferQueue<V> getTransferQueue(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RQueue<V> getQueue(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RRingBuffer<V> getRingBuffer(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RPriorityQueue<V> getPriorityQueue(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(
      org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(
      org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RPriorityDeque<V> getPriorityDeque(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBlockingQueue<V> getBlockingQueue(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(
      org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RDeque<V> getDeque(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> RBlockingDeque<V> getBlockingDeque(org.redisson.api.options.PlainOptions p1) {
    throw new UnsupportedOperationException();
  }
}
