package org.mskcc.cbio.importer.foundation.extractor;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.inject.internal.Lists;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.config.internal.ImporterSpreadsheetService;
import org.mskcc.cbio.importer.model.*;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;

import javax.annotation.Nullable;

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
 * Represents a a file extractor responsible for copying FMI XML files
 * from a download directory to subdirectories based on the MSKCC cancer study name.
 * Responsibilities: 
 * 1. for each XML file in the foundation download directory
 *   a.determine the appropriate subdirectory based on the associated cancer study
 *      (FoundationMetadata) 
 *   b. move the new XML file to that subdirectory
 *   c. add the affected cancer study to a Set 
 * 2. return a Set of Foundation cancer studies that have new XML files
 *
 * @author fcriscuo
 */
public class        FoundationStudyExtractor {

    private  final FileDataSource inputDataSource;
    private  Config config;
    private final static Logger logger = Logger.getLogger(FoundationStudyExtractor.class);
    private final String foundationDataDirectory;

    private final static String DEFAULT_DOWNLOAD_DIRECTORY = "/tmp/foundation";
    private final static String DEFAULT_DATA_SOURCE = "foundation-dev";
    private final static String worksheetName = "foundation";
    private String foundationDataSource;

    public FoundationStudyExtractor() {
        this(DEFAULT_DATA_SOURCE);
    }

