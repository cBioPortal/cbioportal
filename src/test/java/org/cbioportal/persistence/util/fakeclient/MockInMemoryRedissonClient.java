package org.cbioportal.persistence.util.fakeclient;

import org.redisson.api.*;
import org.redisson.api.redisnode.BaseRedisNodes;
import org.redisson.api.redisnode.RedisNodes;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonCodec;
import org.redisson.config.Config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
    public <K, V> RMapCache<K, V> getMapCache(String s, Codec codec, MapOptions<K, V> mapOptions) {
        throw new UnsupportedOperationException(); 
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String s) {
        throw new UnsupportedOperationException(); 
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String s, MapOptions<K, V> mapOptions) {
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
    public <V> RJsonBucket<V> getJsonBucket(String name, JsonCodec<V> codec) {
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
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String s, LocalCachedMapOptions<K, V> localCachedMapOptions) {
        throw new UnsupportedOperationException(); 
    }

    @Override
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String s, Codec codec, LocalCachedMapOptions<K, V> localCachedMapOptions) {
        throw new UnsupportedOperationException(); 
    }

    @Override
    public <K, V> RMap<K, V> getMap(String s) {
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
    public RScheduledExecutorService getExecutorService(String s, Codec codec, ExecutorOptions executorOptions) {
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
    public void shutdown() {

    }

    @Override
    public void shutdown(long l, long l1, TimeUnit timeUnit) {

    }

    @Override
    public Config getConfig() {
        throw new UnsupportedOperationException(); 
    }

    @Override
    public <T extends BaseRedisNodes> T getRedisNodes(RedisNodes<T> redisNodes) {
        throw new UnsupportedOperationException(); 
    }

    /**
     * @deprecated
     */
    @Override
    @Deprecated
    public NodesGroup<Node> getNodesGroup() {
        throw new UnsupportedOperationException(); 
    }

    /**
     * @deprecated
     */
    @Override
    @Deprecated
    public ClusterNodesGroup getClusterNodesGroup() {
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
}
