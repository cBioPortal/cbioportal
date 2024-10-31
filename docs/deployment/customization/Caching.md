# Backend Caching

cBioPortal provides the option of caching information on the backend to improve performance. Without caching, every time
a request is received by the backend, a query is sent to the database system for information, and the returned data is
processed to construct a response. This may lead to performance issues as the entire process can be rather costly,
especially for queries on larger studies. With caching turned on, query responses can be taken directly from the cache
if they have already been constructed. They would only be constructed for the initial query.

## Cache Configuration

The portal is configured to use Ehcache or Redis for backend caching. Ehcache supports a hybrid (disk + heap),
disk-only, and heap-only mode. Redis stores the cache in memory and periodically writes the updated data to disk. Cache
configuration is specified inside `application.properties`(more
information [here](application.properties-Reference.md#cache-settings)).

## Creating additional caches

The default configuration initializes two separate caches; however, you may wish to introduce new caches for different
datatypes. Please see the [Redis](#redis) and [Ehcache](#ehcache) sections to see how to set up a new cache in whichever
system you are using.

### Redis

Cache initialization is handled inside
the [CustomRedisCachingProvider](https://github.com/cBioPortal/cbioportal/blob/master/persistence/persistence-api/src/main/java/org/cbioportal/persistence/util/CustomRedisCachingProvider.java)
. To create additional caches (e.g creating a cache specifically for clinical data), new code must be added to
the `CustomRedisCachingProvider`.

Within the `CustomRedisCachingProvider`, create your new cache using the `CacheManager`. The appName must be prepended
to your cache name.

```
manager.createCache(appName + "ClinicalDataCache", config);
```

You also need to create a new cache resolver
in [RedisConfig.java](https://github.com/cBioPortal/cbioportal/blob/master/src/main/java/org/cbioportal/persistence/config/RedisConfig.java):

```
@Bean
public CacheResolver generalRepositoryCacheResolver() {
    return new NamedCacheResolver(cacheManager(), ${redis.name:cbioportal} + "GeneralRepositoryCache");
}
```

The `@Cacheable` annotation must also be added (or adjusted) to function declarations to indicate which functions are to
be cached. Those might look like this example:

``` 
@Cacheable(cacheResolver = "clinicalDataCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
public String getDataFromClinicalDataRepository(String param) {}
```

For more information on linking caches to functions, refer to the
documentation [here](https://spring.io/guides/gs/caching/).

### Ehcache

Within the `CustomEhcachingProvider`, initialize a new `ResourcePoolsBuilder` for the new cache and set the resources
accordingly.

```
ResourcePoolsBuilder clinicalDataCacheResourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder();
clinicalDataCacheResourcePoolsBuilder = clinicalDataCacheResourcePoolsBuilder.heap(clinicalDataCacheHeapSize, MemoryUnit.MB);
clinicalDataCacheResourcePoolsBuilder = clinicalDataCacheResourcePoolsBuilder.disk(clinicalDataCacheDiskSize, MemoryUnit.MB);
```

After initialzing the `ResourcePoolsBuilder`, create a CacheConfiguration for the new cache using the
new `ResourcePoolsBuilder` just created.

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

You also need to create a new cache resolver
in [EhCacheConfig.java](https://github.com/cBioPortal/cbioportal/blob/master/persistence-api/src/main/java/org/cbioportal/persistence/config/EhCacheConfig.java):

```
@Bean
public NamedCacheResolver generalRepositoryCacheResolver() {
    return new NamedCacheResolver(cacheManager(), "GeneralRepositoryCache");
}
```

The `@Cacheable` annotation must also be added (or adjusted) to function declarations to indicate which functions are to
be cached. Those might look like this example:

``` 
@Cacheable(cacheResolver = "clinicalDataCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
public String getDataFromClinicalDataRepository(String param) {}
```

Additionally, new properties for setting cache sizes should be added to `application.properties` and loaded into
the [CustomEhcachingProvider](https://github.com/cBioPortal/cbioportal/blob/master/persistence/persistence-api/src/main/java/org/cbioportal/persistence/util/CustomEhcachingProvider.java)
. Alternatively, values may be hardcoded directly
inside [CustomEhcachingProvider](https://github.com/cBioPortal/cbioportal/blob/master/persistence/persistence-api/src/main/java/org/cbioportal/persistence/util/CustomEhcachingProvider.java)
.

For more information on cache templates and the Ehcache xml configuration file, refer to the
documentation [here](https://www.ehcache.org/documentation/3.7/xml.html).

For more information on linking caches to functions, refer to the
documentation [here](https://spring.io/guides/gs/caching/).


## User-authorization cache

In addition to the above-mentioned Spring-managed caches, cBioPortal maintains a separate cache that holds references to
sample lists, molecular profiles and cancer studies. This user-authorization cache is used to establish
whether a user has access to the data of a particular sample list or molecular profile based on study-level permissions.

By default, the user-authorization cache is implemented as a HashMap that is populated when cBioPortal is started. This
implementation allows for very fast response times of user-permission evaluation.

The user-authorization cache can be delegated to the Spring-managed caching solution by setting the [cache.cache-map-utils.spring-managed](application.properties-Reference.md#cache-settings)
to _true_. Depending on the implementation, this may add a delay to any data request that is caused by the additional consultation
of the external cache. This configuration should only be used where a central caching solution is required or no
instance/container-specific local caches are allowed. For example, cache eviction via the `api/cache` endpoint in a Kubernetes
deployment of cBioPortal where multiple pods/containers that represent a single cBioPortal instance is possible with a
Spring-managed user-authorization cache because a call to this endpoint in a single pod/container invalidates Redis caches
for the entire deployment thereby preventing inconsistent state of user-authorization caches between pods.


## Cache eviction

When the database is updated (e.g new studies loaded, existing study updated, new gene panel imported) the caches of a
cBioPortal instance should be updated. One way is to restart the cBioPortal spring application. When using Redis, this
will work only when `redis.clear_on_startup` is not set to _false_.

Alternatively, caches can be cleared (a.k.a. _evicted_) by calling the _/api/cache_ endpoint. Advantage of the cache
eviction end point is that user-sessions remain undisturbed since the portal instance is not restarted. By default the
cache eviction enpoint is disabled and can be enabled by setting `cache.endpoint.enabled` to _true_. The endpoint is
secured by a secret API key that can be customized with the `cache.endpoint.api-key` property. Caches are evicted by
making a DELETE request to the endoint while passing the API key in the `X-API-KEY` header. When using _curl_ use the
following command (replace the API key for the value configured in _application.properties_):

```
curl -X DELETE http://my-portal-url.org/api/cache  -H 'X-API-KEY: fd15f1ae-66f2-4b8a-8d54-fb899b03557e'
```

### Cache eviction after cancer study updates

When a study is added, deleted or updated, a more selective cache eviction strategy is possible, where only affected
cached data is evicted. This more selective cache eviction is triggered by calling the `/api/cache/{studyId}` endpoint
where _{studyId}_ is the _cancer_study_identifier_ stated in the meta-study.txt file. When using _curl_ use the
following command after update of a study with study identifier _my_cancer_study_ (replace the API key for the value
configured in _application.properties_):

```
curl -X DELETE http://my-portal-url.org/api/cache/my_cancer_study  -H 'X-API-KEY: fd15f1ae-66f2-4b8a-8d54-fb899b03557e'
```

:important: This endpoint can ony be used when adding/deleting/updating a study. When data related to gene panels or
gene sets is updated, all caches should be evicted with a call to `/api/cache`.

#### How does study-specific cache eviction work?

##### Structure of cache keys

The caching solutions integrated with cBioPortal (EHCache and Redis) store data as key-value pairs. Each key represents
a method call signature that contains the Java class name, the method name and a serialized representation of all method
arguments. For instance this is the key for a call to the CancerTypMyBatisRepository.getAllCancerTypes() method with
arguments _[ SUMMARY, 10000000, 0, null, ASC]_:

```
CancerTypeMyBatisRepository_getAllCancerTypes_"SUMMARY"_10000000_0_null_"ASC"
```

Cached data that relates to a specific study can be recognized by the occurrence of the cancer study identifier anywhere
in the method arguments. The study identifier can occur in the method arguments because it is passed as argument itself,
like here for a study with identifier _study_es_0_:

```
SignificantCopyNumberRegionMyBatisRepository_getSignificantCopyNumberRegions_"study_es_0"_"SUMMARY"_null_null_null_null
```

Alternatively the study identifier is present as the prefix of referenced study entities. For example, this is the
request for all molecular profiles:

```
MolecularProfileMyBatisRepository_getMolecularProfiles_["study_es_0_mrna_median_Zscores","study_es_0_log2CNA","study_es_0_fusion","study_es_0_treatment_ec50","study_es_0_mutational_signature","study_es_0_gsva_pvalues","study_es_0_mutations","study_es_0_methylation_hm27","study_es_0_gistic","study_es_0_generic_assay_patient_test","study_es_0_treatment_ic50","study_es_0_mrna","study_es_0_gsva_scores"]_"SUMMARY""
```

#### Cache eviction rules

When a study is added, deleted or updated, all caches are evicted where the respective key meets any of these
requirements:

1. The key contains the cancer study identifier of the study that is added, deleted or updated.
2. The key does not contain the cancer study identifier of any study present in the database.

The rationale behind rule 1. is that when a key references data for the affected study it points to potentially outdated
data and its associated cache should be evicted. The rationale behind rule 2. is that any key that does not reference
data for any study potentially points to data derived from all studies in the database, including the affected study,
and its associated cache should be evicted. Because not every key without study identifiers necessarily points to study
related data, this rule is overly broad. At the moment of this writing we were unable to implement reliable methods that
would further specify such keys. This might be a start-off point for future optimizations.


