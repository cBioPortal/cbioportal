package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.GenesetMolecularAlteration;
import org.cbioportal.model.GeneMolecularAlteration;

import org.springframework.cache.annotation.Cacheable;

public interface MolecularDataRepository {

    @Cacheable("GeneralRepositoryCache")
    String getCommaSeparatedSampleIdsOfMolecularProfile(String molecularProfileId);

    @Cacheable("GeneralRepositoryCache")
    List<String> getCommaSeparatedSampleIdsOfMolecularProfiles(List<String> molecularProfileIds);

    @Cacheable("GeneralRepositoryCache")
    List<GeneMolecularAlteration> getGeneMolecularAlterations(String molecularProfileId, List<Integer> entrezGeneIds,
                                                              String projection);

    @Cacheable("GeneralRepositoryCache")
    Iterable<GeneMolecularAlteration> getGeneMolecularAlterationsIterable(String molecularProfileId, List<Integer> entrezGeneIds,
                                                                          String projection);

    @Cacheable("GeneralRepositoryCache")
    List<GeneMolecularAlteration> getGeneMolecularAlterationsInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                                         List<Integer> entrezGeneIds,
                                                                                         String projection);

    @Cacheable("GeneralRepositoryCache")
    List<GenesetMolecularAlteration> getGenesetMolecularAlterations(String molecularProfileId, List<String> genesetIds,
                                                                    String projection);

}
