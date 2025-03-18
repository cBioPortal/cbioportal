package org.cbioportal.legacy.persistence;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneTableRepository {

	@Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
	String getGenetableVersion();

}
