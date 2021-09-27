# Backend Caching 
cBioPortal provides the option of caching information on the backend to improve performance. Without caching, every time a request is received by the backend, a query is sent to the database system for information, and the returned data is processed to construct a response. This may lead to performance issues as the entire process can be rather costly, especially for queries on larger studies. With caching turned on, query responses can be taken directly from the cache if they have already been constructed. They would only be constructed for the initial query.

## Cache Configuration
The portal is configured to use Ehcache or Redis for backend caching. Ehcache supports a hybrid (disk + heap), disk-only, and heap-only mode. Redis stores the cache in memory and periodically writes the updated data to disk. Cache configuration is specified inside `portal.properties`(more information [here](portal.properties-Reference.md#cache-settings).
 
## Creating additional caches
The default configuration initializes two seperate caches; however, you may wish to introduce new caches for different datatypes. Please see the [Redis](#redis) and [Ehcache](#ehcache) sections to see how to set up a new cache in whichever system you are using.

### Redis

Cache initialization is handled inside the [CustomRedisCachingProvider](../persistence/persistence-api/src/main/java/org/cbioportal/persistence/util/CustomRedisCachingProvider.java). To create additional caches (e.g creating a cache specifically for clinical data), new code must be added to the `CustomRedisCachingProvider`.

Within the `CustomRedisCachingProvider`, create your new cache using the `CacheManager`.  The appName must be prepended to your cache name.
```
manager.createCache(appName + "ClinicalDataCache", config);
```

You also need to create a new cache resolver in [RedisConfig.java](../persistence/persistence-api/src/main/java/org/cbioportal/persistence/config/RedisConfig.java):
```
@Bean
public CacheResolver generalRepositoryCacheResolver() {
    return new NamedCacheResolver(cacheManager(), ${redis.name:cbioportal} + "GeneralRepositoryCache");
}
```

The `@Cacheable` annotation must also be added (or adjusted) to function declarations to indicate which functions are to be cached. Those might look like this example:
``` 
@Cacheable(cacheResolver = "clinicalDataCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
public String getDataFromClinicalDataRepository(String param) {}
```

For more information on linking caches to functions, refer to the documentation [here](https://spring.io/guides/gs/caching/).

### Ehcache

Within the `CustomEhcachingProvider`, initialize a new `ResourcePoolsBuilder` for the new cache and set the resources accordingly. 
```
ResourcePoolsBuilder clinicalDataCacheResourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder();
clinicalDataCacheResourcePoolsBuilder = clinicalDataCacheResourcePoolsBuilder.heap(clinicalDataCacheHeapSize, MemoryUnit.MB);
clinicalDataCacheResourcePoolsBuilder = clinicalDataCacheResourcePoolsBuilder.disk(clinicalDataCacheDiskSize, MemoryUnit.MB);
```
After initialzing the `ResourcePoolsBuilder`, create a CacheConfiguration for the new cache using the new `ResourcePoolsBuilder` just created.
```
CacheConfiguration<Object, Object> clinicalDataCacheConfiguration = xmlConfiguration.newCacheConfigurationBuilderFromTemplate("RepositoryCacheTemplate", 
Object.class, Object.class, clinicalDataCacheResourcePoolsBuilder)
.withSizeOfMaxObjectGraph(Long.MAX_VALUE)
.withSizeOfMaxObjectSize(Long.MAX_VALUE, MemoryUnit.B)
.build();
```
Finally, add the new `CacheConfiguration` to the map of managed caches with a name for the cache. 
```
caches.put("ClinicalDataCache", ClinicalDataCacheConfiguration);
```

You also need to create a new cache resolver in [EhCacheConfig.java](../persistence/persistence-api/src/main/java/org/cbioportal/persistence/config/EhCacheConfig.java):
```
@Bean
public NamedCacheResolver generalRepositoryCacheResolver() {
    return new NamedCacheResolver(cacheManager(), "GeneralRepositoryCache");
}
```

The `@Cacheable` annotation must also be added (or adjusted) to function declarations to indicate which functions are to be cached. Those might look like this example:
``` 
@Cacheable(cacheResolver = "clinicalDataCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
public String getDataFromClinicalDataRepository(String param) {}
```

Additionally, new properties for setting cache sizes should be added to `portal.properties` and loaded into the [CustomEhcachingProvider](../persistence/persistence-api/src/main/java/org/cbioportal/persistence/util/CustomEhcachingProvider.java). Alternatively, values may be hardcoded directly inside [CustomEhcachingProvider](../persistence/persistence-api/src/main/java/org/cbioportal/persistence/util/CustomEhcachingProvider.java).

For more information on cache templates and the Ehcache xml configuration file, refer to the documentation [here](https://www.ehcache.org/documentation/3.7/xml.html). 

For more information on linking caches to functions, refer to the documentation [here](https://spring.io/guides/gs/caching/).

## User-authorization cache

In addition to the above-mentioned Spring-managed caches, cBioPortal maintains a separate cache that holds references to
sample lists, molecular profiles and cancer studies. This user-authorization cache is used to establish
whether a user has access to the data of a particular sample list or molecular profile based on study-level permissions.

By default, the user-authorization cache is implemented as a HashMap that is populated when cBioPortal is started. This
implementation allows for very fast response times of user-permission evaluation.

The user-authorization cache can be delegated to the Spring-managed caching solution by setting the [cache.cache-map-utils.spring-managed](portal.properties-Reference.md#externalize-study-data-for-user-authorization-evaluation)
to _true_. Depending on the implementation, this may add a delay to any data request that is caused by the additional consultation
of the external cache. This configuration should only be used where a central caching solution is required or no
instance/container-specific local caches are allowed. For example, cache eviction via the `api/cache` endpoint in a Kubernetes
deployment of cBioPortal where multiple pods/containers that represent a single cBioPortal instance is possible with a
Spring-managed user-authorization cache because a call to this endpoint in a single pod/container invalidates Redis caches
for the entire deployment thereby preventing inconsistent state of user-authorization caches between pods.