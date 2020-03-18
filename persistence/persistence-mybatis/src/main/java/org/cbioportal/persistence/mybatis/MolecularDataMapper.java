package org.cbioportal.persistence.mybatis;


import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GenericAssayMolecularAlteration;
import org.cbioportal.model.GenesetMolecularAlteration;
import org.cbioportal.model.MolecularProfileSamples;

import java.util.List;
import org.apache.ibatis.cursor.Cursor;

public interface MolecularDataMapper {

    List<MolecularProfileSamples> getCommaSeparatedSampleIdsOfMolecularProfiles(List<String> molecularProfileIds);

    List<GeneMolecularAlteration> getGeneMolecularAlterations(String molecularProfileId, List<Integer> entrezGeneIds,
                                                              String projection);

    Cursor<GeneMolecularAlteration> getGeneMolecularAlterationsIter(String molecularProfileId, List<Integer> entrezGeneIds,
                                                                    String projection);

    List<GeneMolecularAlteration> getGeneMolecularAlterationsInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                                         List<Integer> entrezGeneIds, String projection);

    List<GenesetMolecularAlteration> getGenesetMolecularAlterations(String molecularProfileId, List<String> genesetIds,
                                                                    String projection);

    List<GenericAssayMolecularAlteration> getGenericAssayMolecularAlterations(String molecularProfileId, List<String> stableIds,
                                                                    String projection);
}
