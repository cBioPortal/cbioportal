package org.mskcc.cbio.importer.cvr.dmp.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.dmp.model.DmpData;
import org.mskcc.cbio.importer.cvr.dmp.model.DmpSnp;
import org.mskcc.cbio.importer.cvr.dmp.model.MetaData;
import org.mskcc.cbio.importer.cvr.dmp.model.Result;
import org.mskcc.cbio.importer.cvr.dmp.util.DmpUtils;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationModel;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationTransformer;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;

import javax.annotation.Nullable;
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
 * Created by criscuof on 11/26/14.
 */
public class DmpSnpTransformer extends MutationTransformer implements DMPDataTransformable {

    private final static Logger logger = Logger.getLogger(DmpSnpTransformer.class);

    public DmpSnpTransformer(TsvStagingFileHandler aHandler,Path stagingFileDirectory) {
        super(aHandler);
        if(StagingUtils.isValidStagingDirectoryPath(stagingFileDirectory)) {
            aHandler.registerTsvStagingFile(stagingFileDirectory.resolve("data_mutations_extended.txt"),
                    MutationModel.resolveColumnNames());
        }
    }

    @Override
    public void transform(DmpData data) {
        Preconditions.checkArgument(null != data, "A DmpData object is required");
        // process any deprecated samples
        DmpUtils.removeDeprecatedSamples(data, this.fileHandler);
        // convert DmpSnp objects to DmpSnpModel objects and output to staging file
        this.fileHandler.transformImportDataToTsvStagingFile(this.resolveDmpMutations(data),
                MutationModel.getTransformationModel());
    }

    /*
    private method to convert DmpSnp objects to DmpModel objects
     */
    private List<DmpSnpModel> resolveDmpMutations(DmpData data){
        List<DmpSnpModel> modelList = Lists.newArrayList();
        for(Result result : data.getResults()){
            // add sample id to Snp object
            List<DmpSnp> snpList = Lists.newArrayList();
            final MetaData meta = result.getMetaData();
            // combine the two types of SNPs in DMP data
            snpList.addAll(result.getSnpExonic());
            snpList.addAll(result.getSnpSilent());
            modelList.addAll(FluentIterable.from(snpList)
                    .transform(new Function<DmpSnp, DmpSnp>() {
                        @Override
                        //add the sample id to each snp from the result metadata
                        public DmpSnp apply(DmpSnp snp) {
                            // add the sample id to the SNP
                            snp.setDmpSampleId(meta.getDmpSampleId());
                            return snp;
                        }
                    })
                            // generate the model objects
                    .transform(new Function<DmpSnp, DmpSnpModel>() {
                        @Nullable
                        @Override
                        public DmpSnpModel apply(DmpSnp snp) {
                            return new DmpSnpModel(snp);
                        }
                    }).toList());
        }
        return modelList;
    }
    // main method for stand alone testing
    public static void main(String...args){
        ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        String tempDir = "/tmp/cvr/dmp";
        File tmpDir = new File(tempDir);
        tmpDir.mkdirs();
        Path stagingFileDirectory = Paths.get(tempDir);
        TsvStagingFileHandler fileHandler = new MutationFileHandlerImpl();

       // fileHandler.registerTsvStagingFile(stagingFileDirectory.resolve("data_mutations_mutations.txt"),
        //        MutationModel.resolveColumnNames(),true);
        DmpSnpTransformer transformer = new DmpSnpTransformer(fileHandler,stagingFileDirectory);

        try {
            DmpData data = OBJECT_MAPPER.readValue(new File("/tmp/cvr/dmp/result-sv.json"), DmpData.class);
            transformer.transform(data);

        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
}
