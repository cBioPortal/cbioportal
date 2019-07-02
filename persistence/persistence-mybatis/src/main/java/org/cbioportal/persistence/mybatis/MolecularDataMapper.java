package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GenesetMolecularAlteration;

import java.util.List;
import org.apache.ibatis.cursor.Cursor;

public interface MolecularDataMapper {

    List<String> getCommaSeparatedSampleIdsOfMolecularProfiles(List<String> molecularProfileIds);

    Cursor<GeneMolecularAlteration> getGeneMolecularAlterations(String molecularProfileId, List<Integer> entrezGeneIds,
                                                                String projection);

    List<GeneMolecularAlteration> getGeneMolecularAlterationsInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                                         List<Integer> entrezGeneIds,
                                                                                         String projection);

	List<GenesetMolecularAlteration> getGenesetMolecularAlterations(String molecularProfileId, List<String> genesetIds,
                                                                    String projection);
}
