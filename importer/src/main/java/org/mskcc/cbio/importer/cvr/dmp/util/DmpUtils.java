package org.mskcc.cbio.importer.cvr.dmp.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.dmp.model.DmpData;
import org.mskcc.cbio.importer.cvr.dmp.model.Result;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;

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
 * Created by criscuof on 11/25/14.
 */
public class DmpUtils {
    private final static Logger logger = Logger.getLogger(DmpUtils.class);
    /*
    utility to remove deprecated data from an existing DMP staging file
     */
    // legacy method signature
    public static void removeDeprecatedSamples( DmpData data, TsvStagingFileHandler fileHandler){
        removeDeprecatedSamples(data,fileHandler,DMPCommonNames.SAMPLE_ID_COLUMN_NAME);
    }
    public static void removeDeprecatedSamples( DmpData data, TsvStagingFileHandler fileHandler, String sampleColumnName){
        Preconditions.checkArgument(null != data, "A DmpData object is required");
        Preconditions.checkArgument(null != fileHandler, "A file fandler implementation is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(sampleColumnName),
                "The name of the sample column is required");
        // identify samples that have been marked as deprecated
        Set<String> deprecatedSamples = resolveDeprecatedSamples(data);
        if (!deprecatedSamples.isEmpty()) {
           fileHandler.removeDeprecatedSamplesFomTsvStagingFiles(sampleColumnName,
                   deprecatedSamples);
            logger.info(deprecatedSamples.size() +" deprecated samples have been removed");
        }
    }
    public static Set<String> resolveDeprecatedSamples(DmpData data){
        Preconditions.checkArgument(null!= data ,
                "DMP data are required");
        return  FluentIterable.from(data.getResults())
                .filter(new Predicate<Result>() {
                    @Override
                    public boolean apply(Result result) {
                        return (result.getMetaData().getRetrieveStatus() == DMPCommonNames.DMP_DATA_STATUS_RETRIEVAL) ;
                    }
                })
                .transform(new Function<Result, String>() {
                    @Override
                    public String apply(Result result) {
                        return result.getMetaData().getDmpSampleId();
                    }
                })
                .toSet();
    }
}
