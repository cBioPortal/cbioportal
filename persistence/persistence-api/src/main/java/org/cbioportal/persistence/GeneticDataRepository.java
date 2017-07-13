package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.GenesetGeneticAlteration;
import org.cbioportal.model.GeneGeneticAlteration;

public interface GeneticDataRepository {

    String getCommaSeparatedSampleIdsOfGeneticProfile(String geneticProfileId);

    List<GeneGeneticAlteration> getGeneGeneticAlterations(String geneticProfileId, List<Integer> entrezGeneIds,
                                                          String projection);

    List<GenesetGeneticAlteration> getGenesetGeneticAlterations(String geneticProfileId, List<String> genesetIds,
                                                                String projection);
}
