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

You also need to create a new cache resolver in [applicationContext-rediscache.xml](../persistence/persistence-api/src/main/resources/applicationContext-rediscache.xml):
```
<bean id="clinicalDataCacheResolver" class="org.springframework.cache.interceptor.NamedCacheResolver">
    <constructor-arg index="0" ref="redisCacheManager"/>
    <constructor-arg index="1" value="${app.name:cbioportal}ClinicalDataCache"/>
</bean>
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

You also need to create a new cache resolver in [applicationContext-ehcache.xml](../persistence/persistence-api/src/main/resources/applicationContext-ehcache.xml):
```
<bean id="clinicalDataCacheResolver" class="org.springframework.cache.interceptor.NamedCacheResolver">
    <constructor-arg index="0" ref="ehcacheCacheManager"/>
    <constructor-arg index="1" value="ClinicalDataCache"/>
</bean>
```

The `@Cacheable` annotation must also be added (or adjusted) to function declarations to indicate which functions are to be cached. Those might look like this example:
``` 
@Cacheable(cacheResolver = "clinicalDataCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
public String getDataFromClinicalDataRepository(String param) {}
```

Additionally, new properties for setting cache sizes should be added to `portal.properties` and loaded into the [CustomEhcachingProvider](../persistence/persistence-api/src/main/java/org/cbioportal/persistence/util/CustomEhcachingProvider.java). Alternatively, values may be hardcoded directly inside [CustomEhcachingProvider](../persistence/persistence-api/src/main/java/org/cbioportal/persistence/util/CustomEhcachingProvider.java).

For more information on cache templates and the Ehcache xml configuration file, refer to the documentation [here](https://www.ehcache.org/documentation/3.7/xml.html). 

For more information on linking caches to functions, refer to the documentation [here](https://spring.io/guides/gs/caching/).
