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
import com.google.common.collect.Lists;
import org.mskcc.cbio.importer.dmp.importer.DMPclinicaldataimporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.importer.Config;

import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.dmp.model.DmpData;
import org.mskcc.cbio.importer.dmp.support.DMPStagingFileManager;
import org.mskcc.cbio.importer.dmp.transformer.CnvVariantDataTransformer;
import org.mskcc.cbio.importer.dmp.transformer.DMPDataTransformer;
import org.mskcc.cbio.importer.dmp.transformer.DMPTransformable;
import org.mskcc.cbio.importer.dmp.transformer.SnpExonicDataTransformer;
import org.mskcc.cbio.importer.dmp.transformer.SnpSilentDataTransformer;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;

import org.mskcc.cbio.importer.dmp.importer.MockConfig;

public class DMPFetcherImpl implements Fetcher{

    private ObjectMapper OBJECT_MAPPER;
    private DMPStagingFileManager fileManager;

    private static final Log LOG = LogFactory.getLog(DMPFetcherImpl.class);
    private Config config;
    private FileUtils fileUtils;
    private String dataSourcePath;

    private DataSourcesMetadata dataSourceMetadata;

    public DMPFetcherImpl(Config config, FileUtils fileUtils) {
        this.config = config;
        this.fileUtils = fileUtils;
    }
    
    @Override
    public void fetch(String dataSource, String desiredRunDate) 
            throws Exception {

        if (LOG.isInfoEnabled()) {
                LOG.info("fetch(), dateSource:runDate: " + dataSource + ":" + desiredRunDate);
        }
        
        //Get the datasource from config obj
        this.config = new MockConfig(); //TODO: remove the mock config obj and replace with the real one
        Collection<DataSourcesMetadata> dataSourcesMetaData = config.getDataSourcesMetadata(dataSource);
        for (DataSourcesMetadata singleDataSourceMetaData : dataSourcesMetaData) {
            if (singleDataSourceMetaData.getDataSource().equals(dataSource)) {
                dataSourcePath = singleDataSourceMetaData.getDownloadDirectory();
            }
        }
        
        //Register transformers
       
        DMPDataTransformer transformer = new DMPDataTransformer(fileManager);
        
        //Retrieve process
        DMPclinicaldataimporter dmpImporter_retrieve = new DMPclinicaldataimporter();
        DmpData data = OBJECT_MAPPER.readValue(dmpImporter_retrieve.getResult(), DmpData.class);
        transformer.transform(data);
        
        //Marking/call back process
        ArrayList<String> _consumedSampleIds = new ArrayList<>();
        DMPclinicaldataimporter dmpImporter_mark = new DMPclinicaldataimporter(_consumedSampleIds);

    }

    @Override
    public void fetchReferenceData(ReferenceMetadata referenceMetadata) 
            throws Exception {
    }
    
}
