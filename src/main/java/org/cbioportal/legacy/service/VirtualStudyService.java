package org.cbioportal.legacy.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.CopyNumberSeg;
import org.cbioportal.legacy.model.DiscreteCopyNumberData;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.web.parameter.VirtualStudy;

public interface VirtualStudyService {
  String ALL_USERS = "*";

  VirtualStudy getVirtualStudy(String id);

  Optional<VirtualStudy> getVirtualStudyByIdIfExists(String id);

  List<VirtualStudy> getUserVirtualStudies(String user);

  // TODO implement cache
  List<VirtualStudy> getPublishedVirtualStudies();

  // TODO add study id to the cache
  void publishVirtualStudy(String id, String typeOfCancerId, String pmid);

  // TODO evict study id from the cache
  void unPublishVirtualStudy(String id);

  List<VirtualStudy> getPublishedVirtualStudies(String keyword);

  ClinicalData virtualizeClinicalData(String virtualStudyId, ClinicalData clinicalData);

  // TODO cahce
  // TODO maybe vs study to materialized study mapping would be more useful
  Set<String> getPublishedVirtualStudyIds();

  // TODO cahce
  Map<StudyScopedId, StudyScopedId> getVirtualToMaterializedStudySamplePairs();

  // TODO cahce
  Map<StudyScopedId, StudyScopedId> getVirtualToMaterializedStudyPatientPairs();

  Map<StudyScopedId, Set<String>> toMaterializedStudySamplePairsMap(
      List<StudyScopedId> studyScopedIds);

  Map<StudyScopedId, Set<String>> toMaterializedStudyPatientPairsMap(
      List<StudyScopedId> studyScopedIds);

  Pair<List<StudyScopedId>, List<StudyScopedId>> splitMaterialisedAndVirtualStudySamplePairs(
      List<StudyScopedId> studyScopedIds);

  List<StudyScopedId> toStudySamplePairs(List<String> studyIds, List<String> sampleIds);

  Pair<List<String>, List<String>> toStudyAndSampleIdLists(
      Iterable<StudyScopedId> studySamplePairs);

  Sample virtualizeSample(String virtualStudyId, Sample sample);

  Map<String, Pair<String, String>> toMolecularProfileInfo(Set<String> molecularProfileIds);

  DiscreteCopyNumberData virtualizeDiscreteCopyNumber(
      String vitualStudyId, DiscreteCopyNumberData dcn);

  Mutation virtualizeMutation(String virtualStudyId, Mutation m);

  StructuralVariant virtualizeStructuralVariant(String virtualStudyId, StructuralVariant sv);

  CopyNumberSeg virtualizeCopyNumberSeg(String virtualStudyId, CopyNumberSeg segment);
}
