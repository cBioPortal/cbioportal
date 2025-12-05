package org.cbioportal.legacy.persistence.mybatisclickhouse;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.GenesetMolecularAlteration;
import org.cbioportal.legacy.model.GeneMolecularAlteration;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.model.MolecularProfileSamples;
import org.cbioportal.legacy.model.MolecularDataRowPerSample;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.persistence.MolecularDataRepository;
import org.cbioportal.legacy.persistence.mybatis.MolecularDataMyBatisRepository;
import org.cbioportal.legacy.service.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;

@Repository
@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "test")
public class MolecularDataMyBatisClickhouseRepository implements MolecularDataRepository {

  private final MolecularDataMapper mapper;
  private final MolecularDataMyBatisRepository legacyRepository;
  private final SampleService sampleService;

  @Autowired
  public MolecularDataMyBatisClickhouseRepository(
      MolecularDataMapper mapper, MolecularDataMyBatisRepository legacyRepository, SampleService sampleService) {
    this.mapper = mapper;
    this.legacyRepository = legacyRepository;
    this.sampleService = sampleService;
  }

  @Override
  public Map<String, MolecularProfileSamples> commaSeparatedSampleIdsOfMolecularProfilesMap(
      Set<String> molecularProfileIds) {
    return legacyRepository.commaSeparatedSampleIdsOfMolecularProfilesMap(molecularProfileIds);
  }

  @Override
  public List<GeneMolecularAlteration> getGeneMolecularAlterationsInMultipleMolecularProfiles(
      Set<String> molecularProfileIds, List<Integer> entrezGeneIds, String projection) {
    List<MolecularDataRowPerSample> rows =
        mapper.getGeneMolecularAlterationsPerSampleInMultipleMolecularProfiles(
            molecularProfileIds, entrezGeneIds);

    if (rows.isEmpty()) {
      return Collections.emptyList();
    }

    // Build sample order map for profiles
    Map<String, MolecularProfileSamples> profileSamplesMap =
        legacyRepository.commaSeparatedSampleIdsOfMolecularProfilesMap(molecularProfileIds);

    // Collect all internal IDs from profileSamplesMap for sample lookup
    List<Integer> allInternalIds = new ArrayList<>();
    profileSamplesMap.values()
        .forEach(s -> Arrays.stream(s.getSplitSampleIds()).mapToInt(Integer::parseInt).forEach(allInternalIds::add));

    // Samples fetch
    List<Sample> samples = sampleService.getSamplesByInternalIds(allInternalIds);
    // Map unique id composed as studyId_sampleStableId for quick lookup
    Map<String, Sample> uniqueIdToSample = new HashMap<>();
    for (Sample s : samples) {
      String uniqueId = s.getCancerStudyIdentifier() + "_" + s.getStableId();
      uniqueIdToSample.put(uniqueId, s);
    }

    // Group rows by profile and by entrez gene
    Map<String, Map<Integer, Map<String, String>>> map = new HashMap<>();
    for (MolecularDataRowPerSample r : rows) {
      map.computeIfAbsent(r.getMolecularProfileId(), k -> new HashMap<>())
          .computeIfAbsent(r.getEntrezGeneId(), k -> new HashMap<>())
          .put(r.getSampleUniqueId(), r.getValue());
    }

    List<GeneMolecularAlteration> results = new ArrayList<>();
    for (String profileId : profileSamplesMap.keySet()) {
      MolecularProfileSamples mps = profileSamplesMap.get(profileId);
      String[] sampleIds = mps.getSplitSampleIds();
      int numSamples = sampleIds.length;
      List<String> sampleUniqueIdOrder = new ArrayList<>(numSamples);
      // Build quick internal id -> sample map to avoid O(n^2)
      Map<Integer, Sample> internalIdToSample = samples.stream().collect(Collectors.toMap(Sample::getInternalId, Function.identity()));
      for (String internalIdStr : sampleIds) {
        int internalId = Integer.parseInt(internalIdStr);
        Sample s = internalIdToSample.get(internalId);
        if (s != null) {
          sampleUniqueIdOrder.add(s.getCancerStudyIdentifier() + "_" + s.getStableId());
          } else {
            sampleUniqueIdOrder.add(null);
        }
      }

      Map<Integer, Map<String, String>> geneMap = map.get(profileId);
      if (geneMap == null) continue;

      for (Integer geneId : geneMap.keySet()) {
        Map<String, String> sampleValueMap = geneMap.get(geneId);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sampleUniqueIdOrder.size(); i++) {
          if (i > 0) sb.append(',');
          String sampleUniqueId = sampleUniqueIdOrder.get(i);
          if (sampleUniqueId != null && sampleValueMap.containsKey(sampleUniqueId)) {
            sb.append(sampleValueMap.get(sampleUniqueId));
          } else {
            // keep empty placeholder - will produce adjacent commas when missing
          }
        }
        GeneMolecularAlteration alteration = new GeneMolecularAlteration();
        alteration.setEntrezGeneId(geneId);
        alteration.setMolecularProfileId(profileId);
        alteration.setValues(sb.toString());
        results.add(alteration);
      }
    }

    return results;
  }

  // Leave other methods unimplemented for now; they can delegate or throw UnsupportedOperationException
  @Override
  public MolecularProfileSamples getCommaSeparatedSampleIdsOfMolecularProfile(String molecularProfileId) {
    return legacyRepository.getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfileId);
  }

  // The rest of the interface methods would be implemented similarly as needed.
  @Override
  public List<GeneMolecularAlteration> getGeneMolecularAlterations(String molecularProfileId, List<Integer> entrezGeneIds, String projection) {
    throw new UnsupportedOperationException("Not implemented in clickhouse repository yet");
  }

  @Override
  public Iterable<GeneMolecularAlteration> getGeneMolecularAlterationsIterable(String molecularProfileId, List<Integer> entrezGeneIds, String projection) {
    throw new UnsupportedOperationException("Not implemented in clickhouse repository yet");
  }

  @Override
  public Iterable<GeneMolecularAlteration> getGeneMolecularAlterationsIterableFast(String molecularProfileId) {
    throw new UnsupportedOperationException("Not implemented in clickhouse repository yet");
  }

  @Override
  public List<GenesetMolecularAlteration> getGenesetMolecularAlterations(String molecularProfileId, List<String> genesetIds, String projection) {
    throw new UnsupportedOperationException("Not implemented in clickhouse repository yet");
  }

  @Override
  public List<org.cbioportal.legacy.model.GenericAssayMolecularAlteration> getGenericAssayMolecularAlterations(String molecularProfileId, List<String> stableIds, String projection) {
    throw new UnsupportedOperationException("Not implemented in clickhouse repository yet");
  }

  @Override
  public Iterable<org.cbioportal.legacy.model.GenericAssayMolecularAlteration> getGenericAssayMolecularAlterationsIterable(String molecularProfileId, List<String> stableIds, String projection) {
    throw new UnsupportedOperationException("Not implemented in clickhouse repository yet");
  }

}
