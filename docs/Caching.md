# Backend Caching 
cBioPortal provides the option of caching information on the backend to improve performance. Without caching, every time a request is received by the backend, a query is sent to the database system for information, and the returned data is processed to construct a response. This may lead to performance issues as the entire process can be rather costly, especially for queries on larger studies. With caching turned on, query responses can be taken directly from the cache if they have already been constructed. They would only be constructed for the initial query.

## Cache Configuration
The portal is configured to use Ehcache for backend caching; caching configuration is specified inside an xml file under `persistence/persistence-api/src/main/resources`. The default configuration is a hybrid (disk + heap); however, Ehcache supports both a disk-only and heap-only mode. Refer to comments in `ehcache.xml` to switch between cache configurations. The configuration files also specify which caches to create. Additional specifications such as cache size and location are set inside `portal.properties` (more information [here](portal.properties-Reference.md#ehcache-settings)).
 
## Creating additional caches
The default configuration initializes two separate caches. However, you may wish to introduce new caches with different policies (e.g expiration policy) for different datatypes. To create additional caches (e.g creating a cache specifically for clinical data), a new cache must be added to the Ehcache xml configuration file. The `@Cacheable` annotation must also be added (or adjusted) to function declarations to indicate which functions are to be cached. Those might look like this example:
``` 
@Cacheable(cacheNames = "ClinicalDataCache", condition = "@cacheEnabledConfig.getEnabled()")
public String getDataFromClinicalDataRepository(String param) {}
```
Additionally, new properties for setting cache sizes should be added to `portal.properties`. Alternatively, values may also be hardcoded directly into the Ehcache xml configuration file. 

For more information on constructing an Ehcache xml configuration file, refer to the documentation [here](https://www.ehcache.org/documentation/3.7/xml.html). 

For more information on linking caches to functions, refer to the documentation [here](https://spring.io/guides/gs/caching/).
