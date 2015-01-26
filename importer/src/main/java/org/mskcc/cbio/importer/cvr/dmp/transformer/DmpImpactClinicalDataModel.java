package org.mskcc.cbio.importer.cvr.dmp.transformer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.dmp.model.MetaData;
import org.mskcc.cbio.importer.cvr.dmp.util.DMPCommonNames;
import org.mskcc.cbio.importer.persistence.staging.clinical.ImpactClinicalDataModel;

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
 * Created by criscuof on 1/25/15.
 */
public class DmpImpactClinicalDataModel extends ImpactClinicalDataModel {
    private static final Logger logger = Logger.getLogger(DmpImpactClinicalDataModel.class);
    private final MetaData metaData;

    public DmpImpactClinicalDataModel(MetaData md) {
        Preconditions.checkArgument(null != md, "A MetaData object is required");
        this.metaData = md;
    }

    @Override
    public String getSampleId() {
        return metaData.getDmpSampleId();
    }

    @Override
    public String getPatientId() {
        return this.metaData.getDmpPatientId();
    }

    @Override
    public String getCancerType() {
        return this.metaData.getTumorTypeName();
    }

    @Override
    public String getSampleType() {
        return resolveSampleType.apply(metaData.getIsMetastasis());
    }

    @Override
    public String getSampleClass() {
        return DMPCommonNames.DEFAULT_SAMPLE_TYPE;
    }

    @Override
    public String getMetastaticSite() {

        if(null == this.metaData.getIsMetastasis()){
            return "Not Applicable";
        }
        return this.metaData.getMetastasisSite().toString();
    }

    @Override
    public String getPrimarySite() {
        return "";
    }

    @Override
    public String getCancerTypeDetailed() {
        return "";
    }

    @Override
    public String getKnownMolecularClassifier() {
        return "";
    }

    /*
       resolve the sample type based on the is_metastastis attribute value
       */
    Function<Object, String> resolveSampleType
            = new Function<Object, String>() {
        @Override
        public String apply(Object obj) {
            if (null == obj) {
                return "";
            }
            return (obj.toString().equalsIgnoreCase(DMPCommonNames.IS_METASTASTIC_SITE))
                    ? "Metastasis" : "Primary";
        }

    };


}
