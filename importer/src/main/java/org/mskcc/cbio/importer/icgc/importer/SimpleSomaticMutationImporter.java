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
package org.mskcc.cbio.importer.icgc.importer;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;

import org.mskcc.cbio.importer.icgc.analytics.ICGCSummaryTable;
import org.mskcc.cbio.importer.icgc.support.IcgcImportService;

/*
 responsible for invoking ETL operations for simple somatic ICGC studys
 primary input is a list of ICGC studys

 */
public class SimpleSomaticMutationImporter {

    /*
    responsible for:
        1. generating a List of URLs for ICGC simple somatic mutation files
        2. instantiating a SimpleSomaticFileTransformer object
        3. invoking multiple IcgcStudyFileETL operations to import ICGC data

     */

    private static Logger logger = Logger.getLogger(SimpleSomaticMutationImporter.class);
    private static final Integer ETL_THREADS = 4;
    private List<String> simpleSomaticStudyList;
    private Path baseStagingPath;

    public SimpleSomaticMutationImporter( final Path aPath) {
        Preconditions.checkArgument(null != aPath,
                "A Path to the head of the staging file area is required");
        // get simple somatic mutation URLs for registered studies
        Map<String, String> urlMap = IcgcImportService.INSTANCE.getIcgcMutationUrlMap();


    }

    
    public  List<Path> processSimpleSomaticMutations() {
     //  List<Path> mafList = this.etl.processICGCStudies(simpleSomaticStudyList, destPath,
         //      new SimpleSomaticFileTransformer(new MutationFileHandlerImpl(), destPath));
       // complete summary ststistics
       ICGCSummaryTable table = new ICGCSummaryTable(baseStagingPath.toString());
       logger.info("Summary statistics completed");
       return null;
    }

    /*
    main method for testing
    */
    public static void main(String...args){
        // Base URls for ICGC non-US studies - requires editing to specific file type by Importer

        Map<String, String> urlMap = IcgcImportService.INSTANCE.getIcgcMutationUrlMap();
        // test Path
         Path p = Paths.get("/tmp/asynctest");
        SimpleSomaticMutationImporter controller = new SimpleSomaticMutationImporter( p);
      List<Path> mafPathList = controller.processSimpleSomaticMutations();
      for(Path path : mafPathList){
          logger.info("MAF File: " +path.toString());
      }
      logger.info("Finis");
        
    }

}
