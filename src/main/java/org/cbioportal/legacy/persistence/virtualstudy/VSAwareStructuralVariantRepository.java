package org.cbioportal.legacy.persistence.virtualstudy;

import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.calculateVirtualMoleculaProfileId;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.GeneFilterQuery;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.model.StructuralVariantFilterQuery;
import org.cbioportal.legacy.model.StructuralVariantQuery;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.persistence.StructuralVariantRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.VirtualStudy;

public class VSAwareStructuralVariantRepository implements StructuralVariantRepository {
  private final VirtualStudyService virtualStudyService;
  private final StructuralVariantRepository structuralVariantRepository;
  private final VSAwareMolecularProfileRepository molecularProfileRepository;

  public VSAwareStructuralVariantRepository(
      VirtualStudyService virtualStudyService,
      StructuralVariantRepository structuralVariantRepository,
      VSAwareMolecularProfileRepository molecularProfileRepository) {
    this.virtualStudyService = virtualStudyService;
    this.structuralVariantRepository = structuralVariantRepository;
    this.molecularProfileRepository = molecularProfileRepository;
  }

  private List<StructuralVariant> fetchStructuralVariants2(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      BiFunction<List<String>, List<String>, List<StructuralVariant>> fetch) {
    if (molecularProfileIds.size() != sampleIds.size()) {
      throw new IllegalArgumentException(
          "Molecular profile ids and sample ids must have the same size");
    }
    LinkedHashMap<String, LinkedHashSet<String>> molecularProfileToSampleIds =
        new LinkedHashMap<>();
    for (int i = 0; i < molecularProfileIds.size(); i++) {
      String molecularProfileId = molecularProfileIds.get(i);
      String sampleId = sampleIds.get(i);
      molecularProfileToSampleIds
          .computeIfAbsent(molecularProfileId, k -> new LinkedHashSet<>())
          .add(sampleId);
    }
    Map<String, VirtualStudy> virtualStudiesById =
        virtualStudyService.getPublishedVirtualStudies().stream()
            .collect(Collectors.toMap(VirtualStudy::getId, Function.identity()));
    Map<String, MolecularProfile> molecularProfilesByStableId =
        molecularProfileRepository
            .getMolecularProfiles(molecularProfileToSampleIds.keySet(), Projection.DETAILED.name())
            .stream()
            .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));
    Map<String, MolecularProfile> virtualMolecularProfilesByStableId =
        molecularProfilesByStableId.entrySet().stream()
            .filter(
                entry ->
                    virtualStudiesById.containsKey(entry.getValue().getCancerStudyIdentifier()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    LinkedHashMap<String, LinkedHashSet<String>> molecularProfileToCorrectSampleIds =
        molecularProfileToSampleIds.sequencedEntrySet().stream()
            .map(
                entry -> {
                  String molecularProfileId = entry.getKey();
                  LinkedHashSet<String> sampleIdsSet = entry.getValue();
                  MolecularProfile molecularProfile =
                      virtualMolecularProfilesByStableId.get(molecularProfileId);
                  if (molecularProfile == null) {
                    return new AbstractMap.SimpleEntry<>(molecularProfileId, sampleIdsSet);
                  }
                  VirtualStudy virtualStudy =
                      virtualStudiesById.get(molecularProfile.getCancerStudyIdentifier());
                  Set<String> allVsSampleIds =
                      virtualStudy.getData().getStudies().stream()
                          .flatMap(vss -> vss.getSamples().stream())
                          .collect(Collectors.toSet());
                  return new AbstractMap.SimpleEntry<>(
                      molecularProfileId,
                      sampleIdsSet.stream()
                          .filter(allVsSampleIds::contains)
                          .collect(Collectors.toCollection(LinkedHashSet::new)));
                })
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (a, b) -> {
                      a.addAll(b);
                      return a;
                    },
                    LinkedHashMap::new));
    // TODO this way of calculating virtual molecular profile ids has to change
    Map<String, String> virtualMolecularProfileIdToMaterializedMolecularProfileId =
        virtualMolecularProfilesByStableId.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getKey().replace(e.getValue().getCancerStudyIdentifier() + "_", "")));
    Map<String, Set<String>> materializedMolecularProfileToVirtualMolecularProfileIds =
        virtualMolecularProfileIdToMaterializedMolecularProfileId.entrySet().stream()
            .collect(
                Collectors.groupingBy(
                    Map.Entry::getValue,
                    Collectors.mapping(Map.Entry::getKey, Collectors.toSet())));
    Map<String, Set<String>> molecularProfileIdToSampleIdToQuery =
        molecularProfileToCorrectSampleIds.entrySet().stream()
            .map(
                entry ->
                    new AbstractMap.SimpleEntry<>(
                        virtualMolecularProfileIdToMaterializedMolecularProfileId.getOrDefault(
                            entry.getKey(), entry.getKey()),
                        entry.getValue()))
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (a, b) -> {
                      a.addAll(b);
                      return a;
                    },
                    LinkedHashMap::new));
    List<String> molecularProfileIdsToQuery = new ArrayList<>();
    List<String> sampleIdsToQuery = new ArrayList<>();
    for (Map.Entry<String, Set<String>> entry : molecularProfileIdToSampleIdToQuery.entrySet()) {
      String molecularProfileId = entry.getKey();
      Set<String> sampleIdsSet = entry.getValue();
      for (String sampleId : sampleIdsSet) {
        molecularProfileIdsToQuery.add(molecularProfileId);
        sampleIdsToQuery.add(sampleId);
      }
    }
    List<StructuralVariant> fetchedEntities =
        fetch.apply(molecularProfileIdsToQuery, sampleIdsToQuery);
    ArrayList<StructuralVariant> result = new ArrayList<>();
    for (StructuralVariant structuralVariant : fetchedEntities) {
      String molecularProfileId = structuralVariant.getMolecularProfileId();
      String sampleId = structuralVariant.getSampleId();
      if (molecularProfileToCorrectSampleIds.containsKey(molecularProfileId)
          && molecularProfileToCorrectSampleIds.get(molecularProfileId).contains(sampleId)) {
        // this is a materialized structural variant
        result.add(structuralVariant);
      }
      if (materializedMolecularProfileToVirtualMolecularProfileIds.containsKey(
          molecularProfileId)) {
        Set<String> virtualMolecularProfileIds =
            materializedMolecularProfileToVirtualMolecularProfileIds.get(molecularProfileId);
        // this is a virtual structural variant
        for (String virtualMolecularProfileId : virtualMolecularProfileIds) {
          // we need to check if the sample id is in the correct sample ids for the virtual
          // molecular profile
          // this is a virtual structural variant
          if (molecularProfileToCorrectSampleIds.containsKey(virtualMolecularProfileId)
              && molecularProfileToCorrectSampleIds
                  .get(virtualMolecularProfileId)
                  .contains(sampleId)) {
            MolecularProfile virtualMolecularProfile =
                virtualMolecularProfilesByStableId.get(virtualMolecularProfileId);
            result.add(
                virtualizeStructuralVariant(
                    virtualMolecularProfile.getCancerStudyIdentifier(), structuralVariant));
          }
        }
      }
    }
    // TODO we might want to sort the result here in certain order if needed
    return result;
  }

  private List<StructuralVariant> fetchStructuralVariants(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      BiFunction<List<String>, List<String>, List<StructuralVariant>> fetch) {
    Map<String, Pair<String, String>> mapping =
        virtualStudyService.toMolecularProfileInfo(new HashSet<>(molecularProfileIds));
    List<String> materializedMolecularProfileIds = new ArrayList<>();
    List<String> materializedSampleIds = new ArrayList<>();
    List<String> virtualMolecularProfileIds = new ArrayList<>();
    List<StudyScopedId> virtualSampleIds = new ArrayList<>();
    List<StructuralVariant> result = new ArrayList<>();
    for (int i = 0; i < sampleIds.size(); i++) {
      String sampleId = sampleIds.get(i);
      String molecularProfileId = molecularProfileIds.get(i);
      Pair<String, String> pair = mapping.get(molecularProfileId);
      if (pair == null) {
        materializedMolecularProfileIds.add(molecularProfileId);
        materializedSampleIds.add(sampleId);
      } else {
        virtualMolecularProfileIds.add(pair.getRight());
        virtualSampleIds.add(new StudyScopedId(pair.getLeft(), sampleId));
      }
    }
    if (!materializedMolecularProfileIds.isEmpty()) {
      result.addAll(fetch.apply(materializedMolecularProfileIds, materializedSampleIds));
    }
    if (!virtualMolecularProfileIds.isEmpty()) {
      Map<StudyScopedId, StudyScopedId> sampleIdsMapping =
          virtualStudyService.getVirtualToMaterializedStudySamplePairs();
      Map<Pair<String, String>, Set<String>> virtualStudiesRequestionProfileSampleId =
          new HashMap<>();
      for (int i = 0; i < virtualMolecularProfileIds.size(); i++) {
        String virtualMolecularProfileId = virtualMolecularProfileIds.get(i);
        StudyScopedId virtualSampleId = virtualSampleIds.get(i);
        StudyScopedId originalSampleId = sampleIdsMapping.get(virtualSampleId);
        Pair<String, String> key =
            new ImmutablePair<>(virtualMolecularProfileId, originalSampleId.getStableId());
        virtualStudiesRequestionProfileSampleId
            .computeIfAbsent(key, k -> new HashSet<>())
            .add(virtualSampleId.getStudyStableId());
      }
      List<String> originalSampleIds =
          virtualSampleIds.stream()
              .map(sampleIdsMapping::get)
              .map(StudyScopedId::getStableId)
              .toList();

      result.addAll(
          fetch.apply(virtualMolecularProfileIds, originalSampleIds).stream()
              .flatMap(
                  sv -> {
                    Set<String> virtualStudyIds =
                        virtualStudiesRequestionProfileSampleId.get(
                            Pair.of(sv.getMolecularProfileId(), sv.getSampleId()));
                    return virtualStudyIds.stream()
                        .map(virtualStudyId -> virtualizeStructuralVariant(virtualStudyId, sv));
                  })
              .toList());
    }
    return result;
  }

  @Override
  public List<StructuralVariant> fetchStructuralVariants(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<StructuralVariantQuery> structuralVariantQueries) {
    return fetchStructuralVariants2(
        molecularProfileIds,
        sampleIds,
        (mpIds, sIds) ->
            structuralVariantRepository.fetchStructuralVariants(
                mpIds, sIds, entrezGeneIds, structuralVariantQueries));
  }

  @Override
  public List<StructuralVariant> fetchStructuralVariantsByGeneQueries(
      List<String> molecularProfileIds, List<String> sampleIds, List<GeneFilterQuery> geneQueries) {
    return fetchStructuralVariants2(
        molecularProfileIds,
        sampleIds,
        (mpIds, sIds) ->
            structuralVariantRepository.fetchStructuralVariantsByGeneQueries(
                mpIds, sIds, geneQueries));
  }

  @Override
  public List<StructuralVariant> fetchStructuralVariantsByStructVarQueries(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<StructuralVariantFilterQuery> structVarQueries) {
    return fetchStructuralVariants2(
        molecularProfileIds,
        sampleIds,
        (mpIds, sIds) ->
            structuralVariantRepository.fetchStructuralVariantsByStructVarQueries(
                mpIds, sIds, structVarQueries));
  }

  private StructuralVariant virtualizeStructuralVariant(
      String virtualStudyId, StructuralVariant sv) {
    StructuralVariant virtualStructuralVariant = new StructuralVariant();
    virtualStructuralVariant.setMolecularProfileId(
        calculateVirtualMoleculaProfileId(virtualStudyId, sv.getMolecularProfileId()));
    virtualStructuralVariant.setSampleId(sv.getSampleId());
    virtualStructuralVariant.setPatientId(sv.getPatientId());
    virtualStructuralVariant.setStudyId(virtualStudyId);
    virtualStructuralVariant.setSite1EntrezGeneId(sv.getSite1EntrezGeneId());
    virtualStructuralVariant.setSite1HugoSymbol(sv.getSite1HugoSymbol());
    virtualStructuralVariant.setSite1EnsemblTranscriptId(sv.getSite1EnsemblTranscriptId());
    virtualStructuralVariant.setSite1Chromosome(sv.getSite1Chromosome());
    virtualStructuralVariant.setSite1Position(sv.getSite1Position());
    virtualStructuralVariant.setSite1Contig(sv.getSite1Contig());
    virtualStructuralVariant.setSite1Region(sv.getSite1Region());
    virtualStructuralVariant.setSite1RegionNumber(sv.getSite1RegionNumber());
    virtualStructuralVariant.setSite1Description(sv.getSite1Description());
    virtualStructuralVariant.setSite2EntrezGeneId(sv.getSite2EntrezGeneId());
    virtualStructuralVariant.setSite2HugoSymbol(sv.getSite2HugoSymbol());
    virtualStructuralVariant.setSite2EnsemblTranscriptId(sv.getSite2EnsemblTranscriptId());
    virtualStructuralVariant.setSite2Chromosome(sv.getSite2Chromosome());
    virtualStructuralVariant.setSite2Position(sv.getSite2Position());
    virtualStructuralVariant.setSite2Contig(sv.getSite2Contig());
    virtualStructuralVariant.setSite2Region(sv.getSite2Region());
    virtualStructuralVariant.setSite2RegionNumber(sv.getSite2RegionNumber());
    virtualStructuralVariant.setSite2Description(sv.getSite2Description());
    virtualStructuralVariant.setSite2EffectOnFrame(sv.getSite2EffectOnFrame());
    virtualStructuralVariant.setNcbiBuild(sv.getNcbiBuild());
    virtualStructuralVariant.setDnaSupport(sv.getDnaSupport());
    virtualStructuralVariant.setRnaSupport(sv.getRnaSupport());
    virtualStructuralVariant.setNormalReadCount(sv.getNormalReadCount());
    virtualStructuralVariant.setTumorReadCount(sv.getTumorReadCount());
    virtualStructuralVariant.setNormalVariantCount(sv.getNormalVariantCount());
    virtualStructuralVariant.setTumorVariantCount(sv.getTumorVariantCount());
    virtualStructuralVariant.setNormalPairedEndReadCount(sv.getNormalPairedEndReadCount());
    virtualStructuralVariant.setTumorPairedEndReadCount(sv.getTumorPairedEndReadCount());
    virtualStructuralVariant.setNormalSplitReadCount(sv.getNormalSplitReadCount());
    virtualStructuralVariant.setTumorSplitReadCount(sv.getTumorSplitReadCount());
    virtualStructuralVariant.setAnnotation(sv.getAnnotation());
    virtualStructuralVariant.setBreakpointType(sv.getBreakpointType());
    virtualStructuralVariant.setConnectionType(sv.getConnectionType());
    virtualStructuralVariant.setEventInfo(sv.getEventInfo());
    virtualStructuralVariant.setVariantClass(sv.getVariantClass());
    virtualStructuralVariant.setLength(sv.getLength());
    virtualStructuralVariant.setComments(sv.getComments());
    virtualStructuralVariant.setDriverFilter(sv.getDriverFilter());
    virtualStructuralVariant.setDriverFilterAnn(sv.getDriverFilterAnn());
    virtualStructuralVariant.setDriverTiersFilter(sv.getDriverTiersFilter());
    virtualStructuralVariant.setDriverTiersFilterAnn(sv.getDriverTiersFilterAnn());
    virtualStructuralVariant.setSvStatus(sv.getSvStatus());
    return virtualStructuralVariant;
  }
}
