package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.GenesetAlteration;
import org.cbioportal.model.GeneticAlteration;

public interface GeneticDataRepository {

    String getCommaSeparatedSampleIdsOfGeneticProfile(String geneticProfileId);

    List<GeneticAlteration> getGeneticAlterations(String geneticProfileId, List<Integer> entrezGeneIds, 
                                                  String projection);

	List<GenesetAlteration> getGenesetAlterations(String geneticProfileId, List<String> genesetIds, String string);
}
