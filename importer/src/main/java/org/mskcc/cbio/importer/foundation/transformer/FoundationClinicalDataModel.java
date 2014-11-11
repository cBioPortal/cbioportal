package org.mskcc.cbio.importer.foundation.transformer;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.mskcc.cbio.foundation.jaxb.CaseType;
import org.mskcc.cbio.foundation.jaxb.MetricType;
import org.mskcc.cbio.foundation.jaxb.MetricsType;
import org.mskcc.cbio.importer.foundation.support.CommonNames;
import org.mskcc.cbio.importer.persistence.staging.ClinicalDataModel;

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
public class FoundationClinicalDataModel  extends ClinicalDataModel {
    /*
    responsible for encapsulating Foundation clinical data into a data value
    object that supports the ClinicalDataModel contract.
     */

    private final static Logger logger = Logger.getLogger(FoundationClinicalDataModel.class);
    private final CaseType caseType;

    public FoundationClinicalDataModel(CaseType caseType){
        super();
        Preconditions.checkArgument(null != caseType, "A Foundation CaseType is required");
        Preconditions.checkArgument(null!=caseType.getVariantReport(),"The Foundation CaseType is invalid");
        this.caseType = caseType;
    }

    @Override
    public String getSampleId() {
        return  this.caseType.getCase();
    }

    @Override
    public String getGender() {
        return (Strings.isNullOrEmpty(caseType.getVariantReport().getGender()) ?"":
                caseType.getVariantReport().getGender());
    }

    @Override
    public String getStudyId() {
        return caseType.getFmiCase();
    }

    @Override
    public String getPipelineVersion() {
        return (Strings.isNullOrEmpty(caseType.getVariantReport().getPipelineVersion()) ?"":
                caseType.getVariantReport().getPipelineVersion());
    }

    @Override
    public String getTumorNucleiPercent() {
        return  displayMetricValue(caseType, CommonNames.METRIC_TUMOR_NUCLEI_PERCENT);
    }

    @Override
    public String getMedianCoverage() {
        return displayMetricValue(caseType, CommonNames.METRIC_MEDIAN_COVERAGE);
    }

    @Override
    public String get100XCov() {
        return displayMetricValue(caseType, CommonNames.METRIC_COVERAGE_GT_100);
    }

    @Override
    public String getErrorPercent() {
        return displayMetricValue(caseType, CommonNames.METRIC_ERROR);
    }

    /**
     * private method to provide a String representation of a Foundation Metric value
     *
     * @param caseType
     * @param metricName
     * @return
     */
    private String displayMetricValue(CaseType caseType, String metricName) {
        MetricsType metricsType = caseType.getVariantReport().getQualityControl().getMetrics();
        for (MetricType metric : metricsType.getMetric()) {
            if (metric.getName().equals(metricName)) {
                return metric.getMetricTypeValue();
            }
        }
        return "";
    }



}
