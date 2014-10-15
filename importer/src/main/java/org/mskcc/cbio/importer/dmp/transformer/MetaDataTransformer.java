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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.Map;
import org.mskcc.cbio.importer.dmp.model.Result;
import org.mskcc.cbio.importer.dmp.support.DMPStagingFileManager;

public class MetaDataTransformer {
   
    public MetaDataTransformer() {    
    }
    
  
    /*
    public static method to transform Result metadata attributes into a 
    sorted map. Note that the keys for initial map are divided into two sections
    to allow sequence specific attributes to be inserted in the right order
    */
    public static Map<String,String> getBaseTransformationMap(Result result) {
        Preconditions.checkArgument(null!=result,"A Result object is required");
        final Map<String, String> metaMap = Maps.newTreeMap();
                // pre-sequence data
                //metaMap.put("001DMP_Sample_ID", result.getMetaData().getDmpSampleId().toString());            
                //metaMap.put("002DMP_Patient_ID", result.getMetaData().getDmpPatientId().toString());
                //metaMap.put("003Tumor_Type", result.getMetaData().getTumorTypeName());
                //metaMap.put("004Gender", (result.getMetaData().getGender() !=null)?result.getMetaData().getGender().toString():"");
                // post-sequence data
                metaMap.put("500Metastasis", (result.getMetaData().getIsMetastasis() !=null)?result.getMetaData().getIsMetastasis().toString():"");
                metaMap.put("501Metastasis_Site", (result.getMetaData().getMetastasisSite() !=null)?result.getMetaData().getMetastasisSite().toString():"");
                metaMap.put("502Sample_Coverage", result.getMetaData().getSampleCoverage().toString());
                metaMap.put("503Signout_Comments", (result.getMetaData().getSoComments() !=null)?result.getMetaData().getSoComments():"");
                metaMap.put("504Signout_Status", (result.getMetaData().getSoStatusName() !=null)?result.getMetaData().getSoStatusName():"");
                metaMap.put("505Tumor_Purity", (result.getMetaData().getTumorPurity()!=null)?result.getMetaData().getTumorPurity().toString():"");
                return metaMap;
    }

}
