package org.cbioportal.web.util;

import java.util.Base64;
import java.util.Collection;
import java.util.List;
import org.cbioportal.web.interceptor.UniqueKeyInterceptor;
import org.springframework.stereotype.Component;

@Component
public class UniqueKeyExtractor {
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    public void extractUniqueKeys(
        List<String> uniqueKeys,
        Collection<String> studyIdsToReturn
    ) {
        extractUniqueKeys(uniqueKeys, studyIdsToReturn, null);
    }

    public void extractUniqueKeys(
        List<String> uniqueKeys,
        Collection<String> studyIdsToReturn,
        Collection<String> patientOrSampleIdsToReturn
    ) {
        for (String uniqueKey : uniqueKeys) {
            String uniqueId = new String(BASE64_DECODER.decode(uniqueKey));
            String[] patientOrSampleAndStudyId = uniqueId.split(
                UniqueKeyInterceptor.DELIMITER
            );
            if (patientOrSampleAndStudyId.length == 2) {
                if (patientOrSampleIdsToReturn != null) {
                    patientOrSampleIdsToReturn.add(
                        patientOrSampleAndStudyId[0]
                    );
                }
                studyIdsToReturn.add(patientOrSampleAndStudyId[1]);
            }
        }
    }
}
