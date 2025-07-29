package org.cbioportal.legacy.service;

import java.util.List;
import java.util.Optional;
import org.cbioportal.legacy.web.parameter.VirtualStudy;

public interface VirtualStudyService {

  VirtualStudy getVirtualStudy(String id);

  Optional<VirtualStudy> getVirtualStudyByIdIfExists(String id);

  List<VirtualStudy> getUserVirtualStudies(String user);

  List<VirtualStudy> getPublishedVirtualStudies();

  void publishVirtualStudy(String id, String typeOfCancerId, String pmid);

  void unPublishVirtualStudy(String id);
}
