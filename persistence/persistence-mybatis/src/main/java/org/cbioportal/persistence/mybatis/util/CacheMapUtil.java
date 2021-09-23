package org.cbioportal.persistence.mybatis.util;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.SampleList;

import java.util.Map;

public interface CacheMapUtil {
    Map<String, MolecularProfile> getMolecularProfileMap();

    Map<String, SampleList> getSampleListMap();

    Map<String, CancerStudy> getCancerStudyMap();

    Map<String, String> getGenericAssayStableIdToMolecularProfileIdMap();
    
    boolean hasCacheEnabled();

}
