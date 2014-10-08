package org.mskcc.cbio.importer.extractor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.inject.internal.Lists;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.FoundationMetadata;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 *
 * @author fcriscuo
 * 
*/
/**
 * Represents a FileExtractor subclass responsible for copying FMI XML files
 * from a base directory to subdirectories based on the MSKCC cancer study name.
 * Responsibilities: 
 * 1. for each XML file in the foundation base directory 
 *   a.determine the appropriate subdirectory based on the associated cancer study
 *      (FoundationMetadata) 
 *   b. copy the new XML file to that subdirectory 
 *   c. add the affected cancer study to a Set 
 * 2. return a Set of Foundation cancer studies that have new XML files
 *
 * @author fcriscuo
 */
public class FoundationStudyExtractor {
    private  final FileDataSource inputDataSource;
    private final Config config;
    private final Joiner pathJoiner = Joiner.on(System.getProperty("file.separator"));
    private final Logger logger = Logger.getLogger(FoundationStudyExtractor.class);  
    private final String foundationDataDirectory;

    public FoundationStudyExtractor(final Config aConfig) {
        Preconditions.checkArgument(null != aConfig, "A Config implementation is required");   
        this.config = aConfig;
        this.foundationDataDirectory = this.resolveFoundationDataDirectory();
       this.inputDataSource = this.resolveInputDataSource();
    }
    
    private FileDataSource resolveInputDataSource() {
         try {
            return  new FileDataSource(this.foundationDataDirectory, this.xmlFileExtensionFilter);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
         return null;
    }
    
     private String resolveFoundationDataDirectory() {   
        Collection<DataSourcesMetadata> dsmc = config.getDataSourcesMetadata("foundation");
        if(null == dsmc || dsmc.isEmpty()) {
            logger.error("Configuration error: unable to find FMI data source metadata");
        } else if  (dsmc.size()>1) {
            logger.error("Configuration error: multiple data sources registered for FMI");
        } else {
           return  Lists.newArrayList(dsmc).get(0).getDownloadDirectory();
        }
        return ""; 
    }
     
     Predicate xmlFileExtensionFilter = new Predicate<Path>() {
        @Override
        public boolean apply(Path input) {
            return (input.toString().endsWith("xml"));
        }
     };
     
    private String resolveCancerStudyFromFilename(final String filename) {
        final Collection<FoundationMetadata> mdc = config.getFoundationMetadata();
        final List<String> fileList = Lists.newArrayList(filename);
        List<String> affectedStudyList = FluentIterable.from(mdc)
                .filter(new Predicate<FoundationMetadata>() {
                    @Override
                    public boolean apply(final FoundationMetadata meta) {
                        List<String> fl = FluentIterable.from(fileList).filter(meta.getRelatedFileFilter()).toList();
                        return (!fl.isEmpty());

                    }
                }).transform(new Function<FoundationMetadata, String>() {

                    @Override
                    public String apply(FoundationMetadata f) {
                        return f.getCancerStudy();
                    }
                }).toList();
        // there should only be zero or one match
        if(affectedStudyList.size() == 1) {
            return affectedStudyList.get(0);
        }
        if(affectedStudyList.isEmpty()) {
            logger.error("File " +filename +" is not associated with a registered FMI cancer study and will not be processed" );
        } else {
            logger.error("File " +filename +" is associated with >1 registered FMI Cancer studies and will not be processed");
        }
        // return an empty String
        return "";
    }

    public Set<Path> extractData() throws IOException {
        return this.processFoundationFiles();
    }

    private Path resolveDestinationPath(Path sourcePath) {
        String study = this.resolveCancerStudyFromFilename(sourcePath.toString());
        if(!Strings.isNullOrEmpty(study)){
            try {
                // ensure that required directories exist
                Path subDirPath = Paths.get(pathJoiner.join(this.foundationDataDirectory, study));
                Files.createDirectories(subDirPath);   
                return Paths.get(pathJoiner.join(this.foundationDataDirectory, study,
                        sourcePath.getFileName().toString()));
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
        return sourcePath; // copy the file to itself
    }

      private Path resolveArchivePath(Path sourcePath){
          return Paths.get(pathJoiner.join(this.foundationDataDirectory,"archive", 
                  sourcePath.getFileName().toString()));
      }
      
    /**
     * Process each XML file in the input source. Determine the copy destination
     * from the cancer study the file belongs to, copy the file and rename the
     * original. Add the affected cancer study to the Set
     *
     */
    private Set<Path> processFoundationFiles() throws IOException {
        
               return FluentIterable
                .from(inputDataSource.getFilenameList())
                .transform(new Function<Path, Path>() {
                    @Override
                    public Path apply(Path inPath) {
                        Path outPath = resolveDestinationPath(inPath);
                        
                        try {
                            if (outPath != inPath) {
                                Files.move(inPath, outPath, StandardCopyOption.REPLACE_EXISTING);                          
                            }
                        } catch (IOException ex) {
                            logger.error(ex.getMessage());
                        }

                        return outPath.getParent();
                    }
                }).toSet();
        
    }

   

}
