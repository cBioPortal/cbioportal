package org.mskcc.cbio.importer.persistence.staging.clinical;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import org.mskcc.cbio.importer.dmp.util.DmpUtils;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by fcriscuo on 11/9/14.
 */
public abstract class ClinicalDataModel {

    /*
     public static final String[] CLINICAL_DATA_HEADINGS = { "SAMPLE_ID","GENDER",   "FMI_CASE_ID", "PIPELINE_VER",
        						"TUMOR_NUCLEI_PERCENT", "MEDIAN_COV", "COV>100X", "ERROR_PERCENT" };
     */
    public static final Map<String,String> transformationMap = Maps.newTreeMap();
    static {
        transformationMap.put("001SAMPLE_ID","getSampleId");
        transformationMap.put("002GENDER","getGender");
        transformationMap.put("003STUDY_ID","getStudyId");
        transformationMap.put("004PIPELINE_VER","getPipelineVersion");
        transformationMap.put("005TUMOR_NUCLEI_PERCENT", "getTumorNucleiPercent");
        transformationMap.put("006MEDIAN_COV","getMedianCoverage");
        transformationMap.put("COV>100X", "get100XCov");
        transformationMap.put("ERROR_PERCENT","getErrorPercent");

    }
    public static List<String> resolveColumnNames() {
        return FluentIterable.from(transformationMap.keySet())
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return (s.substring(3)); // strip off the three digit numeric prefix
                    }
                }).toList();
    }

    /*
    abstract getters that a subclass must implement
     */

    public abstract String getSampleId();
    public abstract String getGender();
    public abstract String getStudyId();
    public abstract String getPipelineVersion();
    public abstract String getTumorNucleiPercent();
    public abstract String getMedianCoverage();
    public abstract String get100XCov();
    public abstract String getErrorPercent();

    /*
 Function to transform attributes from a  Foundation Short Variant object into MAF attributes collected in
 a tsv String for subsequent output
 */
    final static Function<ClinicalDataModel, String> transformationFunction = new Function<ClinicalDataModel, String>() {
        @Override
        public String apply(final ClinicalDataModel cdm) {
            Set<String> attributeList = transformationMap.keySet();
            List<String> mafAttributes = FluentIterable.from(attributeList)
                    .transform(new Function<String, String>() {
                        @Override
                        public String apply(String attribute) {
                            String getterName = transformationMap.get(attribute);
                            return DmpUtils.pojoStringGetter(getterName, cdm);

                        }
                    }).toList();
            String retRecord = StagingCommonNames.tabJoiner.join(mafAttributes);

            return retRecord;
        }

    };

    /*
     provide access to the transformation function
      */
    public static Function<ClinicalDataModel,String> getTransformationFunction () { return transformationFunction;}


}
