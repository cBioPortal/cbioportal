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
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.FileTransformer;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.dao.ImportDataRecordDAO;
import org.mskcc.cbio.importer.foundation.extractor.FileDataSource;
import org.mskcc.cbio.importer.foundation.extractor.FoundationStudyExtractor;
import org.mskcc.cbio.importer.foundation.transformer.*;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;
import org.mskcc.cbio.importer.persistence.staging.clinical.ClinicalDataFileHandler;
import org.mskcc.cbio.importer.persistence.staging.clinical.ClinicalDataFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.cnv.CnvFileHandler;
import org.mskcc.cbio.importer.persistence.staging.cnv.CnvFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.fusion.FusionFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;

/**
 * This represents a Fetcher implementation that will fetch and transform XML
 * files in the $PORTAL_DATA_HOME/foundation directory 
 *
 * Any existing staging file generated from a prior version of an XML file with
 * the same name will be overwritten
 *
 * A single cancer study can be supported by >1 XML files
 * 
 * For a Foundation study that is supported by >1 XML data file, the addition
 * or update of 1 XML file will cause a regeneration of all staging files for that
 * study
 *
 * In production use, this class will be instantiated by the Spring framework
 * and invoked by the cbio import process
 *
 * @author criscuof
 */
public class FoundationFetcherImpl implements Fetcher {

    private final static Logger logger = Logger.getLogger(FoundationFetcherImpl.class);

    private FoundationStudyExtractor extractor;
    private  FileTransformer fileTransformer;
    private Config config;
    private FileUtils fileUtils;
    private ImportDataRecordDAO importDataRecordDAO;
    private DatabaseUtils databaseUtils;
    private DataSourcesMetadata dataSourceMetadata;

   /*
    Default constructor - for stand alone testing

    */ 
    public FoundationFetcherImpl(){
        this.extractor = new FoundationStudyExtractor();
        TsvStagingFileHandler svtFileHandler = new MutationFileHandlerImpl();
        CnvFileHandler cnVFileHandler = new CnvFileHandlerImpl();
        ClinicalDataFileHandler clinicalDataFileHandler = new ClinicalDataFileHandlerImpl();
        TsvStagingFileHandler fusionFileHandler = new FusionFileHandlerImpl();

        this.fileTransformer= new FoundationXMLTransformer(
                new FoundationShortVariantTransformer(svtFileHandler),
                new FoundationCnvTransformer(cnVFileHandler),
                new FoundationClinicalDataTransformer(clinicalDataFileHandler),
                new FoundationFusionTransformer(fusionFileHandler));

    }

    //Spring-based constructor
    public FoundationFetcherImpl (FoundationStudyExtractor extract,
                                  FoundationXMLTransformer  trans             ){
        Preconditions.checkArgument(null != extract, "A FoundationStudyExtractor is required");
        Preconditions.checkArgument(null != trans,"A FoundationXMLTransformer is required");
        this.extractor = extract;
        this.fileTransformer = trans;
    }

    @Override
    // legacy fetch
    // attributes now resolved from importer spreadsheet
    public void fetch(final String dataSource, final String desiredRunDate, boolean sendNotification) throws Exception {
        this.fetch();

    }

    private void fetch() throws Exception {
        logger.info("fetch operation for Foundation Medicine files invoked");
        // transform the XML data to text format
        for (Path xmlPath : this.extractor.extractData()) {
            // use the Foundation XML Parser to generate the data files
            FileDataSource fds = new FileDataSource(xmlPath.toString(),xmlFileExtensionFilter);
            logger.info("Transforming data from " + xmlPath.toString());
            this.fileTransformer.transform(fds);
        }
    }

    @Override
    public void fetchReferenceData(ReferenceMetadata referenceMetadata) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
     Predicate xmlFileExtensionFilter = new Predicate<Path>() {
                @Override
                public boolean apply(Path input) {
                    return (input.toString().endsWith("xml"));
                }
            };

    // main method for standalone test
    public static void main (String...args){
        FoundationFetcherImpl fetcher = new FoundationFetcherImpl();
        try {
            fetcher.fetch();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
