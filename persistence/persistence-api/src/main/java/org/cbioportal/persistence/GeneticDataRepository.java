package org.cbioportal.persistence;

import org.cbioportal.model.GeneticAlteration;

import java.util.List;

public interface GeneticDataRepository {

    String getCommaSeparatedSampleIdsOfGeneticProfile(String geneticProfileId);

    List<GeneticAlteration> getGeneticAlterations(String geneticProfileId, List<Integer> entrezGeneIds);
}
