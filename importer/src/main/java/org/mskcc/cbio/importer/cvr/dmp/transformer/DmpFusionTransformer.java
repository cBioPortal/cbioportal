package org.mskcc.cbio.importer.cvr.dmp.transformer;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.dmp.model.DmpData;
import org.mskcc.cbio.importer.cvr.dmp.model.Result;
import org.mskcc.cbio.importer.cvr.dmp.model.StructuralVariant;
import org.mskcc.cbio.importer.cvr.dmp.util.DmpUtils;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.persistence.staging.fusion.FusionModel;
import org.mskcc.cbio.importer.persistence.staging.fusion.FusionTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Created by criscuof on 11/24/14.
 */
public class DmpFusionTransformer extends FusionTransformer
        implements DMPDataTransformable{

     /*
    responsible for generating the fusion staging file for DMP structural variants
     */

    private final static Logger logger = Logger.getLogger(DmpFusionTransformer.class);
    private static final Boolean DELETE_STAGING_FILE = false;  // DMP files are appended


    public DmpFusionTransformer(Path aPath,CancerStudyMetadata csMeta) {
        super(aPath.resolve(dtMeta.getStagingFilename()), DELETE_STAGING_FILE, csMeta);
    }

    @Override
    public void transform(DmpData data) {
        Preconditions.checkArgument(null != data, "A DmpData object is required");
        List<DmpFusionModel> fusionModelList = Lists.newArrayList();
        // process any deprecated samples
        this.tsvFileHandler.removeDeprecatedSamplesFomTsvStagingFiles(DmpFusionModel.getSampleIdColumnName(),
                DmpUtils.resolveDeprecatedSamples(data));
        for(Result result : data.getResults()){
            String sampleId = result.getMetaData().getDmpSampleId();
            for(StructuralVariant sv : result.getSvVariants()){
                // add the metadata sample id to the structural variant
                sv.setDmpSampleId(sampleId);
                fusionModelList.add(new DmpFusionModel((sv)));
                if(!StringUtils.equals(sv.getSite1Gene(),sv.getSite2Gene())){
                    logger.info("Generating secondary variant for: " +sv.getEventInfo());
                    fusionModelList.add(
                            // use utility method to generate a record for the second gene in a heterogenous fusion
                            new DmpFusionModel(DmpUtils.generateSecondaryStructuralVariant(sv)));
                }
            }
        }
        // pass this list to the file handler for output
        if (!fusionModelList.isEmpty()) {
            this.tsvFileHandler.transformImportDataToTsvStagingFile(fusionModelList, FusionModel.getTransformationModel());
        }else {
            logger.info("The DMP data did not contain any fusion variations");
        }
    }

    // main method for stand alone testing
    public static void main(String...args){
        ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        Path stagingFileDirectory = Paths.get("/tmp/cvr/dmp");
        Optional<CancerStudyMetadata> csMetaOpt = CancerStudyMetadata.findCancerStudyMetaDataByStableId(DMPDataTransformer.STABLE_ID);
        if (csMetaOpt.isPresent()) {
            DmpFusionTransformer transformer = new DmpFusionTransformer(stagingFileDirectory, csMetaOpt.get());
            try {
                DmpData data = OBJECT_MAPPER.readValue(new File("/tmp/dmp_ws.json"), DmpData.class);
                transformer.transform(data);

            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        } else {
            logger.error("CancerStudyMetadata for " +DMPDataTransformer.STABLE_ID +" could not be resolved");
        }
    }

}
