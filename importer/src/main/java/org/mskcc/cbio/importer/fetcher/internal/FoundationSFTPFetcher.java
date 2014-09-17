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
import java.nio.file.Path;
import joptsimple.internal.Strings;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.FileTransformer;
import org.mskcc.cbio.importer.extractor.FileDataSource;
import org.mskcc.cbio.importer.extractor.FileExtractor;
import org.mskcc.cbio.importer.foundation.support.FoundationMetaDataGenerator;
import org.mskcc.cbio.importer.model.ReferenceMetadata;

/**
 * This represents a Fetcher implementation that will copy a Foundation cancer
 * study xml file from an SFTP repository to a local Mercurial repository,
 * generate new Foundation staging files from the XML input, and commit & push
 * these new files to the global Mercurial repository This class is intended to
 * be invoked by the Spring importer configuration script
 *
 * Cancer study metadata is obtained from the portal_importer_configuration
 * Google doc modified to use new Extractor & Transformer design
 *
 * @author criscuof
 */
public class FoundationSFTPFetcher implements Fetcher {

    private final Logger logger = Logger.getLogger(FoundationSFTPFetcher.class);
    private final FoundationMetaDataGenerator metaDataGenerator;

    private static final String xmlFileExtension = "xml";
    private static final String testFileExtension = "txt";
    private final FileExtractor extractor;
    private String sourceDirectoryName;
    private String outputBaseDir; 
    private FileTransformer fileTransformer;
    private String dataSourceName;

    // constructor consistent with portal_importer_configuration constructor_args
    public FoundationSFTPFetcher(FileDataSource inSource, FileDataSource outSource,
            FoundationMetaDataGenerator meta, 
            FileTransformer aTrans) {
        // check for arguments to support non-Spring invocations
        Preconditions.checkArgument(null != inSource, "Error an input FileDatSource instance is required");
        Preconditions.checkArgument(null != outSource, "Error an output FileDatSource instance is required");
        
        Preconditions.checkArgument(null != aTrans, "ERROR: a FileTransformer implementation is required");
        
        this.metaDataGenerator = meta;
        this.extractor = new FileExtractor(inSource, outSource, xmlFileExtensionFilter);
        this.fileTransformer = aTrans;

    }
    /*
    * we only want to extract xml files
    */
    Predicate xmlFileExtensionFilter = new Predicate<Path>() {
        @Override
        public boolean apply(Path input) {
            return (input.toString().endsWith(xmlFileExtension));
        }
    };
    
    @Override
    public void fetch(String dataSource, String desiredRunDate) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dataSource), "A data source is required");
        this.dataSourceName = dataSource;
        logger.info("fetch operation for Foundation Medicine files invoked");
        // extract new Foundation XML files
        this.extractor.extractData();  
        // transform the XML data to text format
        for (Path xmlPath : this.extractor.getExtractedDataSource().getFilenameList()) {
            // use the Foundation XML Parser to generate the data files
            logger.info("Transforming data from " +xmlPath.toString());
             this.fileTransformer.transform(xmlPath);
             this.metaDataGenerator.generateCancerStudyMetaData(fileTransformer.getPrimaryIdentifier(), 
                     fileTransformer.getPrimaryEntityCount());
        
        }
    }

    @Override
    public void fetchReferenceData(ReferenceMetadata referenceMetadata) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    

}