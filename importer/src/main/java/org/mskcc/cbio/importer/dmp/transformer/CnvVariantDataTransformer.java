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
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.gdata.util.common.base.Joiner;
import com.google.gdata.util.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import org.mskcc.cbio.importer.dmp.model.CnvVariant;
import org.mskcc.cbio.importer.dmp.model.Result;
import org.mskcc.cbio.importer.dmp.support.DMPCommonNames;
import org.mskcc.cbio.importer.dmp.support.DMPStagingFileManager;
import org.mskcc.cbio.importer.dmp.util.DmpUtils;

public class CnvVariantDataTransformer  {

    private static final String REPORT_TYPE = DMPCommonNames.REPORT_TYPE_CNV;
    public static final Joiner tabJoiner = Joiner.on("\t");
     private static final Map<String, String> attributeMap = DmpUtils.reportTypeAttributeMaps.get(REPORT_TYPE);
    
    private Map<String, String> baseMap;

    public CnvVariantDataTransformer() {
    }

   
    public void transform(Result result, DMPStagingFileManager fileManager) {

        Preconditions.checkArgument(null != result, "A DMP Result object is required");
        Preconditions.checkArgument(null != fileManager, "A DMPStagingFileManager is required");
        if (result.getCnvVariants().isEmpty()) {
            return;
        }
        this.baseMap = MetaDataTransformer.getBaseTransformationMap(result);
        fileManager.appendDMPDataToStagingFile(REPORT_TYPE, result.getCnvVariants(), transformationFunction);
    }
   
    
    public Function getTransormationFunction() {
        return this.transformationFunction2;
    }

    public Function<CnvVariant, String> transformationFunction
            = new Function<CnvVariant, String>() {
                @Override
                public String apply(CnvVariant cnv) {
                    Map<String, String> cnvMap = Maps.newTreeMap();
                    cnvMap.putAll(baseMap);
                   for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
                        cnvMap.put(entry.getKey(), DmpUtils.pojoStringGetter(entry.getValue(), cnv));
                    }
                    return tabJoiner.join(cnvMap.values());
                }
            };
    
    
     public Function<Result, List<String>> transformationFunction2
            = new Function<Result, List<String>>() {
        @Override
        public List<String> apply(Result result) {
            final Map<String,String> baseMap = MetaDataTransformer.getBaseTransformationMap(result);
            return FluentIterable.from(result.getCnvVariants())
                    .transform(transformationFunction)
                    .toList();
        }           
            };

}

