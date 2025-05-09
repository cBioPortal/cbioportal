package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.DiscreteCopyNumberData;
import org.cbioportal.legacy.model.GeneFilterQuery;
import org.cbioportal.legacy.model.meta.BaseMeta;

public interface DiscreteCopyNumberMapper {

  List<DiscreteCopyNumberData> getDiscreteCopyNumbersBySampleListId(
      String molecularProfileId,
      String sampleListId,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes,
      String projection);

  BaseMeta getMetaDiscreteCopyNumbersBySampleListId(
      String molecularProfileId,
      String sampleListId,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes);

  List<DiscreteCopyNumberData> getDiscreteCopyNumbersBySampleIds(
      String molecularProfileId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes,
      String projection);

  List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes,
      String projection);

  List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfilesByGeneQueries(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      String projection,
      List<GeneFilterQuery> geneQueries);

  BaseMeta getMetaDiscreteCopyNumbersBySampleIds(
      String molecularProfileId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes);

  List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(
      String molecularProfileId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterations);
}
