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
import com.google.common.collect.Sets;
import com.google.inject.internal.Lists;
import com.google.inject.internal.Preconditions;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.dmp.model.DmpData;
import org.mskcc.cbio.importer.dmp.model.Result;
import org.mskcc.cbio.importer.dmp.persistence.file.DMPStagingFileManager;
import org.mskcc.cbio.importer.dmp.persistence.file.DMPTumorTypeSampleMapManager;

/*
 Responsible for transforming the DMP data encapsulated in the DmpData object
 graph into a set of MAF files
 Inputs: 1. The DMP data as a Java object graph
 2. A reference to a DMPStagingFileManagerOld for writing the DMP staging
 data
 3. A List of DMPTransformable implementations responsible for 
 transforming specific components of the DMP data
 */
public class DMPDataTransformer {

    private final static Logger logger = Logger.getLogger(DMPDataTransformer.class);
    private final DMPStagingFileManager fileManager;
    private final List<DMPDataTransformable> transformableList;
    private final DMPTumorTypeSampleMapManager tumorTypeMap;

    public DMPDataTransformer(DMPStagingFileManager aManager) {
        Preconditions.checkArgument(null != aManager, "A DMPStagingFileManager is required");

        this.fileManager = aManager;
        // instantiate and register data transformers
        this.transformableList = Lists.newArrayList((DMPDataTransformable) new DmpSnpTransformer(this.fileManager));

        this.tumorTypeMap = new DMPTumorTypeSampleMapManager(this.fileManager);

    }
    /*
     transform the DMP data into variant type-specific MAF files
     return a Set of processed SMP sample ids
     */

    public Set<String> transform(DmpData data) {
        Preconditions.checkArgument(null != data, "DMP data is required for transformation");
        /*
         screen current DMP input sample list for previously processed sample ids
         */
        Set<String> processedSampleSet = FluentIterable.from(data.getResults())
                .transform(new Function<Result, String>() {
                    @Override
                    public String apply(Result result) {
                        return result.getMetaData().getDmpSampleId().toString();
                    }
                }).toSet();

        // update the tumor type-sample map
        // filter exisiting DMP MAF files for deprecated DMP samples
        this.processRevisedSampleData(processedSampleSet);
        // process the tumor types
        this.tumorTypeMap.updateTumorTypeSampleMap(data.getResults());
        // invoke the type specific transformers on the DMP data
        for (DMPDataTransformable transformable : this.transformableList) {
            transformable.transform(data);
        }

        return processedSampleSet;
    }

    private void processRevisedSampleData(Set<String> currentSampleList) {
        // look for intersection between previously processed samples and current sample set
        Set<String> deprecatedSampleSet = Sets.intersection(currentSampleList, fileManager.getProcessedSampleSet());
        logger.info(deprecatedSampleSet.size() + " samples in the current input deprecate existing DMP samples");
        fileManager.removeDeprecatedSamplesFomStagingFiles(deprecatedSampleSet);

    }

}
