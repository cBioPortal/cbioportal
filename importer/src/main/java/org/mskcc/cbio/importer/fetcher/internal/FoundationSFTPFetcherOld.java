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

package org.mskcc.cbio.importer.fetcher.internal;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.internal.Preconditions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import joptsimple.internal.Strings;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.foundation.support.FoundationMetaDataGenerator;
import org.mskcc.cbio.importer.transformer.FoundationXMLTransformer;
import org.mskcc.cbio.importer.model.ReferenceMetadata;


public class FoundationSFTPFetcherOld implements Fetcher{
    
   
    
    private final Logger logger = Logger.getLogger(FoundationSFTPFetcherOld.class);
    
    private final String sourceDirectoryName;
    private final String outputBaseDir;
    private final Config config;
    private final FoundationMetaDataGenerator metaDataGenerator;
    
    // constructor consistent with portal_importer_configuration constructor_args
    public FoundationSFTPFetcherOld(Config config,String sourceDirectoryName ,String outDir, FoundationMetaDataGenerator meta){
        // check for arguments to support non-Spring invocations
        Preconditions.checkArgument(null != config, "ERROR - Config implementation is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(sourceDirectoryName), "ERROR: a source directory name is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(outDir), "ERROR: an output direct is required");
        this.config = config;
        this.sourceDirectoryName = sourceDirectoryName;
        this.outputBaseDir = outDir;
        this.metaDataGenerator = meta;
    }
    

    @Override
    public void fetch(String dataSource, String desiredRunDate) throws Exception {
       logger.info("fetch operation for Foundation Medicine files invoked");
       // find new XML files
       List<Path> newFoundationFiles = this.determineNewFoundationXMLFiles(this.sourceDirectoryName);
       if(newFoundationFiles.isEmpty()){
           logger.info("No new XML files were found in " +this.sourceDirectoryName);
           return;
       }
       for(Path xmlPath : newFoundationFiles){
           // use the Foundation XML Parser to generate the data files
           FoundationXMLTransformer transformer = new FoundationXMLTransformer(xmlPath.toString(), this.outputBaseDir);
           transformer.processFoundationData();
           //Integer numCases = transformer.getNumCases();
           Integer numCases = 0;
           this.metaDataGenerator.generateCancerStudyMetaData(transformer.getStudyId(), numCases);
           
       }
    }

    @Override
    public void fetchReferenceData(ReferenceMetadata referenceMetadata) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
   
    
    /**
     * private method to list new 
     */
    private List<Path> determineNewFoundationXMLFiles(String dirName) throws IOException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dirName), "A directory name is required");
        Preconditions.checkArgument(Files.isDirectory(Paths.get(dirName)), dirName +" is not a directory");
        // currently any XML file in source directoy is considered new
        // TODO: filter on date
        List<Path> xmlFileList = FluentIterable
                .from(Files.newDirectoryStream((Paths.get(dirName) )))
                .filter(new Predicate<Path>(){
                     @Override
                        public boolean apply(Path input) {
                           return (input.toString().endsWith("xml"));
                         }
                })
                .toList();
        
        return xmlFileList;
    }
   

}
