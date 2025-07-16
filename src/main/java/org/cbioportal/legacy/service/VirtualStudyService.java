package org.cbioportal.legacy.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
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

  // TODO cahce
  // TODO maybe vs study to materialized study mapping would be more useful
  Set<String> getPublishedVirtualStudyIds();

  // TODO cahce
  Map<StudyScopedId, StudyScopedId> getVirtualToMaterializedStudySamplePairs();

  Map<StudyScopedId, Set<String>> toMaterializedStudySamplePairsMap(
      List<StudyScopedId> studyScopedIds);

  Map<StudyScopedId, Set<String>> toMaterializedStudyPatientPairsMap(
      List<StudyScopedId> studyScopedIds);

  Map<String, Pair<String, String>> toMolecularProfileInfo(Set<String> molecularProfileIds);
}
