package org.mskcc.portal.mutation.diagram;

import java.util.List;

import com.google.common.cache.CacheLoader;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoException;
//import org.mskcc.cgds.dao.DaoIdMapping;
import org.mskcc.cgds.dao.DaoMutation;

/**
 * Mutation diagram package module.
 */
public final class MutationDiagramModule extends AbstractModule {

    @Override 
    protected void configure() {
        bind(DomainService.class).to(CacheDomainService.class).in(Singleton.class);
        bind(IdMappingService.class).to(CgdsIdMappingService.class).in(Singleton.class);
        bind(MutationService.class).to(CgdsMutationService.class).in(Singleton.class);
    }

    @Provides
    CacheLoader<String, List<Domain>> createCacheLoader() {
        return new PfamGraphicsCacheLoader();
    }

    @Provides
    DaoGeneOptimized createDaoGeneOptimized() throws DaoException {
        return DaoGeneOptimized.getInstance();
    }

    @Provides
    DaoMutation createDaoMutation() throws DaoException {
        return DaoMutation.getInstance();
    }
}