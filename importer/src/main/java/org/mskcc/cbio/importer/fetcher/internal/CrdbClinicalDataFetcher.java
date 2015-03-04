package org.mskcc.cbio.importer.fetcher.internal;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.cvr.crdb.transformer.CrdbDatasetTransformer;
import org.mskcc.cbio.importer.cvr.crdb.transformer.CrdbSurveyTransformer;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;

import java.nio.file.Path;

/**
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by Fred Criscuolo on 3/4/15.
 * criscuof@mskcc.org
 */
public class CrdbClinicalDataFetcher implements Fetcher {
    private static final Logger logger = Logger.getLogger(CrdbClinicalDataFetcher.class);

    @Override
    public void fetch(String dataSource, String desiredRunDate) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dataSource),
                "A registered data source name is required");
        Optional<DataSourcesMetadata> metaOpt = DataSourcesMetadata.findDataSourcesMetadataByDataSourceName(dataSource);
        if(metaOpt.isPresent()){
            Path filePath = metaOpt.get().resolveBaseStagingDirectory();
            CrdbSurveyTransformer crdbSurvey = new CrdbSurveyTransformer(filePath);
            crdbSurvey.transform();
            CrdbDatasetTransformer crdbDataset = new CrdbDatasetTransformer(filePath);
            crdbDataset.transform();
        } else{
            logger.error(dataSource +" is not a registered data source");
        }
    }

    @Override
    public void fetchReferenceData(ReferenceMetadata referenceMetadata) throws Exception {

    }

    public static void main (String...args){
        CrdbClinicalDataFetcher fetcher = new CrdbClinicalDataFetcher();
        try {
            fetcher.fetch("crdb-clinical-data","latest");
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }


}
