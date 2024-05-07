package org.cbioportal.web.util;

import org.cbioportal.utils.Encoder;
import java.util.List;
import java.util.Collection;

public class UniqueKeyExtractor {

    private UniqueKeyExtractor() {}
    
    public static void extractUniqueKeys(List<String> uniqueKeys, Collection<String> studyIdsToReturn) {
        extractUniqueKeys(uniqueKeys, studyIdsToReturn, null);
    }

    public static void extractUniqueKeys(List<String> uniqueKeys, Collection<String> studyIdsToReturn, Collection<String> patientOrSampleIdsToReturn) {
        for (String uniqueKey : uniqueKeys) {
            String uniqueId = Encoder.decodeBase64(uniqueKey);
            String[] patientOrSampleAndStudyId = uniqueId.split(Encoder.DELIMITER);
            if (patientOrSampleAndStudyId.length == 2) {
                if (patientOrSampleIdsToReturn != null) {
                    patientOrSampleIdsToReturn.add(patientOrSampleAndStudyId[0]);
                }
                studyIdsToReturn.add(patientOrSampleAndStudyId[1]);
            }
        }
    }
}
