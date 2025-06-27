package org.cbioportal.legacy.persistence.mybatis;

import java.util.*;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.GeneMolecularAlteration;
import org.cbioportal.legacy.model.GenericAssayMolecularAlteration;
import org.cbioportal.legacy.model.GenesetMolecularAlteration;
import org.cbioportal.legacy.model.MolecularProfileSamples;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.persistence.MolecularDataRepository;
import org.cbioportal.legacy.persistence.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MolecularDataMyBatisRepository implements MolecularDataRepository {

  @Autowired private MolecularDataMapper molecularDataMapper;
  // TODO this is not conventional to inject a repository into another repository, but it is a way
  // to translate internal sample IDs to stable sample IDs without changing multiple layers of code.
  @Autowired private SampleRepository sampleRepository;

  @Override
  public List<String> getStableSampleIdsOfMolecularProfile(String molecularProfileId) {
    return stableSampleIdsOfMolecularProfilesMap(Set.of(molecularProfileId))
        .get(molecularProfileId);
  }

  @Override
  public Map<String, List<String>> stableSampleIdsOfMolecularProfilesMap(
      Set<String> molecularProfileIds) {
    Map<String, List<String>> result = new LinkedHashMap<>();
    for (MolecularProfileSamples molecularProfileSamples :
        molecularDataMapper.getCommaSeparatedSampleIdsOfMolecularProfiles(molecularProfileIds)) {
      String molecularProfileId = molecularProfileSamples.getMolecularProfileId();
      List<Integer> internalSampleIds =
          Arrays.stream(molecularProfileSamples.getSplitSampleIds())
              .map(Integer::parseInt)
              .toList();
      Map<Integer, String> internalToExternalSampleIdMapping =
          sampleRepository.getSamplesByInternalIds(internalSampleIds).stream()
              .collect(Collectors.toMap(Sample::getInternalId, Sample::getStableId));
      result
          .computeIfAbsent(molecularProfileId, k -> new ArrayList<>())
          .addAll(internalSampleIds.stream().map(internalToExternalSampleIdMapping::get).toList());
    }
    return result;
  }

  @Override
  public List<GeneMolecularAlteration> getGeneMolecularAlterations(
      String molecularProfileId, List<Integer> entrezGeneIds, String projection) {

    return molecularDataMapper.getGeneMolecularAlterations(
        molecularProfileId, entrezGeneIds, projection);
  }

  @Override
  // In order to return a cursor/iterator to the service layer, we need a transaction setup in the
  // service
  // layer. Currently, the bottom stackframe is CoExpressionService:getCoExpressions.  It is there
  // where
  // you will find the transaction created.
  public Iterable<GeneMolecularAlteration> getGeneMolecularAlterationsIterable(
      String molecularProfileId, List<Integer> entrezGeneIds, String projection) {

    return molecularDataMapper.getGeneMolecularAlterationsIter(
        molecularProfileId, entrezGeneIds, projection);
  }

  @Override
  public Iterable<GeneMolecularAlteration> getGeneMolecularAlterationsIterableFast(
      String molecularProfileId) {

    return molecularDataMapper.getGeneMolecularAlterationsIterFast(molecularProfileId);
  }

  @Override
  public List<GeneMolecularAlteration> getGeneMolecularAlterationsInMultipleMolecularProfiles(
      Set<String> molecularProfileIds, List<Integer> entrezGeneIds, String projection) {

    return molecularDataMapper.getGeneMolecularAlterationsInMultipleMolecularProfiles(
        molecularProfileIds, entrezGeneIds, projection);
  }

  @Override
  public List<GenesetMolecularAlteration> getGenesetMolecularAlterations(
      String molecularProfileId, List<String> genesetIds, String projection) {

    return molecularDataMapper.getGenesetMolecularAlterations(
        molecularProfileId, genesetIds, projection);
  }

  @Override
  public List<GenericAssayMolecularAlteration> getGenericAssayMolecularAlterations(
      String molecularProfileId, List<String> stableIds, String projection) {
    return molecularDataMapper.getGenericAssayMolecularAlterations(
        molecularProfileId, stableIds, projection);
  }

  @Override
  public Iterable<GenericAssayMolecularAlteration> getGenericAssayMolecularAlterationsIterable(
      String molecularProfileId, List<String> stableIds, String projection) {
    return molecularDataMapper.getGenericAssayMolecularAlterationsIter(
        molecularProfileId, stableIds, projection);
  }
}
