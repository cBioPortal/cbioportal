package org.cbioportal.persistence.spark.util;

import org.cbioportal.model.Sample;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SampleTypeUtil {

    public static Sample.SampleType getType(String stableId, String sampleType) {
        Matcher tcgaSampleBarcodeMatcher = Pattern.compile("^TCGA-\\w\\w-\\w\\w\\w\\w-(\\d\\d).*$").matcher(stableId);
        if (tcgaSampleBarcodeMatcher.find()) {
            String tcgaCode =tcgaSampleBarcodeMatcher.group(1);
            if (tcgaCode.equals("01")) {
                return Sample.SampleType.PRIMARY_SOLID_TUMOR;
            }
            else if (tcgaCode.equals("02")) {
                return Sample.SampleType.RECURRENT_SOLID_TUMOR;
            }
            else if (tcgaCode.equals("03")) {
                return Sample.SampleType.PRIMARY_BLOOD_TUMOR;
            }
            else if (tcgaCode.equals("04")) {
                return Sample.SampleType.RECURRENT_BLOOD_TUMOR;
            }
            else if (tcgaCode.equals("06")) {
                return Sample.SampleType.METASTATIC;
            }
            else if (tcgaCode.equals("10")) {
                return Sample.SampleType.BLOOD_NORMAL;
            }
            else if (tcgaCode.equals("11")) {
                return Sample.SampleType.SOLID_NORMAL;
            }
            else {
                return Sample.SampleType.PRIMARY_SOLID_TUMOR;
            }
        }
        else if (sampleType != null && Sample.SampleType.fromString(sampleType) != null) {
            return Sample.SampleType.fromString(sampleType.toUpperCase());
        }
        else {
            return Sample.SampleType.PRIMARY_SOLID_TUMOR;
        }
    }
}
