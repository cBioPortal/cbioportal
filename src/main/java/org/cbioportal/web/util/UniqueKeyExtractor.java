package org.cbioportal.web.util;

import org.cbioportal.utils.Encoding;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Collection;

@Component
public class UniqueKeyExtractor {

    public void extractUniqueKeys(List<String> uniqueKeys, Collection<String> studyIdsToReturn) {
        extractUniqueKeys(uniqueKeys, studyIdsToReturn, null);
    }

    public void extractUniqueKeys(List<String> uniqueKeys, Collection<String> studyIdsToReturn, Collection<String> patientOrSampleIdsToReturn) {
        for (String uniqueKey : uniqueKeys) {
            String uniqueId = Encoding.decodeBase64(uniqueKey);
            String[] patientOrSampleAndStudyId = uniqueId.split(Encoding.DELIMITER);
            if (patientOrSampleAndStudyId.length == 2) {
                if (patientOrSampleIdsToReturn != null) {
                    patientOrSampleIdsToReturn.add(patientOrSampleAndStudyId[0]);
                }
                studyIdsToReturn.add(patientOrSampleAndStudyId[1]);
            }
        }
    }
}
