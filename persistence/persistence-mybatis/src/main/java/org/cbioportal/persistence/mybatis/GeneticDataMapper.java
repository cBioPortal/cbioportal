package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneGeneticAlteration;
import org.cbioportal.model.GenesetGeneticAlteration;

import java.util.List;

public interface GeneticDataMapper {

    String getCommaSeparatedSampleIdsOfGeneticProfile(String geneticProfileId);

    List<GeneGeneticAlteration> getGeneGeneticAlterations(String geneticProfileId, List<Integer> entrezGeneIds, 
                                                  String projection);

	List<GenesetGeneticAlteration> getGenesetGeneticAlterations(String geneticProfileId, List<String> genesetIds,
			String projection);
}
