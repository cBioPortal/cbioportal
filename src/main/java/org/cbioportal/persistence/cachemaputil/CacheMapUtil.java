package org.cbioportal.persistence.cachemaputil;

import java.util.Map;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.SampleList;

public interface CacheMapUtil {
  Map<String, MolecularProfile> getMolecularProfileMap();

  Map<String, SampleList> getSampleListMap();

  Map<String, CancerStudy> getCancerStudyMap();

  boolean hasCacheEnabled();
}
