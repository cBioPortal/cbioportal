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
import java.io.IOException;
import java.nio.file.Paths;
import org.mskcc.cbio.importer.dmp.importer.DMPclinicaldataimporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.importer.Config;

import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.dmp.transformer.DMPDataTransformer;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;

import org.mskcc.cbio.importer.dmp.importer.MockConfig;
import org.mskcc.cbio.importer.dmp.model.DmpData;

public class DMPFetcherImpl implements Fetcher
{
    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    //private DMPStagingFileManager fileManager;

    private static final Log LOG = LogFactory.getLog(DMPFetcherImpl.class);
    private Config config;
    
    private static String dataSourcePath;

    public DMPFetcherImpl(Config config) {
        this.config = config;
    }
    
    @Override
    public boolean fetch(String dataSource, String desiredRunDate) 
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
        DMPDataTransformer transformer = new DMPDataTransformer(Paths.get(dataSourcePath));
        
        //Retrieve sample data 
        DMPclinicaldataimporter dmpImporter_retrieve = new DMPclinicaldataimporter();

        DmpData data = OBJECT_MAPPER.readValue(dmpImporter_retrieve.getResult(), DmpData.class);
        
        DMPclinicaldataimporter dmpImporter_mark = 
                new DMPclinicaldataimporter(transformer.transform(data)); //mark consumed samples (transformer returns a list of consumed sample ids)

        return true;
    }

    @Override
    public boolean fetchReferenceData(ReferenceMetadata referenceMetadata) 
            throws Exception {
        throw new UnsupportedOperationException();
    }
    
}
