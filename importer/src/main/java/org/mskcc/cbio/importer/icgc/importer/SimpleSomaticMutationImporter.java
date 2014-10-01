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

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.inject.internal.Lists;
import com.google.inject.internal.Preconditions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.etl.IcgcStudyFileETL;
import org.mskcc.cbio.importer.icgc.analytics.ICGCSummaryTable;
import org.mskcc.cbio.importer.icgc.support.IcgcImportService;
import org.mskcc.cbio.importer.icgc.support.IcgcStudyBaseUrlMapFromFileSupplier;
import org.mskcc.cbio.importer.icgc.transformer.SimpleSomaticFileTransformer;

/*
 responsible for invoking ETL operations for simple somatic ICGC studys
 primary input is a list of ICGC studys

 */
public class SimpleSomaticMutationImporter {

    private static Logger logger = Logger.getLogger(SimpleSomaticMutationImporter.class);
    private static final Integer EXTRACTOR_THREADS = 4;
    private List<String> simpleSomaticStudyList;
    private Path destPath;
    //private  IcgcSimpleSomaticMutationETL etl;
    private  IcgcStudyFileETL etl;
    
    
    public SimpleSomaticMutationImporter(final List<String> baseUrlList, final Path destDir) {
        
        Preconditions.checkArgument(null != baseUrlList && baseUrlList.size() > 0,
                "A list of ICGC Study URLs is required");
        Preconditions.checkArgument(null != destDir,
                "A destination directory for ICGC study data is required");
        Preconditions.checkArgument(Files.isDirectory(destDir)
                && Files.isDirectory(destDir),
                "The specified directory " + destDir + " is invalid");
        this.destPath = destDir;
       
        // edit mutation type in list
        this.simpleSomaticStudyList = FluentIterable.from(baseUrlList)
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String f) {
                        return f.replaceAll(IcgcImportService.INSTANCE.MUTATION_TYPE,
                                IcgcImportService.INSTANCE.SIMPLE_SOMATIC_MUTATION_TYPE);
                    }
                }).toList();
        this.etl = new IcgcStudyFileETL(4);
    }
    
    private void dispose() {
        this.etl.dispose();
    }
    
    public  List<Path> processSimpleSomaticMutations() {
       List<Path> mafList = this.etl.processICGCStudies(simpleSomaticStudyList, destPath,
               new SimpleSomaticFileTransformer());
       // complete summary ststistics
       ICGCSummaryTable table = new ICGCSummaryTable(destPath.toString());
       logger.info("Summary statistics completed");
       return mafList;
    }

    /*
    main method for testing
    */
    public static void main(String...args){
        // Base URls for ICGC non-US studies - requires editing to specific file type by Importer
        Supplier<Map<String, String>> supplier = Suppliers.memoize(new IcgcStudyBaseUrlMapFromFileSupplier());
        Map<String, String> urlMap = supplier.get();
        // test Path
         Path p = Paths.get("/tmp/asynctest");
        SimpleSomaticMutationImporter controller = new SimpleSomaticMutationImporter(
            Lists.newArrayList(urlMap.values()), p);
      List<Path> mafPathList = controller.processSimpleSomaticMutations();
      for(Path path : mafPathList){
          logger.info("MAF File: " +path.toString());
      }
      logger.info("Finis");
        
    }

}
