/**
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Paths;

import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.importer.Config;

import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.cvr.darwin.service.DarwinImporterService;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;

import org.mskcc.cbio.importer.cvr.dmp.model.DmpData;
import org.mskcc.cbio.importer.cvr.dmp.importer.DMPclinicaldataimporter;
import org.mskcc.cbio.importer.cvr.dmp.transformer.DMPDataTransformer;

import org.mskcc.cbio.importer.cvr.darwin.transformer.DarwinTumorTransformer;
import org.mskcc.cbio.importer.cvr.darwin.util.DarwinSessionManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class DmpDarwinFetcherImpl implements Fetcher
{
    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    //private DMPStagingFileManager fileManager;

    private static final Log LOG = LogFactory.getLog(DmpDarwinFetcherImpl.class);
    private Config config;
    
    private static String dataSourcePath;

    public DmpDarwinFetcherImpl(Config config) {
        this.config = config;
    }
    
    @Override
    public void fetch(String dataSource, String desiredRunDate) 
            throws Exception {
        
        if (LOG.isInfoEnabled()) {
                LOG.info("fetch(), dateSource:runDate: " + dataSource + ":" + desiredRunDate);
        }
        
        //Get the datasource from config obj
        Collection<DataSourcesMetadata> dataSourcesMetaData = config.getDataSourcesMetadata(dataSource);
        for (DataSourcesMetadata singleDataSourceMetaData : dataSourcesMetaData) {
            if (singleDataSourceMetaData.getDataSource().equals(dataSource)) {
                dataSourcePath = singleDataSourceMetaData.getDownloadDirectory();
            }
        }
        //Retrieve DMP data
        DMPDataTransformer transformer = new DMPDataTransformer(Paths.get(dataSourcePath));
        DMPclinicaldataimporter dmpImporterRetriever = new DMPclinicaldataimporter();
        DmpData data = OBJECT_MAPPER.readValue(dmpImporterRetriever.getResult(), DmpData.class);

        DMPclinicaldataimporter dmpImporter_mark = 
                new DMPclinicaldataimporter(transformer.transform(data)); //mark consumed samples (transformer returns a list of consumed sample ids)
        
        //Retrieve Darwin clinical data for retrieved DMP samples
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext-importer.xml");
//        DarwinImporterService darwinImporterService = (DarwinImporterService) applicationContext.getBean("darwinImporterService");
//        darwinImporterService.transformDarwinData(Paths.get(dataSourcePath));
        DarwinTumorTransformer darwinTransformer = new DarwinTumorTransformer(Paths.get(dataSourcePath));
        darwinTransformer.transform();
        DarwinSessionManager.INSTANCE.closeSession();
    }

    @Override
    public void fetchReferenceData(ReferenceMetadata referenceMetadata) 
            throws Exception {
        throw new UnsupportedOperationException();
    }
    
}
