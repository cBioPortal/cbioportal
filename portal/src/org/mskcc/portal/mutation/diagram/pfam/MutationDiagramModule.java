package org.mskcc.portal.mutation.diagram.pfam;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoMutation;
import org.mskcc.portal.mutation.diagram.CgdsIdMappingService;
import org.mskcc.portal.mutation.diagram.CgdsMutationService;
import org.mskcc.portal.mutation.diagram.IdMappingService;
import org.mskcc.portal.mutation.diagram.MutationService;

import com.google.common.cache.CacheLoader;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Mutation diagram package module.
 */
public final class MutationDiagramModule extends AbstractModule {

    @Override 
    protected void configure() {
        bind(FeatureService.class).to(CacheFeatureService.class).in(Singleton.class);
        bind(IdMappingService.class).to(CgdsIdMappingService.class).in(Singleton.class);
        bind(MutationService.class).to(CgdsMutationService.class).in(Singleton.class);
    }

    @Provides
    ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return objectMapper;
    }

    @Provides
    CacheLoader<String, List<Sequence>> createCacheLoader(final ObjectMapper objectMapper) {
        return new PfamGraphicsCacheLoader(objectMapper);
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