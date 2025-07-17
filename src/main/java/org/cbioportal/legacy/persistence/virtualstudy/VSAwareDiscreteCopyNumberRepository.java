package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import java.util.function.BiFunction;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.DiscreteCopyNumberData;
import org.cbioportal.legacy.model.GeneFilterQuery;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.DiscreteCopyNumberRepository;
import org.cbioportal.legacy.web.parameter.Projection;

public class VSAwareDiscreteCopyNumberRepository implements DiscreteCopyNumberRepository {

  private final VirtualizationService virtualizationService;
  private final DiscreteCopyNumberRepository discreteCopyNumberRepository;
  private final VSAwareSampleListRepository sampleListRepository;

  public VSAwareDiscreteCopyNumberRepository(
      VirtualizationService virtualizationService,
      DiscreteCopyNumberRepository discreteCopyNumberRepository,
      VSAwareSampleListRepository sampleListRepository) {
    this.virtualizationService = virtualizationService;
    this.discreteCopyNumberRepository = discreteCopyNumberRepository;
    this.sampleListRepository = sampleListRepository;
  }

  @Override
  public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMolecularProfileBySampleListId(
      String molecularProfileId,
      String sampleListId,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes,
      String projection) {
    SampleList sampleList = sampleListRepository.getSampleList(sampleListId);
    return fetchDiscreteCopyNumbersInMolecularProfile(
        molecularProfileId, sampleList.getSampleIds(), entrezGeneIds, alterationTypes, projection);
  }

  @Override
  public BaseMeta getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(
      String molecularProfileId,
      String sampleListId,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        getDiscreteCopyNumbersInMolecularProfileBySampleListId(
                molecularProfileId,
                sampleListId,
                entrezGeneIds,
                alterationTypes,
                Projection.ID.name())
            .size());
    return baseMeta;
  }

  @Override
  public List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInMolecularProfile(
      String molecularProfileId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes,
      String projection) {

    return virtualizationService.handleMolecularData(
        molecularProfileId,
        sampleIds,
        DiscreteCopyNumberData::getMolecularProfileId,
        DiscreteCopyNumberData::getSampleId,
        (mpid, sids) ->
            discreteCopyNumberRepository.fetchDiscreteCopyNumbersInMolecularProfile(
                mpid, sids, entrezGeneIds, alterationTypes, projection),
        this::virtualizeDiscreteCopyNumber);
  }

  @Override
  public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes,
      String projection) {
    return handleDiscreteCopyNumberData(
        molecularProfileIds,
        sampleIds,
        (mpids, sids) ->
            discreteCopyNumberRepository.getDiscreteCopyNumbersInMultipleMolecularProfiles(
                mpids, sids, entrezGeneIds, alterationTypes, projection));
  }

  @Override
  public List<DiscreteCopyNumberData>
      getDiscreteCopyNumbersInMultipleMolecularProfilesByGeneQueries(
          List<String> molecularProfileIds,
          List<String> sampleIds,
          List<GeneFilterQuery> geneFilterQuery,
          String projection) {
    return handleDiscreteCopyNumberData(
        molecularProfileIds,
        sampleIds,
        (mpids, sids) ->
            discreteCopyNumberRepository
                .getDiscreteCopyNumbersInMultipleMolecularProfilesByGeneQueries(
                    mpids, sids, geneFilterQuery, projection));
  }

  private List<DiscreteCopyNumberData> handleDiscreteCopyNumberData(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      BiFunction<List<String>, List<String>, List<DiscreteCopyNumberData>> fetch) {
    return virtualizationService.handleMolecularData(
        molecularProfileIds,
        sampleIds,
        DiscreteCopyNumberData::getMolecularProfileId,
        DiscreteCopyNumberData::getSampleId,
        fetch,
        this::virtualizeDiscreteCopyNumber);
  }

  private DiscreteCopyNumberData virtualizeDiscreteCopyNumber(
      MolecularProfile molecularProfile, DiscreteCopyNumberData dcn) {
    DiscreteCopyNumberData virtualDcn = new DiscreteCopyNumberData();
    virtualDcn.setStudyId(molecularProfile.getCancerStudyIdentifier());
    virtualDcn.setSampleId(dcn.getSampleId());
    virtualDcn.setEntrezGeneId(dcn.getEntrezGeneId());
    virtualDcn.setAlteration(dcn.getAlteration());
    virtualDcn.setPatientId(dcn.getPatientId());
    virtualDcn.setMolecularProfileId(molecularProfile.getStableId());
    virtualDcn.setDriverFilter(dcn.getDriverFilter());
    virtualDcn.setDriverFilterAnnotation(dcn.getDriverFilterAnnotation());
    virtualDcn.setDriverTiersFilter(dcn.getDriverTiersFilter());
    virtualDcn.setDriverTiersFilterAnnotation(dcn.getDriverTiersFilterAnnotation());
    virtualDcn.setGene(dcn.getGene());
    return virtualDcn;
  }

  @Override
  public BaseMeta fetchMetaDiscreteCopyNumbersInMolecularProfile(
      String molecularProfileId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        fetchDiscreteCopyNumbersInMolecularProfile(
                molecularProfileId, sampleIds, entrezGeneIds, alterationTypes, Projection.ID.name())
            .size());
    return baseMeta;
  }

  @Override
  public List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(
      String molecularProfileId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterations) {

    Pair<String, List<String>> pair =
        virtualizationService.toMaterializedMolecularProfileIds(molecularProfileId, sampleIds);
    return discreteCopyNumberRepository
        .getSampleCountByGeneAndAlterationAndSampleIds(
            pair.getKey(), pair.getRight(), entrezGeneIds, alterations)
        .stream()
        .toList();
  }
}
