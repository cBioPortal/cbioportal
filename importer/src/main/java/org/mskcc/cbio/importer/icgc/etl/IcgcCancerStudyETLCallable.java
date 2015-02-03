package org.mskcc.cbio.importer.icgc.etl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.util.StringUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.model.IcgcClinicalModel;
import org.mskcc.cbio.importer.icgc.model.IcgcFusionModel;
import org.mskcc.cbio.importer.icgc.support.IcgcFunctionLibrary;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;
import org.mskcc.cbio.importer.persistence.staging.filehandler.FileHandlerService;
import org.mskcc.cbio.importer.persistence.staging.filehandler.TsvFileHandler;
import org.mskcc.cbio.importer.persistence.staging.filehandler.TsvFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.fusion.FusionModel;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationModel;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationModelFunnel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

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
 * Created by criscuof on 12/24/14.
 */
public class IcgcCancerStudyETLCallable implements Callable<String> {
    /*
    repsonsible for obtaining data from the ICGC Data Repository using a
    supplied URL and transforming them into a staging file
     */
    private static final Logger logger = Logger.getLogger(IcgcCancerStudyETLCallable.class);
    private final String icgcStudyUrl;
    private final Class modelClass;
    private final String modelType;
    private final TsvFileHandler fileHandler;


        public IcgcCancerStudyETLCallable(String aUrl, Class aClass,
                String aType, TsvFileHandler aHandler){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aUrl), "An ICGC Cancer study is required");
        Preconditions.checkArgument(null != aClass, "A model Class is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aType),"A model type specification is required");
        Preconditions.checkArgument(null != aHandler," A TsvStagingFileHandler is required");
        Preconditions.checkArgument(aHandler.isRegistered(),
                "The supplied  TsvStagingFileHandler does not reference a staging file");
        this.icgcStudyUrl = aUrl;
        this.modelClass = aClass;
        this.modelType = aType;
        this.fileHandler = aHandler;

    }

    @Override
    public String call() throws Exception {
        logger.info("Processing  study: " +this.icgcStudyUrl);
       switch(this.modelType){
           case StagingCommonNames.MUTATION_TYPE:
               logger.info("Invoking mutation transformation");
                return this.generateMutationStagingFile();
           case StagingCommonNames.CLINICAL_TYPE:
               logger.info("Invoking clinical transformation");
               return this.generateClinicalStagingFile();
           case StagingCommonNames.STRUCTURAL_MUTATION_TYPE:
               logger.info("Invoking structural variation transformation");
               return this.generateFusionFile();
          default:
              logger.error(this.modelType +" is not a supported model type");
              return "";

       }
    }

    /*
    process ICGC structural variant files
     */

    private String generateFusionFile() {
        int lineCount = 0;
        try {

            BufferedReader rdr = new BufferedReader(new InputStreamReader(IOUtils.getInputStreamFromURLOrClasspathOrFileSystem(this.icgcStudyUrl)));
            String line = "";
            while ((line = rdr.readLine()) != null) {
                if (lineCount++ > 0) {
                    IcgcFusionModel model =  StringUtils.columnStringToObject(this.modelClass,
                            line, StagingCommonNames.tabPattern, IcgcFunctionLibrary.resolveFieldNames(this.modelClass));
                    fileHandler.transformImportDataToTsvStagingFile(Lists.newArrayList(model), IcgcFusionModel.getTransformationFunction());
                    // reverse to and from genes for second model if they are different genes
                    if( !Strings.isNullOrEmpty(model.getGene_affected_by_bkpt_to()) && !model.getGene_affected_by_bkpt_to().equals(model.getGene())){
                        model.swapToAndFromLocations();
                        // output reversed model
                        fileHandler.transformImportDataToTsvStagingFile(Lists.newArrayList(model), IcgcFusionModel.getTransformationFunction());
                    }
                }
            }
        } catch (IOException | InvocationTargetException  |NoSuchMethodException
                |NoSuchFieldException  | InstantiationException | IllegalAccessException e ) {
            logger.error("Error at line count " +lineCount +" url: " +this.icgcStudyUrl);
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "Processed " +lineCount +" structural variation staging records";
    }



    /*
    process ICGC clinical files
     */
    private String generateClinicalStagingFile(){
        int lineCount = 0;
        try {

            BufferedReader rdr = new BufferedReader(new InputStreamReader(IOUtils.getInputStreamFromURLOrClasspathOrFileSystem(this.icgcStudyUrl)));
            String line = "";
            while ((line = rdr.readLine()) != null) {
                if (lineCount++ > 0) {
                    IcgcClinicalModel model =  StringUtils.columnStringToObject(this.modelClass,
                            line, StagingCommonNames.tabPattern, IcgcFunctionLibrary.resolveFieldNames(this.modelClass));

                        fileHandler.transformImportDataToTsvStagingFile(Lists.newArrayList(model),model.transformationFunction );
                }
            }
        } catch (IOException | InvocationTargetException  |NoSuchMethodException
                |NoSuchFieldException  | InstantiationException | IllegalAccessException e ) {
            logger.error("Error at line count " +lineCount +" url: " +this.icgcStudyUrl);
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "Processed " +lineCount +" clinical staging records";
    }

    private String generateMutationStagingFile() {
        int lineCount = 0;
       /*
       mutation files are filtered for duplicate records using a BloomFilter and a subset of
       MutationModel attributes as specified in the MutationModelFunnel class
        */
        BloomFilter<MutationModel> modelFilter = BloomFilter.create(new MutationModelFunnel(), 5000000);
        try {
            BufferedReader rdr = new BufferedReader(new InputStreamReader(IOUtils.getInputStreamFromURLOrClasspathOrFileSystem(this.icgcStudyUrl)));
            String line = "";
            while ((line = rdr.readLine()) != null) {
                if (lineCount++ > 0) {
                    MutationModel model =  StringUtils.columnStringToObject(this.modelClass,
                            line, StagingCommonNames.tabPattern, IcgcFunctionLibrary.resolveFieldNames(this.modelClass));
                    if (!modelFilter.mightContain(model)) {
                        fileHandler.transformImportDataToTsvStagingFile(Lists.newArrayList(model), model.getTransformationFunction() );
                        modelFilter.put(model);
                    }
                }
            }
        } catch (IOException | InvocationTargetException  |NoSuchMethodException
                |NoSuchFieldException  | InstantiationException | IllegalAccessException e ) {
            logger.error("Error at line count " +lineCount +" url: " +this.icgcStudyUrl);
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "Processed " +lineCount +" simple somatic  records";
    }

    // main class for stand alone testing
    public static void main (String...args){
        String studyUrl = "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/PRAD-CA/structural_somatic_mutation.PRAD-CA.tsv.gz";
        Path tempPath = Paths.get("/tmp/icgctest/data_fusions.txt");

        TsvFileHandler handler = FileHandlerService.INSTANCE.obtainFileHandlerForNewStagingFile
                (tempPath,FusionModel.resolveColumnNames());

        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
        IcgcCancerStudyETLCallable etl = new IcgcCancerStudyETLCallable(studyUrl, IcgcFusionModel.class,
                StagingCommonNames.STRUCTURAL_MUTATION_TYPE, handler);
        ListenableFuture<String> lf = service.submit(etl);
        try {
            logger.info(lf.get(600, TimeUnit.SECONDS));
            lf.cancel(true);
            service.shutdown();
            logger.info("service shutdown ");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        logger.info("FINIS");
    }
}
