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
import com.google.common.collect.Maps;
import com.google.gdata.util.common.base.Joiner;
import com.google.gdata.util.common.base.Preconditions;
import java.util.Map;

import org.mskcc.cbio.importer.dmp.model.Result;
import org.mskcc.cbio.importer.dmp.model.SnpExonic;
import org.mskcc.cbio.importer.dmp.support.DMPCommonNames;
import org.mskcc.cbio.importer.dmp.support.DMPStagingFileManager;
import org.mskcc.cbio.importer.dmp.util.DmpUtils;

public class SnpExonicDataTransformer implements DMPTransformable {

    private static final String REPORT_TYPE = DMPCommonNames.REPORT_TYPE_SNP_EXONIC;
    public static final Joiner tabJoiner = Joiner.on("\t");
    private static final Map<String, String> attributeMap = DmpUtils.reportTypeAttributeMaps.get(REPORT_TYPE);
    
    private Map<String, String> baseMap;

    public SnpExonicDataTransformer() {
    }

    @Override
    public void transform(Result result, DMPStagingFileManager fileManager) {

        Preconditions.checkArgument(null != result, "A DMP Result object is required");
        Preconditions.checkArgument(null != fileManager, "A DMPStagingFileManager is required");
        if (result.getSnpExonic().isEmpty()) {
            return;
        }
        this.baseMap = MetaDataTransformer.getBaseTransformationMap(result);

        fileManager.appendDMPDataToStagingFile(REPORT_TYPE, result.getSnpExonic(), transformationFunction);

    }

    @Override
    public Function getTransormationFunction() {
        return this.transformationFunction;
    }

    public Function<SnpExonic, String> transformationFunction
            = new Function<SnpExonic, String>() {

                @Override
                public String apply(SnpExonic snp) {
                    Map<String, String> snpMap = Maps.newTreeMap();
                    snpMap.putAll(baseMap);
                    for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
                        snpMap.put(entry.getKey(), DmpUtils.pojoStringGetter(entry.getValue(), snp));
                    }

                    return tabJoiner.join(snpMap.values());
                }

            };

}
