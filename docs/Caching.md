# Backend Caching 
cBioPortal provides the option of caching information on the backend to improve performance. Without caching, every time a request is received by the backend, a query is sent to the database system for information, and the returned data is processed to construct a response. This may lead to performance issues as the entire process can be rather costly, especially for queries on larger studies. With caching turned on, query responses can be taken directly from the cache if they have already been constructed. They would only be constructed for the initial query.

## Cache Configuration
The portal is configured to use Ehcache for backend caching. Ehcache supports a hybrid (disk + heap), disk-only, and heap-only mode; this, along with additional specifications such as cache size and location, are set inside `portal.properties`(more information [here](portal.properties-Reference.md#ehcache-settings)). 
 
## Creating additional caches
Cache initialization is handled inside the [CustomEhCachingProvider](../persistence/persistence-api/src/main/java/org/cbioportal/persistence/util/CustomEhCachingProvider.java). The default configuration initializes two seperate caches; however, you may wish to introduce new caches for different datatypes. To create additional caches (e.g creating a cache specifically for clinical data), a new cache must be added to the CustomEhCachingProvider. 

Within the `CustomEhCachingProvider`, initialize a new `ResourcePoolsBuilder` for the new cache and set the resources accordingly. 
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
The `@Cacheable` annotation must also be added (or adjusted) to function declarations to indicate which functions are to be cached. Those might look like this example:
``` 
@Cacheable(cacheNames = "ClinicalDataCache", condition = "@cacheEnabledConfig.getEnabled()")
public String getDataFromClinicalDataRepository(String param) {}
```
Additionally, new properties for setting cache sizes should be added to `portal.properties` and loaded into the [CustomEhCachingProvider](../persistence/persistence-api/src/main/java/org/cbioportal/persistence/util/CustomEhCachingProvider.java). Alternatively, values may be hardcoded directly inside [CustomEhCachingProvider](../persistence/persistence-api/src/main/java/org/cbioportal/persistence/util/CustomEhCachingProvider.java).

For more information on cache templates and the Ehcache xml configuration file, refer to the documentation [here](https://www.ehcache.org/documentation/3.7/xml.html). 

For more information on linking caches to functions, refer to the documentation [here](https://spring.io/guides/gs/caching/).
