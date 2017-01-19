package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneticAlteration;

import java.util.List;

public interface GeneticDataMapper {

    String getCommaSeparatedSampleIdsOfGeneticProfile(String geneticProfileId);

    List<GeneticAlteration> getGeneticAlterations(String geneticProfileId, List<Integer> entrezGeneIds);
}
