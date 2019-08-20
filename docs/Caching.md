# Backend Caching 
cBioPortal provides the option of caching information on the backend as a means for improving performance. Without caching, each query including repeated queries, will query the database, process the returned data, and construct a response. This may lead to performance issues as the entire process is rather costly, especially for queries on larger studies. With caching turned on, the query response will be stored during the initial query. If the same query is made, the response will be taken directly from cache instead of having to be reconstructed. 

## Cache Configuration
The portal is configured to use Ehcache for backend caching; caching configuration is
specified inside an xml file under `persistence/persistence-api/src/main/resources`. There are 
three different configurations available - `disk-only`, `heap-only`, and `mixed`. The configuration files 
specify which caches to create. Additional specifications such as cache size and location are set inside `portal.properties` (more information [here](portal.properties-Reference.md#ehcache-settings)).
 
 
 ## Creating additional caches
The default configuration initializes two separate caches. However, a user may wish to introduce new caches with different policies (e.g expiration policy) for different datatypes. To create additional caches (e.g creating a cache specifically for clinical data), a new cache must be added to the ehcache.xml configuration file. The `@Cacheable` annotation must also be added to function declarations to indicate which functions are to be cached. 
``` 
@Cacheable(ClinicalDataCache)
public String getDataFromClinicalDataRepository(String param) {}
```
Additionally, new properties for setting cache sizes should be added to `portal.properties`. Alternatively, values may also be hardcoded directly into the ehcache.xml configuration file. 

For more information on constructing an ehcache.xml configuration file, refer to the documentation [here](https://www.ehcache.org/documentation/3.7/xml.html). 

For more information on linking caches to functions, refer to the documentation [here](https://spring.io/guides/gs/caching/).
