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

import com.google.common.collect.FluentIterable;
import com.google.inject.internal.Lists;
import com.google.inject.internal.Preconditions;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;

import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.dmp.model.DmpData;
import org.mskcc.cbio.importer.dmp.model.Result;
import org.mskcc.cbio.importer.dmp.persistence.file.DMPTumorTypeSampleMapManager;
import org.mskcc.cbio.importer.persistence.staging.*;

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
    
    private final List<DMPDataTransformable> transformableList;
    private  DMPTumorTypeSampleMapManager tumorTypeMap;

    public DMPDataTransformer(Path stagingDirectoryPath) {
        
        com.google.common.base.Preconditions.checkArgument
        (null != stagingDirectoryPath,
                "A Path to the staging file directory is required");
        com.google.common.base.Preconditions.checkArgument
        (Files.isDirectory(stagingDirectoryPath, LinkOption.NOFOLLOW_LINKS),
                "The specified Path: " + stagingDirectoryPath + " is not a directory");
        com.google.common.base.Preconditions.checkArgument
        (Files.isWritable(stagingDirectoryPath),
                "The specified Path: " + stagingDirectoryPath + " is not writable");
        // instantiate and register data transformers
        //SNPs
        this.transformableList = Lists.newArrayList((DMPDataTransformable) 
                new DmpSnpTransformer( new MutationFileHandlerImpl(),
                        stagingDirectoryPath));
       //CNVs
        this.transformableList.add((DMPDataTransformable) 
                new DmpCnvTransformer( new CnvFileHandlerImpl(), 
                        stagingDirectoryPath));
        //Metadata
        this.transformableList.add((DMPDataTransformable) new DmpMetadataTransformer 
            ( new ClinicalDataFileHandlerImpl(), stagingDirectoryPath));

        // segment data
        this.transformableList.add( new SegmentDataTransformer(new SegmentFileHandlerImpl(),stagingDirectoryPath));
       // this.tumorTypeMap = new DMPTumorTypeSampleMapManager(this.fileManager);

    }
    /*
     transform the DMP data into variant type-specific MAF files
     return a Set of processed SMP sample ids
     */

    public List<String> transform(DmpData data) {
        Preconditions.checkArgument(null != data, "DMP data is required for transformation");
        
        // process the tumor types
        //this.tumorTypeMap.updateTumorTypeSampleMap(data.getResults());
        // invoke the type specific transformers on the DMP data
        for (DMPDataTransformable transformable : this.transformableList) {
            transformable.transform(data);
        }

        return FluentIterable.from(data.getResults())
        .transform(new com.google.common.base.Function<Result, String>() {
            @Override
            public String apply(Result result) {
                return result.getMetaData().getDmpSampleId();
            }
        }).toList();
    }

  

}
