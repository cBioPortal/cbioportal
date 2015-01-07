package org.mskcc.cbio.importer.icgc.importer;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.google.common.util.concurrent.*;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.etl.IcgcCancerStudyETLCallable;
import org.mskcc.cbio.importer.icgc.etl.IcgcCopyNumberETLCallable;
import org.mskcc.cbio.importer.icgc.etl.IcgcSegmentDataETLCallable;
import org.mskcc.cbio.importer.icgc.model.IcgcClinicalModel;
import org.mskcc.cbio.importer.icgc.model.IcgcFusionModel;
import org.mskcc.cbio.importer.icgc.model.IcgcSimpleSomaticMutationModel;
import org.mskcc.cbio.importer.icgc.support.IcgcFunctionLibrary;
import org.mskcc.cbio.importer.icgc.support.IcgcMetadataService;
import org.mskcc.cbio.importer.model.IcgcMetadata;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;
import org.mskcc.cbio.importer.persistence.staging.cnv.CnvFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.fusion.FusionModel;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationTransformation;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

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
 * Created by criscuof on 12/25/14.
 */
public class IcgcCancerStudyImporter implements Callable<String> {
    /*
    Callable implementation that is responsible for the ETL operations
    for a specified ICGC Cancer Study

     */
    private static Logger logger = Logger.getLogger(IcgcCancerStudyImporter.class);
    private final IcgcMetadata metadata;
    private final Path stagingFileDirectory;
    private static final Integer ETL_THREADS = 12;
    private boolean completedFlag = false;
    private final List<ListenableFuture<String>> futureList = Lists.newArrayList();
    final ListeningExecutorService service =
            MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(ETL_THREADS));

    public IcgcCancerStudyImporter(
            IcgcMetadata meta, Path aPath) {
        Preconditions.checkArgument(null != meta,
                "An IcgcMetadata object is required");
        Preconditions.checkArgument(StagingUtils.isValidStagingDirectoryPath(aPath),
                "The staging file directory is null or invalid");
        this.metadata = meta;
        this.stagingFileDirectory = aPath.resolve(this.metadata.getDownloaddirectory());
        logger.info("Staging file directory set to " + this.stagingFileDirectory);

    }

    public boolean isCompleted() {
        return this.completedFlag;
    }

    @Override
    public String call() throws Exception {
        return this.invokeEtlTasks();

    }

    private String invokeEtlTasks() {

        List<Callable<String>> etlTaskList = this.resolveEtlTasks();
        final Integer submittedStudies = etlTaskList.size();
        final List<String> completedStudies = Lists.newArrayList();

        for (Callable<String> etlTask : etlTaskList) {
            futureList.add(service.submit(etlTask));
        }
        final StringBuffer sb = new StringBuffer();   // StringBuffer for concurrency;
        ListenableFuture<List<String>> etlResults = Futures.successfulAsList(futureList);
        Futures.addCallback(etlResults, new FutureCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> resultList) {

                for (String result : resultList) {
                    logger.info("Transformed study: " + result);
                    sb.append(result + "\n");
                    completedStudies.add(result);
                    if (completedStudies.size() == submittedStudies) {
                        completedFlag = true;
                        logger.info("All tasks have been completed");
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error(t.getMessage());
            }
        });
        //}
        return sb.toString();
    }

    /*
    private method to construct a list of transformation tasks for the current ICGC cancer study
    since not every cancer study contains the same set of variation files, the task list will
    vary based on whether there is an appropriate file type registered for that study
     */
    private List<Callable<String>> resolveEtlTasks() {
        List<Callable<String>> etlTasks = Lists.newArrayList();

        if (!Strings.isNullOrEmpty(this.metadata.getSomaticmutationurl())) {
            Path stagingFilePath = this.stagingFileDirectory.resolve(StagingCommonNames.MUTATIONS_STAGING_FILENAME);
            TsvStagingFileHandler aHandler = new MutationFileHandlerImpl();
            aHandler.registerTsvStagingFile(stagingFilePath,
                    Lists.newArrayList(MutationTransformation.INSTANCE.getTransformationMap().keySet()), true);
            etlTasks.add(new IcgcCancerStudyETLCallable(this.metadata.getSomaticmutationurl(),
                    IcgcSimpleSomaticMutationModel.class, StagingCommonNames.MUTATION_TYPE, aHandler));
            logger.info("Added clinical transformation for: " + this.metadata.getSomaticmutationurl());

        }
        // clinical

        if (!Strings.isNullOrEmpty(this.metadata.getClinicalurl())) {
            Path stagingFilePath = this.stagingFileDirectory.resolve(StagingCommonNames.CLINICAL_STAGING_FILENAmE);
            TsvStagingFileHandler aHandler = new MutationFileHandlerImpl();
            aHandler.registerTsvStagingFile(stagingFilePath, Lists.newArrayList(
                    IcgcFunctionLibrary.resolveColumnNames(IcgcClinicalModel.transformationMap)), true);
            etlTasks.add(new IcgcCancerStudyETLCallable(this.metadata.getClinicalurl(),
                    IcgcClinicalModel.class, StagingCommonNames.CLINICAL_TYPE, aHandler));
            logger.info("Added clinical transformation for: " + this.metadata.getClinicalurl());

        }
        /*
         ICGC copy number variation data generates two (2) staging files:
            1.  CNA matrix file
            2. segment file
         */
        if (!Strings.isNullOrEmpty(this.metadata.getCopynumberurl())) {
            // generate the CNA file
            //Path stagingFilePath = this.stagingFileDirectory.resolve(StagingCommonNames.CNA_STAGING_FILENAME);
            etlTasks.add(new IcgcCopyNumberETLCallable(this.metadata, this.stagingFileDirectory,new CnvFileHandlerImpl() ));
            logger.info("Added copy number transformation for " +this.metadata.getCopynumberurl());
            // generate the segment file
            etlTasks.add(new IcgcSegmentDataETLCallable(this.metadata, this.stagingFileDirectory, new MutationFileHandlerImpl()));
            logger.info("Added segment transformation for " +this.metadata.getCopynumberurl());

        }
        if (!Strings.isNullOrEmpty(this.metadata.getExparrayurl())) {

        }
        if (!Strings.isNullOrEmpty(this.metadata.getExpsequrl())) {

        }
        if (!Strings.isNullOrEmpty(this.metadata.getMetharrayurl())) {

        }
        if (!Strings.isNullOrEmpty(this.metadata.getMethsequrl())) {

        }
        if (!Strings.isNullOrEmpty((this.metadata.getMirnasequrl()))) {

        }


        if (!Strings.isNullOrEmpty(this.metadata.getSplicevarianturl())) {

        }
        if (!Strings.isNullOrEmpty(this.metadata.getStructuralmutationurl())) {
            Path stagingFilePath = this.stagingFileDirectory.resolve("data_fusions.txt");
            TsvStagingFileHandler aHandler = new MutationFileHandlerImpl();
            aHandler.registerTsvStagingFile(stagingFilePath,
                    Lists.newArrayList(FusionModel.transformationMap.keySet()), true);
            etlTasks.add(new IcgcCancerStudyETLCallable(this.metadata.getStructuralmutationurl(),
                    IcgcFusionModel.class, StagingCommonNames.STRUCTURAL_MUTATION_TYPE, aHandler));
            logger.info("Added  fusion transformation for: " + this.metadata.getStructuralmutationurl());
        }
        return etlTasks;
    }


    // main method for stand alone testing

    public static void main(String... args) {
        IcgcMetadata metadata = IcgcMetadataService.INSTANCE.getIcgcMetadataById("PACA-CA");
        logger.info("Metadata for " + metadata.getIcgcid() + " obtained");
        Path basePath = Paths.get("/tmp/icgctest");
        final ListeningExecutorService service =
                MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
        IcgcCancerStudyImporter importer = new IcgcCancerStudyImporter(metadata, basePath);

        List<ListenableFuture<String>> futureList = Lists.newArrayList();
        futureList.add(service.submit(importer));

        ListenableFuture<List<String>> etlResults = Futures.successfulAsList(futureList);
        Futures.addCallback(etlResults, new FutureCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> resultList) {
                for (String result : resultList) {
                    logger.info("Transformed study: " + result);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error(t.getMessage());
            }
        });
        logger.info("futures submitted");
        // loop until work is done
        while (!importer.isCompleted()) {
            try {
                Thread.sleep(30000L);
                logger.info("Waiting.....completion flag = " + importer.isCompleted());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        importer.service.shutdownNow();
        service.shutdown();
        logger.info("service shutdown");
        logger.info("FINIS...");
        System.exit(0);

    }

}