    public FoundationStudyExtractor(String dataSourceName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dataSourceName),
                "A importer data source name is required");
        this.foundationDataSource = dataSourceName;
      Optional<DataSourcesMetadata> dsMeta =
        DataSourcesMetadata.findDataSourcesMetadataByDataSourceName(dataSourceName);
        if(dsMeta.isPresent()) {
            this.foundationDataDirectory = dsMeta.get().getDownloadDirectory();
        } else {
            this.foundationDataDirectory = DEFAULT_DOWNLOAD_DIRECTORY;
        }
        // duplicate files for filtered studies
        StagingUtils.copyFilteredXMLFiles(dsMeta.get().resolveBaseStagingDirectory());
        Optional<FileDataSource> fds  = this.resolveInputDataSource();
        if (fds.isPresent()) {
            this.inputDataSource = fds.get();
            logger.info("FDS diretcory: " + this.inputDataSource.getDirectoryName());
            for(Path p : this.inputDataSource.getFilenameList()){
                logger.info("XML file " +p.toString());
            }

        } else {
            this.inputDataSource = null;  // TODO: fix this
        }
    }

    public FoundationStudyExtractor(final Config aConfig) {
        Preconditions.checkArgument(null != aConfig, "A Config implementation is required");   
        this.config = aConfig;
        Optional<String> dd = this.resolveFileDownloadDirectoryFromConfig();
        if (dd.isPresent()){
            this.foundationDataDirectory = dd.get();
        } else {
            this.foundationDataDirectory = "/tmp"; // divert search to temp
        }
        Optional<FileDataSource> fds  = this.resolveInputDataSource();
        if (fds.isPresent()) {
            this.inputDataSource = fds.get();
            logger.info("FDS diretcory: " + this.inputDataSource.getDirectoryName());
            for(Path p : this.inputDataSource.getFilenameList()){
                logger.info("XML file " +p.toString());
            }

        } else {
            this.inputDataSource = null;  // TODO: fix this
        }
    }

    private Optional<FileDataSource> resolveInputDataSource() {
         try {
            return  Optional.of(new FileDataSource(this.foundationDataDirectory, this.xmlFileExtensionFilter));
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
         return Optional.absent();
    }
    /*
    private method to return the download directory name for FMI files defined in the config
    encapsulate the String in an Optional so that caller is aware that it may not be defined.
     */
     private Optional<String> resolveFileDownloadDirectoryFromConfig() {
         Optional<DataSourcesMetadata> dsMetaOpt = DataSourcesMetadata.findDataSourcesMetadataByDataSourceName(foundationDataSource);
        if (dsMetaOpt.isPresent()){
            return Optional.of(dsMetaOpt.get().getDownloadDirectory());
        }
        return Optional.absent();
    }
     
     Predicate xmlFileExtensionFilter = new Predicate<Path>() {
        @Override
        public boolean apply(Path input) {
            return (input.toString().endsWith("xml"));
        }
     };
     /*
     private method to determine the Foundation cancer study name from the XML file name
     Usually only a subset of the file name is registered in the config  data
     return an Optional to deal with cases where a study could not be found
      */
    private Optional<String> resolveFoundationCancerStudyNameFromXMLFileName(final String filename) {

       /*
       obtain a list of FMI cancer studies from the foundation worksheet
        */
        List<String> cancerStudyList = ImporterSpreadsheetService.INSTANCE.getWorksheetValuesByColumnName(FoundationMetadata.worksheetName,
                FoundationMetadata.cancerStudyColumnName);

        /*
        for each FMI cancer study, determine if one of its dependencies forms a unique portion of the file name
         if so, return the name of that study
         */
        return FluentIterable.from(cancerStudyList)
                .transform(new Function<String, FoundationMetadata>() {
                    @Nullable
                    @Override
                    public FoundationMetadata apply(String cs) {
                        Optional<Map<String,String>> optMap =
                                ImporterSpreadsheetService.INSTANCE.getWorksheetRowByColumnValue(FoundationMetadata.worksheetName,
                                        FoundationMetadata.cancerStudyColumnName,cs);

                        if (optMap.isPresent()){
                            return new FoundationMetadata(optMap.get());
                        }

                        return null;
                    }
                })

                .filter(new Predicate<FoundationMetadata>() {
                    @Override
                    public boolean apply(@Nullable FoundationMetadata meta) {
                        List<String> fl = FluentIterable.from(Lists.newArrayList(filename)).filter(meta.getRelatedFileFilter())
                                .toList();
                        return (!fl.isEmpty());
                    }
                })
                .transform(new Function<FoundationMetadata, String>() {

                    @Override
                    public String apply(FoundationMetadata f) {
                        return f.getCancerStudy();
                    }
                }).first();
    }

    /*
    private method to determine where a new Foundation XML file should be moved to
    the destination directory is based on the cancer study name registered in the config data
     */
    private Path resolveDestinationPath(Path sourcePath) {
       Optional<String> studyOpt = this.resolveFoundationCancerStudyNameFromXMLFileName(sourcePath.toString());
        if(studyOpt.isPresent()){
            try {
                // ensure that required directories exist
                Path subDirPath = Paths.get(StagingCommonNames.pathJoiner.join(this.foundationDataDirectory, studyOpt.get()));
                Files.createDirectories(subDirPath);   
                return Paths.get(StagingCommonNames.pathJoiner.join(this.foundationDataDirectory, studyOpt.get(),
                        sourcePath.getFileName().toString()));
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
        return sourcePath;
    }

    /**
     * Process each XML file in the input source. Determine the  destination
     * from the cancer study the file belongs to, move the file.
     * Add the affected cancer study to the Set
     *
     */
    public Set<Path> extractData() throws IOException {
               return FluentIterable
                .from(inputDataSource.getFilenameList())
                .transform(new Function<Path, Path>() {
                    @Override
                    public Path apply(Path inPath) {
                        Path outPath = resolveDestinationPath(inPath);
                        logger.info("Moving from " +inPath.toString() +" to " +outPath.toString());
                        try {
                            if (outPath != inPath) {
                                Files.move(inPath, outPath, StandardCopyOption.REPLACE_EXISTING);                          
                            }
                        } catch (IOException ex) {
                            logger.error(ex.getMessage());
                        }
                        // return the Path to the parent directory
                        return outPath.getParent();
                    }
                }).toSet();
        
    }

    // main method for stand alone testing
    public static void main (String...args){
        String testDataSource = "foundation-dev";
        FoundationStudyExtractor extractor = new FoundationStudyExtractor(testDataSource);
        String fileName1 = "lymphoma.xml";
        String fileName2 = "lymphoma-filtered.xml";
        logger.info(extractor.resolveFoundationCancerStudyNameFromXMLFileName(fileName1).get());
        logger.info(extractor.resolveFoundationCancerStudyNameFromXMLFileName(fileName2).get());

    }


}
