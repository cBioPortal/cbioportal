package org.cbioportal.legacy.service.impl;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.DiscreteCopyNumberData;
import org.cbioportal.legacy.model.GeneFilterQuery;
import org.cbioportal.legacy.model.GeneMolecularAlteration;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.model.MolecularDataCountItem;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.legacy.model.MolecularProfileSamples;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.DiscreteCopyNumberRepository;
import org.cbioportal.legacy.persistence.MolecularDataRepository;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.service.MolecularDataService;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.SampleService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class MolecularDataServiceImpl implements MolecularDataService {

  @Autowired private MolecularDataRepository molecularDataRepository;
  @Autowired private SampleService sampleService;
  @Autowired private MolecularProfileService molecularProfileService;
  @Autowired private SampleListRepository sampleListRepository;
  @Autowired private DiscreteCopyNumberRepository discreteCopyNumberRepository;

  @Override
  public List<GeneMolecularData> getMolecularData(
      String molecularProfileId,
      String sampleListId,
      List<Integer> entrezGeneIds,
      String projection)
      throws MolecularProfileNotFoundException {

    validateMolecularProfile(molecularProfileId);
    List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
    if (sampleIds.isEmpty()) {
      return Collections.emptyList();
    }
    return fetchMolecularData(molecularProfileId, sampleIds, entrezGeneIds, projection);
  }

  @Override
  public BaseMeta getMetaMolecularData(
      String molecularProfileId, String sampleListId, List<Integer> entrezGeneIds)
      throws MolecularProfileNotFoundException {

    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        getMolecularData(molecularProfileId, sampleListId, entrezGeneIds, "ID").size());
    return baseMeta;
  }

  @Override
  public List<GeneMolecularData> fetchMolecularData(
      String molecularProfileId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      String projection)
      throws MolecularProfileNotFoundException {

    validateMolecularProfile(molecularProfileId);
    List<GeneMolecularData> molecularDataList = new ArrayList<>();

    MolecularProfileSamples commaSeparatedSampleIdsOfMolecularProfile =
        molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfileId);
    if (commaSeparatedSampleIdsOfMolecularProfile == null) {
      return molecularDataList;
    }
    List<Integer> internalSampleIds =
        Arrays.stream(commaSeparatedSampleIdsOfMolecularProfile.getSplitSampleIds())
            .mapToInt(Integer::parseInt)
            .boxed()
            .collect(Collectors.toList());
    Map<Integer, Integer> internalSampleIdsMap = new HashMap<>();
    for (int lc = 0; lc < internalSampleIds.size(); lc++) {
      internalSampleIdsMap.put(internalSampleIds.get(lc), lc);
    }

    List<Sample> samples;
    if (sampleIds == null) {
      samples = sampleService.getSamplesByInternalIds(internalSampleIds);
    } else {
      MolecularProfile molecularProfile =
          molecularProfileService.getMolecularProfile(molecularProfileId);
      List<String> studyIds = new ArrayList<>();
      sampleIds.forEach(s -> studyIds.add(molecularProfile.getCancerStudyIdentifier()));
      samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");
    }

    List<GeneMolecularAlteration> molecularAlterations =
        molecularDataRepository.getGeneMolecularAlterations(
            molecularProfileId, entrezGeneIds, projection);

    for (Sample sample : samples) {
      Integer indexOfSampleId = internalSampleIdsMap.get(sample.getInternalId());
      if (indexOfSampleId != null) {
        for (GeneMolecularAlteration molecularAlteration : molecularAlterations) {
          GeneMolecularData molecularData = new GeneMolecularData();
          molecularData.setMolecularProfileId(molecularProfileId);
          molecularData.setSampleId(sample.getStableId());
          molecularData.setPatientId(sample.getPatientStableId());
          molecularData.setStudyId(sample.getCancerStudyIdentifier());
          molecularData.setEntrezGeneId(molecularAlteration.getEntrezGeneId());
          molecularData.setValue(molecularAlteration.getSplitValues()[indexOfSampleId]);
          molecularData.setGene(molecularAlteration.getGene());
          molecularDataList.add(molecularData);
        }
      }
    }

    return molecularDataList;
  }

  @Override
  public BaseMeta fetchMetaMolecularData(
      String molecularProfileId, List<String> sampleIds, List<Integer> entrezGeneIds)
      throws MolecularProfileNotFoundException {

    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        fetchMolecularData(molecularProfileId, sampleIds, entrezGeneIds, "ID").size());
    return baseMeta;
  }

  @Override
  public Iterable<GeneMolecularAlteration> getMolecularAlterations(
      String molecularProfileId, List<Integer> entrezGeneIds, String projection)
      throws MolecularProfileNotFoundException {

    validateMolecularProfile(molecularProfileId);
    if ((entrezGeneIds == null || entrezGeneIds.isEmpty()) && projection.equals("SUMMARY")) {
      return molecularDataRepository.getGeneMolecularAlterationsIterableFast(molecularProfileId);
    }
    return molecularDataRepository.getGeneMolecularAlterationsIterable(
        molecularProfileId, entrezGeneIds, projection);
  }

  @Override
  public Integer getNumberOfSamplesInMolecularProfile(String molecularProfileId) {

    MolecularProfileSamples commaSeparatedSampleIdsOfMolecularProfile =
        molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfileId);
    if (commaSeparatedSampleIdsOfMolecularProfile == null) {
      return null;
    }

    return commaSeparatedSampleIdsOfMolecularProfile.getSplitSampleIds().length;
  }

  @Override
  public List<GeneMolecularData> getMolecularDataInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      String projection) {

    List<GeneMolecularData> molecularDataList = new ArrayList<>();
    SortedSet<String> distinctMolecularProfileIds = new TreeSet<>(molecularProfileIds);

    Map<String, MolecularProfileSamples> commaSeparatedSampleIdsOfMolecularProfilesMap =
        molecularDataRepository.commaSeparatedSampleIdsOfMolecularProfilesMap(
            distinctMolecularProfileIds);
    if (commaSeparatedSampleIdsOfMolecularProfilesMap.size() == 0) {
      return molecularDataList;
    }

    Map<String, Map<Integer, Integer>> internalSampleIdsMap = new HashMap<>();
    List<Integer> allInternalSampleIds = new ArrayList<>();

    for (String molecularProfileId : distinctMolecularProfileIds) {
      List<Integer> internalSampleIds =
          Arrays.stream(
                  commaSeparatedSampleIdsOfMolecularProfilesMap
                      .get(molecularProfileId)
                      .getSplitSampleIds())
              .mapToInt(Integer::parseInt)
              .boxed()
              .collect(Collectors.toList());
      HashMap<Integer, Integer> molecularProfileSampleMap = new HashMap<Integer, Integer>();
      for (int lc = 0; lc < internalSampleIds.size(); lc++) {
        molecularProfileSampleMap.put(internalSampleIds.get(lc), lc);
      }
      internalSampleIdsMap.put(molecularProfileId, molecularProfileSampleMap);
      allInternalSampleIds.addAll(internalSampleIds);
    }

    List<MolecularProfile> molecularProfiles = new ArrayList<>();
    List<MolecularProfile> distinctMolecularProfiles =
        molecularProfileService.getMolecularProfiles(distinctMolecularProfileIds, "SUMMARY");
    Map<String, MolecularProfile> molecularProfileMapById =
        distinctMolecularProfiles.stream()
            .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));
    Map<String, List<MolecularProfile>> molecularProfileMapByStudyId =
        distinctMolecularProfiles.stream()
            .collect(groupingBy(MolecularProfile::getCancerStudyIdentifier));
    List<Sample> samples;
    if (sampleIds == null) {
      samples = sampleService.getSamplesByInternalIds(allInternalSampleIds);
      for (String molecularProfileId : distinctMolecularProfileIds) {
        internalSampleIdsMap
            .get(molecularProfileId)
            .keySet()
            .forEach(s -> molecularProfiles.add(molecularProfileMapById.get(molecularProfileId)));
      }
    } else {
      for (String molecularProfileId : molecularProfileIds) {
        molecularProfiles.add(molecularProfileMapById.get(molecularProfileId));
      }
      List<String> studyIds =
          molecularProfiles.stream()
              .map(MolecularProfile::getCancerStudyIdentifier)
              .collect(Collectors.toList());
      samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");
    }

    // Query all requested entrez gene ids in a single call instead of per-gene queries.
    // Doing so reduces query overhead and avoids N+1 query patterns, which are a
    // significant performance problem when using high-latency backends such as ClickHouse.
    if (entrezGeneIds == null || entrezGeneIds.isEmpty()) {
      return molecularDataList;
    }

    List<GeneMolecularAlteration> molecularAlterations =
      molecularDataRepository.getGeneMolecularAlterationsInMultipleMolecularProfiles(
        distinctMolecularProfileIds, entrezGeneIds, projection);
    Map<String, List<GeneMolecularAlteration>> molecularAlterationsMap =
        molecularAlterations.stream()
            .collect(groupingBy(GeneMolecularAlteration::getMolecularProfileId));

    for (Sample sample : samples) {
      for (MolecularProfile molecularProfile :
          molecularProfileMapByStudyId.get(sample.getCancerStudyIdentifier())) {
        String molecularProfileId = molecularProfile.getStableId();
        Integer indexOfSampleId =
            internalSampleIdsMap.get(molecularProfileId).get(sample.getInternalId());
        if (indexOfSampleId != null && molecularAlterationsMap.containsKey(molecularProfileId)) {
          for (GeneMolecularAlteration molecularAlteration :
              molecularAlterationsMap.get(molecularProfileId)) {
            GeneMolecularData molecularData = new GeneMolecularData();
            molecularData.setMolecularProfileId(molecularProfileId);
            molecularData.setSampleId(sample.getStableId());
            molecularData.setPatientId(sample.getPatientStableId());
            molecularData.setStudyId(sample.getCancerStudyIdentifier());
            molecularData.setEntrezGeneId(molecularAlteration.getEntrezGeneId());
            try {
              molecularData.setValue(molecularAlteration.getSplitValues()[indexOfSampleId]);
            } catch (ArrayIndexOutOfBoundsException e) {
              molecularData.setValue(null);
            }
            molecularData.setGene(molecularAlteration.getGene());
            molecularDataList.add(molecularData);
          }
        }
      }
    }

    return molecularDataList;
  }

  @Override
  public List<GeneMolecularData> getMolecularDataInMultipleMolecularProfilesByGeneQueries(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<GeneFilterQuery> geneQueries,
      String projection) {
    // Molecular alterations for all genes in the geneQueries
    List<Integer> entrezGeneIds =
        geneQueries.stream().map(GeneFilterQuery::getEntrezGeneId).collect(Collectors.toList());
    List<GeneMolecularData> molecularDataList =
        getMolecularDataInMultipleMolecularProfiles(
            molecularProfileIds, sampleIds, entrezGeneIds, projection);

    // All CNA events that match requested geneQueries
    List<DiscreteCopyNumberData> copyNumberData =
        discreteCopyNumberRepository.getDiscreteCopyNumbersInMultipleMolecularProfilesByGeneQueries(
            molecularProfileIds, sampleIds, geneQueries, projection);

    // molecularProfile->entrezGeneId->sampleId->alterationType lookup table for CNA events
    Map<String, DiscreteCopyNumberData> cnaEventLookup =
        copyNumberData.stream().collect(toMap(d -> cnaEventKey(d), Function.identity()));

    // remove molecular data that is not covered by a CNA event
    molecularDataList =
        molecularDataList.stream()
            .filter(d -> cnaEventLookup.containsKey(cnaEventKey(d)))
            .collect(Collectors.toList());

    return molecularDataList;
  }

  @Override
  @PreAuthorize(
      "hasPermission(#molecularProfileIds, 'Collection<MolecularProfileId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public BaseMeta getMetaMolecularDataInMultipleMolecularProfiles(
      List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds) {

    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        getMolecularDataInMultipleMolecularProfiles(
                molecularProfileIds, sampleIds, entrezGeneIds, "ID")
            .size());
    return baseMeta;
  }

  @Override
  @PreAuthorize(
      "hasPermission(#molecularProfileIds, 'Collection<MolecularProfileId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public List<MolecularDataCountItem> fetchMolecularDataCountsInMultipleMolecularProfiles(
      List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds) {

    // Fetch molecular data with minimal projection to reduce data transfer
    List<GeneMolecularData> molecularData =
        getMolecularDataInMultipleMolecularProfiles(
            molecularProfileIds, sampleIds, entrezGeneIds, "ID");

    // Group by molecular profile ID and count
    Map<String, Long> countsByProfile =
        molecularData.stream()
            .collect(
                Collectors.groupingBy(
                    GeneMolecularData::getMolecularProfileId, Collectors.counting()));

    // Convert to list of MolecularDataCountItem
    List<MolecularDataCountItem> result = new ArrayList<>();
    for (String profileId : molecularProfileIds) {
      MolecularDataCountItem item = new MolecularDataCountItem();
      item.setMolecularProfileId(profileId);
      item.setCount(countsByProfile.getOrDefault(profileId, 0L).intValue());
      result.add(item);
    }

    return result;
  }

  private void validateMolecularProfile(String molecularProfileId)
      throws MolecularProfileNotFoundException {

    MolecularProfile molecularProfile =
        molecularProfileService.getMolecularProfile(molecularProfileId);

    if (molecularProfile
            .getMolecularAlterationType()
            .equals(MolecularAlterationType.MUTATION_EXTENDED)
        || molecularProfile
            .getMolecularAlterationType()
            .equals(MolecularAlterationType.MUTATION_UNCALLED)
        || molecularProfile
            .getMolecularAlterationType()
            .equals(MolecularAlterationType.STRUCTURAL_VARIANT)) {

      throw new MolecularProfileNotFoundException(molecularProfileId);
    }
  }

  private String cnaEventKey(DiscreteCopyNumberData cna) {
    StringJoiner stringJoiner = new StringJoiner("_");
    stringJoiner.add(cna.getMolecularProfileId());
    stringJoiner.add(String.valueOf(cna.getEntrezGeneId()));
    stringJoiner.add(cna.getSampleId());
    stringJoiner.add(String.valueOf(cna.getAlteration()));
    return stringJoiner.toString();
  }

  private String cnaEventKey(GeneMolecularData cna) {
    StringJoiner stringJoiner = new StringJoiner("_");
    stringJoiner.add(cna.getMolecularProfileId());
    stringJoiner.add(String.valueOf(cna.getEntrezGeneId()));
    stringJoiner.add(cna.getSampleId());
    stringJoiner.add(String.valueOf(cna.getValue()));
    return stringJoiner.toString();
  }
}
