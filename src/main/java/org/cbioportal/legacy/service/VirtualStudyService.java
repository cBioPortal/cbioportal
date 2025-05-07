package org.cbioportal.legacy.service;

import java.util.List;
import java.util.Optional;
import org.cbioportal.legacy.model.CancerStudy;
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

  // TODO: check if sample counts of the bean are still used
  CancerStudy toCancerStudy(VirtualStudy vs);

  List<VirtualStudy> getPublishedVirtualStudies(String keyword);
}
