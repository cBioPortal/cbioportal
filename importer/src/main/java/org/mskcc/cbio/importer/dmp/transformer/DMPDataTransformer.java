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
import com.google.inject.internal.Preconditions;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.dmp.model.DmpData;
import org.mskcc.cbio.importer.dmp.model.Result;
import org.mskcc.cbio.importer.dmp.support.DMPStagingFileManager;
import scala.Tuple2;

/*
 Responsible for transforming the DMP data encapsulated in the DmpData object
 graph into a set of MAF files
 Inputs: 1. The DMP data as a Java object graph
 2. A reference to a DMPStagingFileManager for writing the DMP staging
 data
 3. A List of DMPTransformable implementations responsible for 
 transforming specific components of the DMP data
 */
public class DMPDataTransformer {

    private final static Logger logger = Logger.getLogger(DMPDataTransformer.class);
    private final DMPStagingFileManager fileManager;
    private final List<DMPTransformable> transformableList;

    public DMPDataTransformer(DMPStagingFileManager aManager, List<DMPTransformable> transList) {
        Preconditions.checkArgument(null != aManager, "A DMPStagingFileManager is required");
        Preconditions.checkArgument(null != transList && !transList.isEmpty(), 
                "A valid list of DMPTransformable implemntations is required");
        this.fileManager = aManager;
        this.transformableList = transList;
        
    }
    
    public void transform(DmpData data){
        Preconditions.checkArgument(null != data, "DMP data is required for transformation");
        for (Result result : data.getResults()){
           for(DMPTransformable transformable : this.transformableList){
               logger.info("Transforming result " +result.getMetaData().getDmpSampleId() +" using " +transformable.getClass().getName());
               transformable.transform(result, fileManager);
           }
            
        }
    }

    
    

}
