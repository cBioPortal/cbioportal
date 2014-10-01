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
package org.mskcc.cbio.importer.fetcher.internal;

import com.google.common.base.Predicate;
import com.google.inject.internal.Preconditions;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Level;
import joptsimple.internal.Strings;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.FileTransformer;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.dao.ImportDataRecordDAO;
import org.mskcc.cbio.importer.extractor.FileDataSource;
import org.mskcc.cbio.importer.extractor.FileExtractor;
import org.mskcc.cbio.importer.foundation.support.FoundationMetadataGenerator;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;

/**
 * This represents a Fetcher implementation that will fetch and transform XML
 * files in the $PORTAL_DATA_HOME/foundation directory These XML files will
 * automatically copied to this base directory from an MSKCC SFTP repository
 * available for Foundation Medicine file uploads
 *
 * Any existing staging file generated from a prior version of an XML file with
 * the same name will be overwritten
 *
 * A single cancer study can be supported by >1 XML files
 *
 * In production use, this class will be instantiated by the Spring framework
 * and invoked by the cbio import process
 *
 * @author criscuof
 */
public class FoundationFetcherImpl implements Fetcher {

    private final Logger logger = Logger.getLogger(FoundationFetcherImpl.class);
    private FoundationMetadataGenerator metaDataGenerator;

    private static final String xmlFileExtension = "xml";
    private static final String testFileExtension = "txt";
    private FileExtractor extractor;
    private String sourceDirectoryName;
    private String outputBaseDir;
    private FileTransformer fileTransformer;
    private String dataSource;

    private Config config;
    private FileUtils fileUtils;
    private ImportDataRecordDAO importDataRecordDAO;
    private DatabaseUtils databaseUtils;
    private DataSourcesMetadata dataSourceMetadata;

    public FoundationFetcherImpl(Config config, FileUtils fileUtils,
            DatabaseUtils databaseUtils, ImportDataRecordDAO importDataRecordDAO) {

        // set members
        this.config = config;
        this.fileUtils = fileUtils;
        this.databaseUtils = databaseUtils;
        this.importDataRecordDAO = importDataRecordDAO;

    }

/*
     // constructor consistent with portal_importer_configuration constructor_args
    public FoundationFetcherImpl(FileDataSource inSource, FileDataSource outSource,
                                 FoundationMetadataGenerator meta,
                                 FileTransformer aTrans) {

        // check for arguments to support non-Spring invocations
        Preconditions.checkArgument(null != inSource, "Error an input FileDataSource instance is required");
        Preconditions.checkArgument(null != outSource, "Error an output FileDataSource instance is required");
        Preconditions.checkArgument(null != aTrans, "ERROR: a FileTransformer implementation is required");

        this.metaDataGenerator = meta;
        this.extractor = new FileExtractor(inSource, outSource, xmlFileExtensionFilter);
        this.fileTransformer = aTrans;
        this.outputBaseDir = outSource.getDirectoryName();
    }
*/

    /*
     private method to complete the input and output file data sources
     this provides a mapping for copying the FMI XML files to the correct
     cbio-data directories
     */
    private void resolveFileDataSources() {
        Collection<DataSourcesMetadata> dataSourcesMetadata = config.getDataSourcesMetadata(this.dataSource);
        Preconditions.checkState(!dataSourcesMetadata.isEmpty(),
                "The Foundation DataSourcesMetadata object is invalid");
        Preconditions.checkState(dataSourcesMetadata.size() ==1, 
                "The DataSourcesMetadata for FMI is ambiguous");
        this.dataSourceMetadata = dataSourcesMetadata.iterator().next();
        try {
            FileDataSource inSource = new FileDataSource(this.dataSourceMetadata.getDownloadDirectory(),
                    xmlFileExtensionFilter);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
    
    /*
     * we only want to fetch xml files
     */
    Predicate xmlFileExtensionFilter = new Predicate<Path>() {
        @Override
        public boolean apply(Path input) {
            return (input.toString().endsWith(xmlFileExtension));
        }
    };

    @Override
    public void fetch(final String dataSource, final String desiredRunDate) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dataSource), "A data source is required");
        this.dataSource = dataSource;
        logger.info("fetch operation for Foundation Medicine files invoked");
        this.resolveFileDataSources();
        // fetch new Foundation XML files
        this.extractor.extractData();
        // transform the XML data to text format
        for (Path xmlPath : this.extractor.getExtractedDataSource().getFilenameList()) {
            // use the Foundation XML Parser to generate the data files
            logger.info("Transforming data from " + xmlPath.toString());
            this.fileTransformer.transform(xmlPath);
            /*
            this.metaDataGenerator.generateFoundationMetadataFiles(dataSource,
                    fileTransformer.getPrimaryIdentifier(),
                    fileTransformer.getPrimaryEntityCount(),
                    outputBaseDir);
                    */
        }
    }

    @Override
    public void fetchReferenceData(ReferenceMetadata referenceMetadata) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
