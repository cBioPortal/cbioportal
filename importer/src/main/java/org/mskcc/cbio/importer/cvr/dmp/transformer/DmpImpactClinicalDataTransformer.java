package org.mskcc.cbio.importer.cvr.dmp.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.dmp.model.DmpData;
import org.mskcc.cbio.importer.cvr.dmp.model.Result;
import org.mskcc.cbio.importer.cvr.dmp.util.DmpUtils;
import org.mskcc.cbio.importer.persistence.staging.clinical.ImpactClinicalDataModel;
import org.mskcc.cbio.importer.persistence.staging.filehandler.FileHandlerService;
import org.mskcc.cbio.importer.persistence.staging.filehandler.TsvFileHandler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
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
 * Created by criscuof on 1/25/15.
 */
public class DmpImpactClinicalDataTransformer  implements DMPDataTransformable {
    /*
    responsible for transforming data from DMP MetaData objects to DmpImpactClinicalDataModel objects
    and outputting the transformed data to a clinical data staging file
     */
    private final TsvFileHandler fileHandler;
    private final static Logger logger = Logger.getLogger(DmpImpactClinicalDataTransformer.class);
    private static final String clinicalDataFilename = "data_clinical.txt";


    public DmpImpactClinicalDataTransformer (Path stagingFileDirectory) {

        Preconditions.checkArgument(null!= stagingFileDirectory,"A Path to the staging file directory is " +
                "required");
        this.fileHandler = FileHandlerService.INSTANCE.
                obtainFileHandlerForAppendingToStagingFile(stagingFileDirectory.resolve(clinicalDataFilename),
                        ImpactClinicalDataModel.resolveColumnNames());
    }

    @Override
    public void transform(DmpData data) {
        Preconditions.checkArgument(null != data, "A DmpData object is required");
        // filter out any deprecated samples from the existing file
        this.fileHandler.removeDeprecatedSamplesFomTsvStagingFiles(ImpactClinicalDataModel.SAMPLE_ID_COLUMN_NAME,
                DmpUtils.resolveDeprecatedSamples(data));
        this.fileHandler.transformImportDataToTsvStagingFile(this.resolveClinicalData(data),
                ImpactClinicalDataModel.getTransformationFunction() );
    }

    /*
    transform the metadata objects in the DMP data to a List of DmpImpactClinicalDataModel objects
     */
    private List<DmpImpactClinicalDataModel> resolveClinicalData(DmpData data){
        List<DmpImpactClinicalDataModel> modelList = Lists.newArrayList();
        for (Result result :data.getResults()){
            modelList.add(new DmpImpactClinicalDataModel(result.getMetaData()));
        }
        return modelList;
    }

    // main method for stand alone testing
    public static void main(String...args) {
        ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        String tempDir = "/tmp/cvr/dmp";
        File tmpDir = new File(tempDir);
        tmpDir.mkdirs();
        Path stagingFileDirectory = Paths.get(tempDir);
        DmpImpactClinicalDataTransformer transformer = new DmpImpactClinicalDataTransformer((stagingFileDirectory));
        try {
            DmpData data = OBJECT_MAPPER.readValue(new File("/tmp/cvr/dmp/result.json"), DmpData.class);
            transformer.transform(data);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
}
