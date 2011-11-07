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
        domains.put("P04637", new Domain("FOO", 1, 100));
        domains.put("P04637", new Domain("BAR", 200, 250));
        domains.put("P04637", new Domain("BAZ", 350, 400));
        domains.put("P04637", new Domain("QUX", 500, 700));
        domains.put("Q00987", new Domain("FOO", 1, 50));
        domains.put("Q00987", new Domain("BAR", 190, 270));
        domains.put("Q00987", new Domain("BAZ", 300, 470));
        domains.put("Q00987", new Domain("QUX", 530, 700));
        domains.put("O15151", new Domain("FOO", 1, 90));
        domains.put("O15151", new Domain("BAR", 200, 290));
        domains.put("O15151", new Domain("BAZ", 300, 425));
        domains.put("O15151", new Domain("QUX", 475, 700));
        return domains;
    }

    @Provides
    Multimap<String, String> createUniProtIds() {
        Multimap<String, String> uniProtIds = HashMultimap.create();
        uniProtIds.put("DVL1", "DVL1_HUMAN");
        uniProtIds.put("DVL1", "O14640");
        uniProtIds.put("TP53", "P04637");
        uniProtIds.put("MDM2", "Q00987");
        uniProtIds.put("MDM4", "O15151");
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
        mutations.put("TP53", new Mutation(24, 4));
        mutations.put("TP53", new Mutation(218, 3));
        mutations.put("TP53", new Mutation(330, 1));
        mutations.put("TP53", new Mutation("Y398V", 398, 2));
        mutations.put("TP53", new Mutation("K512G", 512, 8));
        mutations.put("MDM2", new Mutation(69, 4));
        mutations.put("MDM2", new Mutation(213, 3));
        mutations.put("MDM2", new Mutation(311, 1));
        mutations.put("MDM2", new Mutation("Y331V", 331, 2));
        mutations.put("MDM2", new Mutation("K550G", 550, 8));
        mutations.put("MDM4", new Mutation(18, 4));
        mutations.put("MDM4", new Mutation(125, 3));
        mutations.put("MDM4", new Mutation(135, 1));
        mutations.put("MDM4", new Mutation("Y330V", 330, 2));
        mutations.put("MDM4", new Mutation("K499G", 499, 8));
        return mutations;
    }
}