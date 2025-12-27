package org.cbioportal.legacy.persistence.mybatis;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.GeneMolecularAlteration;
import org.cbioportal.legacy.model.GenericAssayMolecularAlteration;
import org.cbioportal.legacy.model.GenesetMolecularAlteration;
import org.cbioportal.legacy.model.MolecularProfileSamples;
import org.cbioportal.legacy.persistence.MolecularDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.service.SampleService;

@Repository
public class MolecularDataMyBatisRepository implements MolecularDataRepository {

  @Autowired private MolecularDataMapper molecularDataMapper;
  
  @Autowired(required = false)
  private SampleService sampleService;
  
  @Value("${clickhouse_mode:false}")
  private boolean clickhouseMode;

  @Override
  public MolecularProfileSamples getCommaSeparatedSampleIdsOfMolecularProfile(
      String molecularProfileId) {
    try {
      return molecularDataMapper
          .getCommaSeparatedSampleIdsOfMolecularProfiles(Collections.singleton(molecularProfileId))
          .get(0);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  @Override
  public Map<String, MolecularProfileSamples> commaSeparatedSampleIdsOfMolecularProfilesMap(
      Set<String> molecularProfileIds) {

    return molecularDataMapper
        .getCommaSeparatedSampleIdsOfMolecularProfiles(molecularProfileIds)
        .stream()
        .collect(
            Collectors.toMap(MolecularProfileSamples::getMolecularProfileId, Function.identity()));
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

    if (clickhouseMode) {
      return getGeneMolecularAlterationsInMultipleMolecularProfilesClickHouse(
          molecularProfileIds, entrezGeneIds, projection);
    }
    
    return molecularDataMapper.getGeneMolecularAlterationsInMultipleMolecularProfiles(
        molecularProfileIds, entrezGeneIds, projection);
  }
  
  /**
   * ClickHouse-optimized implementation that queries genetic_alteration_derived table
   * and aggregates per-sample rows into CSV format expected by the service layer.
   */
  private List<GeneMolecularAlteration> getGeneMolecularAlterationsInMultipleMolecularProfilesClickHouse(
      Set<String> molecularProfileIds, List<Integer> entrezGeneIds, String projection) {
    
    List<GeneMolecularAlteration> rawRows = 
        molecularDataMapper.getGeneMolecularAlterationsInMultipleMolecularProfilesClickHouse(
            molecularProfileIds, entrezGeneIds);

    if (rawRows.isEmpty()) {
      return Collections.emptyList();
    }
    
    // Group by profile
    Map<String, List<GeneMolecularAlteration>> rowsByProfile = rawRows.stream()
        .collect(Collectors.groupingBy(GeneMolecularAlteration::getMolecularProfileId));

    // Get sample order for each profile
    Map<String, MolecularProfileSamples> profileSamplesMap =
        commaSeparatedSampleIdsOfMolecularProfilesMap(molecularProfileIds);

    // Collect all internal IDs for sample lookup
    List<Integer> allInternalIds = new ArrayList<>();
    profileSamplesMap.values().forEach(s -> 
        Arrays.stream(s.getSplitSampleIds())
            .mapToInt(Integer::parseInt)
            .forEach(allInternalIds::add));

    // Fetch samples and build unique ID map
    List<Sample> samples = sampleService.getSamplesByInternalIds(allInternalIds);
    Map<Integer, Sample> internalIdToSample = samples.stream()
        .collect(Collectors.toMap(Sample::getInternalId, Function.identity()));

    List<GeneMolecularAlteration> results = new ArrayList<>();
    
    for (String profileId : profileSamplesMap.keySet()) {
      MolecularProfileSamples mps = profileSamplesMap.get(profileId);
      String[] sampleIds = mps.getSplitSampleIds();
      
      // Build sample unique ID order
      List<String> sampleUniqueIdOrder = new ArrayList<>(sampleIds.length);
      for (String internalIdStr : sampleIds) {
        try {
          int internalId = Integer.parseInt(internalIdStr);
          Sample s = internalIdToSample.get(internalId);
          if (s != null) {
            sampleUniqueIdOrder.add(s.getCancerStudyIdentifier() + "_" + s.getStableId());
          } else {
            sampleUniqueIdOrder.add(null);
          }
        } catch (NumberFormatException e) {
          // Skip invalid internalIdStr, add null to maintain order
          sampleUniqueIdOrder.add(null);
        }
      }

      List<GeneMolecularAlteration> profileRows = rowsByProfile.get(profileId);
      if (profileRows == null) continue;

      // Group rows by gene
      Map<Integer, List<GeneMolecularAlteration>> geneRows = profileRows.stream()
          .collect(Collectors.groupingBy(GeneMolecularAlteration::getEntrezGeneId));

      for (Map.Entry<Integer, List<GeneMolecularAlteration>> entry : geneRows.entrySet()) {
        Integer geneId = entry.getKey();
        List<GeneMolecularAlteration> geneAlterations = entry.getValue();
        
        // Build map of sampleUniqueId -> value
        Map<String, String> sampleValueMap = new HashMap<>();
        for (GeneMolecularAlteration alt : geneAlterations) {
          // Values field contains sampleUniqueId|value for ClickHouse rows
          String[] parts = alt.getValues().split("\\|", 2);
          if (parts.length == 2) {
            sampleValueMap.put(parts[0], parts[1]);
          }
        }
        
        // Build CSV values string in sample order
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sampleUniqueIdOrder.size(); i++) {
          if (i > 0) sb.append(',');
          String sampleUniqueId = sampleUniqueIdOrder.get(i);
          if (sampleUniqueId != null && sampleValueMap.containsKey(sampleUniqueId)) {
            sb.append(sampleValueMap.get(sampleUniqueId));
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
