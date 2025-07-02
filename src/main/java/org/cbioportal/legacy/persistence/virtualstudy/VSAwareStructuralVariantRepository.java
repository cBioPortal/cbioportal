package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.GeneFilterQuery;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.model.StructuralVariantFilterQuery;
import org.cbioportal.legacy.model.StructuralVariantQuery;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.persistence.StructuralVariantRepository;
import org.cbioportal.legacy.service.VirtualStudyService;

public class VSAwareStructuralVariantRepository implements StructuralVariantRepository {
  private final VirtualStudyService virtualStudyService;
  private final StructuralVariantRepository structuralVariantRepository;

  public VSAwareStructuralVariantRepository(
      VirtualStudyService virtualStudyService,
      StructuralVariantRepository structuralVariantRepository) {
    this.virtualStudyService = virtualStudyService;
    this.structuralVariantRepository = structuralVariantRepository;
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
                        .map(
                            virtualStudyId ->
                                virtualStudyService.virtualizeStructuralVariant(
                                    virtualStudyId, sv));
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
    return fetchStructuralVariants(
        molecularProfileIds,
        sampleIds,
        (mpIds, sIds) ->
            structuralVariantRepository.fetchStructuralVariants(
                mpIds, sIds, entrezGeneIds, structuralVariantQueries));
  }

  @Override
  public List<StructuralVariant> fetchStructuralVariantsByGeneQueries(
      List<String> molecularProfileIds, List<String> sampleIds, List<GeneFilterQuery> geneQueries) {
    return fetchStructuralVariants(
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
    return fetchStructuralVariants(
        molecularProfileIds,
        sampleIds,
        (mpIds, sIds) ->
            structuralVariantRepository.fetchStructuralVariantsByStructVarQueries(
                mpIds, sIds, structVarQueries));
  }
}
