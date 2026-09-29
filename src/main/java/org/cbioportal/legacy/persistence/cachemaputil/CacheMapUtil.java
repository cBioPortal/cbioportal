package org.cbioportal.legacy.persistence.cachemaputil;

import java.util.Map;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.SampleList;

public interface CacheMapUtil {
  Map<String, MolecularProfile> getMolecularProfileMap();

  Map<String, SampleList> getSampleListMap();

  /**
   * Returns cancer studies keyed by stable identifier, populated with only the fields permission
   * checks actually use (cancerStudyIdentifier, groups) -- see
   * CacheMapBuilder.buildCancerStudyPermissionMap. Every other CancerStudy field is left unset; do
   * not use this for anything but permission evaluation. Backed by CancerStudyPermissionCache's own
   * short TTL, independent of whichever caching strategy this implementation otherwise uses.
   */
  Map<String, CancerStudy> getCancerStudyPermissionMap();

  boolean hasCacheEnabled();
}
