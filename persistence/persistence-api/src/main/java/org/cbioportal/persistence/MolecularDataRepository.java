package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GenesetMolecularAlteration;
import org.cbioportal.model.TreatmentMolecularAlteration;

import org.springframework.cache.annotation.Cacheable;

public interface MolecularDataRepository {

    @Cacheable("RepositoryCache")
    String getCommaSeparatedSampleIdsOfMolecularProfile(String molecularProfileId);

    @Cacheable("RepositoryCache")
    List<String> getCommaSeparatedSampleIdsOfMolecularProfiles(List<String> molecularProfileIds);

    @Cacheable("RepositoryCache")
    List<GeneMolecularAlteration> getGeneMolecularAlterations(String molecularProfileId, List<Integer> entrezGeneIds,
                                                              String projection);

    @Cacheable("RepositoryCache")
    Iterable<GeneMolecularAlteration> getGeneMolecularAlterationsIterable(String molecularProfileId, List<Integer> entrezGeneIds,
                                                                          String projection);

    @Cacheable("RepositoryCache")
    List<GeneMolecularAlteration> getGeneMolecularAlterationsInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                                         List<Integer> entrezGeneIds,
                                                                                         String projection);
    @Cacheable("RepositoryCache")
    List<GenesetMolecularAlteration> getGenesetMolecularAlterations(String molecularProfileId, List<String> genesetIds,
                                                                    String projection);

    @Cacheable("RepositoryCache")
    List<TreatmentMolecularAlteration> getTreatmentMolecularAlterations(String molecularProfileId,
                                                                        List<String> treatmentIds, String projection);
}
