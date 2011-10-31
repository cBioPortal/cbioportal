package org.mskcc.portal.mutation.diagram;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ListMultimap;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Mock mutation diagram package module.
 */
public final class MockMutationDiagramModule extends AbstractModule {

    @Override 
    protected void configure() {
        bind(DomainService.class).to(ListMultimapDomainService.class).in(Singleton.class);
        bind(IdMappingService.class).to(MultimapIdMappingService.class).in(Singleton.class);
        bind(MutationService.class).to(ListMultimapMutationService.class).in(Singleton.class);
    }

    @Provides
    ListMultimap<String, Domain> createDomains() {
        ListMultimap<String, Domain> domains = ArrayListMultimap.create();
        domains.put("O14640", new Domain("DIX", 1, 85));
        domains.put("O14640", new Domain("DVL1", 144, 215));
        domains.put("O14640", new Domain("PDZ", 251, 336));
        domains.put("O14640", new Domain("DEP", 428, 497));
        domains.put("DVL1_HUMAN", new Domain("DIX", 1, 85));
        domains.put("DVL1_HUMAN", new Domain("DVL1", 144, 215));
        domains.put("DVL1_HUMAN", new Domain("PDZ", 251, 336));
        domains.put("DVL1_HUMAN", new Domain("DEP", 428, 497));
        return domains;
    }

    @Provides
    Multimap<String, String> createUniProtIds() {
        Multimap<String, String> uniProtIds = HashMultimap.create();
        uniProtIds.put("DVL1", "DVL1_HUMAN");
        uniProtIds.put("DVL1", "O14640");
        return uniProtIds;
    }

    @Provides
    ListMultimap<String, Mutation> createMutations() {
        ListMultimap<String, Mutation> mutations = ArrayListMultimap.create();
        mutations.put("DVL1", new Mutation(42, 4));
        mutations.put("DVL1", new Mutation(197, 3));
        mutations.put("DVL1", new Mutation(233, 1));
        mutations.put("DVL1", new Mutation("Y335V", 335, 2));
        mutations.put("DVL1", new Mutation("K429G", 430, 8));
        return mutations;
    }
}