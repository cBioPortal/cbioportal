package org.mskcc.cbio.importer.cvr.dmp.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.dmp.model.DmpData;
import org.mskcc.cbio.importer.cvr.dmp.model.Result;
import org.mskcc.cbio.importer.cvr.dmp.model.StructuralVariant;
import org.mskcc.cbio.importer.cvr.dmp.util.DMPCommonNames;
import org.mskcc.cbio.importer.cvr.dmp.util.DmpUtils;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.persistence.staging.structvariant.StructVariantModel;
import org.mskcc.cbio.importer.persistence.staging.structvariant.StructVariantTransformer;

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
 * Created by criscuof on 1/30/15.
 */
public class DmpStructuralVariantTransformer extends StructVariantTransformer implements DMPDataTransformable {
    //DMP data is appended to existing data
    private static final Boolean DELETE_STAGING_FILE = false;
    private final static Logger logger = Logger.getLogger(DmpStructuralVariantTransformer.class);


    public DmpStructuralVariantTransformer(Path aPath, CancerStudyMetadata csMeta) {

        super(aPath.resolve(stagingFileName), DELETE_STAGING_FILE, csMeta);
    }

    @Override
    public void transform(DmpData data) {
        Preconditions.checkArgument(null != data, "A DmpData object is required");
        List<DmpStructVariantModel> svModelList = Lists.newArrayList();
        // remove any deprecated samples
        this.tsvFileHandler.removeDeprecatedSamplesFomTsvStagingFiles(DmpStructVariantModel.getSampleIdColumnName(),
                DmpUtils.resolveDeprecatedSamples(data));
        for(Result result : data.getResults()) {
            String sampleId = result.getMetaData().getDmpSampleId();
            for (StructuralVariant sv : result.getSvVariants()) {
                // add the metadata sample id to the structural variant
                sv.setDmpSampleId(sampleId);
                svModelList.add(new DmpStructVariantModel(sv));
                // generate new structural variant if heterogeneous event
                if (!StringUtils.equals(sv.getSite1Gene(), sv.getSite2Gene())) {
                    logger.info("Generating secondary variant for: " + sv.getEventInfo());
                    svModelList.add(new DmpStructVariantModel(DmpUtils.generateSecondaryStructuralVariant(sv)));
                }

            }
        }
        // output the contents of the model objects to a staging file
        if(!svModelList.isEmpty()){
            this.tsvFileHandler.transformImportDataToTsvStagingFile(svModelList, StructVariantModel.getTransformationFunction());
        } else {
            logger.info("The DMP data did not contain any structural variations");
        }

    }

    // main method for stand alone testing
    public static void main(String...args) {
        ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        Path stagingFileDirectory = Paths.get("/tmp/cvr/dmp");
        Optional<CancerStudyMetadata> csMetaOpt = CancerStudyMetadata.findCancerStudyMetaDataByStableId(DMPDataTransformer.STABLE_ID);
        if (csMetaOpt.isPresent()) {
            DmpStructuralVariantTransformer transformer = new DmpStructuralVariantTransformer(stagingFileDirectory, csMetaOpt.get());
            try {
                DmpData data = OBJECT_MAPPER.readValue(new File("/tmp/cvr/dmp/result-run160.json"), DmpData.class);
                transformer.transform(data);

            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        } else {
            logger.error("CancerStudyMetadata for " +DMPDataTransformer.STABLE_ID +" could not be resolved");
        }
    }

}
