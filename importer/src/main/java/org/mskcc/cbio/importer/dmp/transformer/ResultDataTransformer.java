/*
 *  Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * 
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 *  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 *  documentation provided hereunder is on an "as is" basis, and
 *  Memorial Sloan-Kettering Cancer Center 
 *  has no obligations to provide maintenance, support,
 *  updates, enhancements or modifications.  In no event shall
 *  Memorial Sloan-Kettering Cancer Center
 *  be liable to any party for direct, indirect, special,
 *  incidental or consequential damages, including lost profits, arising
 *  out of the use of this software and its documentation, even if
 *  Memorial Sloan-Kettering Cancer Center 
 *  has been advised of the possibility of such damage.
 */
package org.mskcc.cbio.importer.dmp.transformer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.util.Map;
import org.mskcc.cbio.importer.dmp.model.Result;

public class ResultDataTransformer {

    private final Result result;
    

    public ResultDataTransformer(Result aResult) {
        Preconditions.checkArgument(null != aResult, "A Result object is required");
        this.result = aResult;
          
    }
    
    public Map<String,String> getBaseTransformationMap() {
        final Map<String, String> metaMap = Maps.newTreeMap();
                metaMap.put("001Tumor_Type", this.result.getMetaData().getTumorTypeName());
                metaMap.put("002DMP_Patient_ID", this.result.getMetaData().getDmpPatientId().toString());
                metaMap.put("003DMP_Sample_ID", this.result.getMetaData().getDmpSampleId().toString());
                metaMap.put("004Gender", (this.result.getMetaData().getGender() !=null)?this.result.getMetaData().getGender().toString():"");
                metaMap.put("500Metastasis", (this.result.getMetaData().getIsMetastasis() !=null)?this.result.getMetaData().getIsMetastasis().toString():"");
                metaMap.put("501Metastasis_Site", (this.result.getMetaData().getMetastasisSite() !=null)?this.result.getMetaData().getMetastasisSite().toString():"");
                metaMap.put("502Sample_Coverage", this.result.getMetaData().getSampleCoverage().toString());
                metaMap.put("503Signout_Comments", (this.result.getMetaData().getSoComments() !=null)?this.result.getMetaData().getSoComments():"");
                metaMap.put("504Signout_Status", (this.result.getMetaData().getSoStatusName() !=null)?this.result.getMetaData().getSoStatusName():"");
                metaMap.put("505Tumor_Purity", (this.result.getMetaData().getTumorPurity()!=null)?this.result.getMetaData().getTumorPurity().toString():"");
                return metaMap;
    }
    

}
