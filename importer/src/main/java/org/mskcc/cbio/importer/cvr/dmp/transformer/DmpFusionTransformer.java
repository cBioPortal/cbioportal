package org.mskcc.cbio.importer.cvr.dmp.transformer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.dmp.model.DmpData;
import org.mskcc.cbio.importer.cvr.dmp.model.Result;
import org.mskcc.cbio.importer.cvr.dmp.model.StructuralVariant;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;
import org.mskcc.cbio.importer.persistence.staging.fusion.FusionModel;
import org.mskcc.cbio.importer.persistence.staging.fusion.FusionTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;

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
    /*
    responsible for generating the fusion staging file for DMP structural variants
     */

 implements DMPDataTransformable{

    private final static Logger logger = Logger.getLogger(DmpFusionTransformer.class);

    public DmpFusionTransformer(TsvStagingFileHandler aHandler) {
        super(aHandler);
    }

    @Override
    public void transform(DmpData data) {
        Preconditions.checkArgument(null != data, "A DmpData object is required");
        List<DmpFusionModel> fusionModelList = Lists.newArrayList();
        for(Result result : data.getResults()){
            String sampleId = result.getMetaData().getDmpSampleId();
            for(StructuralVariant sv : result.getStructuralVariants()){
                // add the metadata sample id to the structural variant
                sv.setDmpSampleId(sampleId);
                fusionModelList.add(new DmpFusionModel((sv)));
                if(!StringUtils.equals(sv.getSite1Gene(),sv.getSite2Gene())){
                    logger.info("Generating secondary variant for: " +sv.getEventInfo());
                    fusionModelList.add(
                            new DmpFusionModel(this.generateSecondaryStructuralVariant(sv)));
                }
            }
        }
        // pass this list to the file handler for output
        if (!fusionModelList.isEmpty()) {
            this.fileHandler.transformImportDataToTsvStagingFile(fusionModelList, FusionModel.getTransformationModel());
        }
    }

    /*
    private method to generate a structural variant for the second gene
    in a fusion pair. The original site1 & site2 attributes are reversed
     */
    private StructuralVariant generateSecondaryStructuralVariant(StructuralVariant primary){
        StructuralVariant secondary = new StructuralVariant();
        secondary.setComments(primary.getComments());
        secondary.setConfidenceClass(primary.getConfidenceClass());
        secondary.setConnType(primary.getConnType());
        secondary.setDmpSampleId(primary.getDmpSampleId());
        secondary.setEventInfo(primary.getEventInfo());
        // reverse site1 and site2 attribute values
        secondary.setSite1Chrom(primary.getSite2Chrom());
        secondary.setSite1Desc(primary.getSite2Desc());
        secondary.setSite1Gene(primary.getSite2Gene());
        secondary.setSite1Pos(primary.getSite2Pos());
        secondary.setSite2Chrom(primary.getSite1Chrom());
        secondary.setSite2Desc(primary.getSite1Desc());
        secondary.setSite2Gene(primary.getSite1Gene());
        secondary.setSite2Pos(primary.getSite1Pos());
        secondary.setSvClassName(primary.getSvClassName());
        secondary.setSvDesc(primary.getSvDesc());
        secondary.setSvLength(primary.getSvLength());
        secondary.setSvVariantId(primary.getSvVariantId());
        secondary.setVariantStatusName(primary.getVariantStatusName());
        return secondary;
    }

    // main method for stand alone testing
    public static void main(String...args){
        ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        Path stagingFileDirectory = Paths.get("/tmp/cvr/dmp");
        TsvStagingFileHandler fileHandler = new MutationFileHandlerImpl();

        fileHandler.registerTsvStagingFile(stagingFileDirectory.resolve("data_fusions.txt"), FusionModel.resolveColumnNames(),true);
        DmpFusionTransformer transformer = new DmpFusionTransformer(fileHandler);

        try {
            DmpData data = OBJECT_MAPPER.readValue(new File("/tmp/cvr/dmp/result-sv.json"), DmpData.class);
            transformer.transform(data);

        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

}
