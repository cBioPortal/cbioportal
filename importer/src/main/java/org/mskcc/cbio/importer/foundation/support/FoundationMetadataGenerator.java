package org.mskcc.cbio.importer.foundation.support;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.internal.Preconditions;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.CaseIDs;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;

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
 */
/**
 * This class is responsible for creating the metadata files associated with a
 * FoundationMedicine cancer study
 *
 * @author criscuof
 */
public class FoundationMetadataGenerator {
/*    
    private final Config config;
    private final String outputDirectory;
    private final Logger logger = Logger.getLogger(FoundationMetadataGenerator.class);
    
    private final FileUtils fileUtils;
    private final CaseIDs caseIDs;
    public static final String MUTATION_METADATA = "mutation-foundation";
    public static final String CNA_METADATA = "cna-foundation";
    public static final String FUSION_METADATA = "fusion";
    private DataSourcesMetadata dataSourceMetadata;
    private final Joiner pathJoiner = Joiner.on(System.getProperty("file.separator"));
    
    public FoundationMetadataGenerator(Config aConfig, String aDirectory) {
        Preconditions.checkArgument(null != aConfig, "A Config implemntation is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aDirectory), "An metadata file output directory is required");
        
        this.config = aConfig;
        this.outputDirectory = aDirectory;
        this.caseIDs = new CaseIDsImpl(this.config);
        this.fileUtils = new FileUtilsImpl(this.config,
                this.caseIDs);
        
    }
    
    public void generateFoundationMetadataFiles(final String dataSource,
            final String cancerStudyIdentifier, final Integer numCases, final String outputDir) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dataSource),
                "A data source is required");        
        Preconditions.checkArgument(!Strings.isNullOrEmpty(cancerStudyIdentifier),
                "A cancer study identifier is required");
        Preconditions.checkArgument(null != numCases && numCases > 0,
                "The number of cases must be > 0");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(outputDir),
                "An output directory is required");
        try {
            this.resolveDataSourceMetadata(dataSource);
            this.generateCancerStudyMetaData(cancerStudyIdentifier, numCases);        
            this.generateCNAMetadataFile(dataSource, numCases, outputDirectory);
            this.generateMutationMetadataFile(cancerStudyIdentifier, numCases, outputDir);
            this.generateFusionMetaFile(cancerStudyIdentifier, numCases, outputDir);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        
    }
    
    private void resolveDataSourceMetadata( String dataSource) {
        
        Collection<DataSourcesMetadata> dataSourcesMetadata = config.getDataSourcesMetadata(dataSource);
        Preconditions.checkNotNull(dataSourcesMetadata, "cannot instantiate a proper DataSourcesMetadata object.");
        Preconditions.checkState(!dataSourcesMetadata.isEmpty(),
                "data source metadata is empty ");
        this.dataSourceMetadata = dataSourcesMetadata.iterator().next();
        
    }
    
    private void generateCancerStudyMetaData( String cancerStudyIdentifier,
             Integer numCases) {
        
        List<String> cancerStudyList = config.findCancerStudiesBySubstring(cancerStudyIdentifier.toLowerCase());
        if (null == cancerStudyList || cancerStudyList.isEmpty()) {
            logger.info("There are no cancer study names associated with: " + cancerStudyIdentifier);
            return;
        }
        if (cancerStudyList.size() > 1) {
            logger.error("The cancer study name: " + cancerStudyIdentifier + " is not specific");
            for (String study : cancerStudyList) {
                logger.info(study);
            }
        }
        try {
            // generate the metadata files
            this.generateStudyMetadataFile(cancerStudyList.get(0), numCases);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        
    }
    
    private void generateCNAMetadataFile(final String cancerStudyIdentifier,
            final Integer numCases, final String outputDir) throws Exception {
        
        DatatypeMetadata datatypeMetadata = this.getDatatypeMetadata(CNA_METADATA);
        CancerStudyMetadata cancerMetadata = this.config.getCancerStudyMetadataByName(cancerStudyIdentifier);
        this.fileUtils.writeMetadataFile(
                pathJoiner.join(dataSourceMetadata.getDownloadDirectory(), outputDir),
                cancerMetadata,
                datatypeMetadata,
                numCases);
    }
    
   private  void generateMutationMetadataFile( String cancerStudyIdentifier, Integer numCases, String outputDir) throws Exception {
        DatatypeMetadata datatypeMetadata = this.getDatatypeMetadata(MUTATION_METADATA);
        CancerStudyMetadata cancerMetadata = this.config.getCancerStudyMetadataByName(cancerStudyIdentifier);
        
        this.fileUtils.writeMetadataFile(
                dataSourceMetadata.getDownloadDirectory() + File.separator + outputDir,
                cancerMetadata,
                datatypeMetadata,
                numCases);
    }
    
    private void generateFusionMetaFile( String cancerStudyIdentifier,
            Integer numCases, String outputDir) throws Exception {
        DatatypeMetadata datatypeMetadata = this.getDatatypeMetadata(FUSION_METADATA);
        CancerStudyMetadata cancerMetadata = this.config.getCancerStudyMetadataByName(cancerStudyIdentifier);
        
        this.fileUtils.writeMetadataFile(
                dataSourceMetadata.getDownloadDirectory() + File.separator + outputDir,
                cancerMetadata,
                datatypeMetadata,
                numCases);
    }
    
    private void
            generateStudyMetadataFile(String cancerStudy, Integer numCases) throws Exception {
        CancerStudyMetadata cancerMetadata = this.config.getCancerStudyMetadataByName(cancerStudy);
        if (null != cancerMetadata) {
            this.fileUtils.writeCancerStudyMetadataFile(
                    this.outputDirectory,
                    cancerMetadata,
                    numCases);
        }
    }
    
    private DatatypeMetadata getDatatypeMetadata(String datatype) {
        Collection<DatatypeMetadata> list = this.config.getDatatypeMetadata(datatype);
        return list.iterator().next();
    }
*/    
}
