package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import java.util.Set;
import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.legacy.model.GeneMolecularAlteration;
import org.cbioportal.legacy.model.GenericAssayMolecularAlteration;
import org.cbioportal.legacy.model.GenesetMolecularAlteration;
import org.cbioportal.legacy.model.MolecularProfileSamples;

public interface MolecularDataMapper {

  List<MolecularProfileSamples> getCommaSeparatedSampleIdsOfMolecularProfiles(
      Set<String> molecularProfileIds);

  List<GeneMolecularAlteration> getGeneMolecularAlterations(
      String molecularProfileId, List<Integer> entrezGeneIds, String projection);

  Cursor<GeneMolecularAlteration> getGeneMolecularAlterationsIter(
      String molecularProfileId, List<Integer> entrezGeneIds, String projection);

  // Same as getGeneMolecularAlterationsIter above, except assumes that
  // entrezGeneIds is null or empty AND projection is "SUMMARY"
  Cursor<GeneMolecularAlteration> getGeneMolecularAlterationsIterFast(String molecularProfileId);

  List<GeneMolecularAlteration> getGeneMolecularAlterationsInMultipleMolecularProfiles(
      Set<String> molecularProfileIds, List<Integer> entrezGeneIds, String projection);

  List<GeneMolecularAlteration> getGeneMolecularAlterationsInMultipleMolecularProfilesClickHouse(
      Set<String> molecularProfileIds, List<Integer> entrezGeneIds);

  List<GenesetMolecularAlteration> getGenesetMolecularAlterations(
      String molecularProfileId, List<String> genesetIds, String projection);

  List<GenericAssayMolecularAlteration> getGenericAssayMolecularAlterations(
      String molecularProfileId, List<String> stableIds, String projection);

  Cursor<GenericAssayMolecularAlteration> getGenericAssayMolecularAlterationsIter(
      String molecularProfileId, List<String> stableIds, String projection);
}
