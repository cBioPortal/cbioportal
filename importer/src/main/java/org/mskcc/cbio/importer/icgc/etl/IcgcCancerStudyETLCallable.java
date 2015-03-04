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
import org.mskcc.cbio.importer.icgc.model.IcgcSimpleSomaticMutationModel;
import org.mskcc.cbio.importer.icgc.support.IcgcFunctionLibrary;
import org.mskcc.cbio.importer.model.IcgcMetadata;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.filehandler.FileHandlerService;
import org.mskcc.cbio.importer.persistence.staging.filehandler.TsvFileHandler;
import org.mskcc.cbio.importer.persistence.staging.fusion.FusionModel;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationModel;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationModelFunnel;
import rx.Observable;
import rx.Subscriber;
import rx.observables.StringObservable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
    responsible for obtaining data from the ICGC Data Repository using a
    supplied URL and transforming them into a staging file
     */
    private static final Logger logger = Logger.getLogger(IcgcCancerStudyETLCallable.class);
    private final String icgcStudyUrl;
    private final Class modelClass;
    private final String modelType;
    private Path stagingFileDirectory;


    public IcgcCancerStudyETLCallable(String aUrl, Class aClass,
                                      String aType, Path aPath) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aUrl), "An ICGC Cancer study is required");
        Preconditions.checkArgument(null != aClass, "A model Class is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aType), "A model type specification is required");
        Preconditions.checkArgument(null != aPath, " A Path to the ICGC staging file directory is required");
        this.icgcStudyUrl = aUrl;
        this.modelClass = aClass;
        this.modelType = aType;
        this.stagingFileDirectory = aPath;
    }

    @Override
    public String call() throws Exception {
        logger.info("Processing  study: " + this.icgcStudyUrl);
        switch (this.modelType) {
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
                logger.error(this.modelType + " is not a supported model type");
                return "";
        }
    }
    /*
    process ICGC structural variant files
     */

    private String generateFusionFile() {
        try (BufferedReader rdr = new BufferedReader(new InputStreamReader(IOUtils.getInputStreamFromURLOrClasspathOrFileSystem(this.icgcStudyUrl)))) {
            Observable<StringObservable.Line> lineObservable =
                    StringObservable.byLine(StringObservable.from(rdr)).skip(1);  // skip the header
            lineObservable.subscribe(new Subscriber<StringObservable.Line>() {
                List<IcgcFusionModel> modelList = Lists.newArrayList();

                @Override
                public void onCompleted() {
                    // output the ICGC fusion data to a new file
                    TsvFileHandler fh = FileHandlerService.INSTANCE.obtainFileHandlerByDataType(stagingFileDirectory,
                            StagingCommonNames.DATATYPE_FUSION, FusionModel.resolveColumnNames(), true);
                    fh.transformImportDataToTsvStagingFile(modelList, IcgcFusionModel.getTransformationFunction());
                }

                @Override
                public void onError(Throwable throwable) {
                    logger.error(throwable.getMessage());
                    throwable.printStackTrace();

                }

                @Override
                public void onNext(StringObservable.Line line) {
                    try {
                        IcgcFusionModel model = StringUtils.columnStringToObject(modelClass,
                                line.getText(), StagingCommonNames.tabPattern, IcgcFunctionLibrary.resolveFieldNames(modelClass));
                        modelList.add(model);
                        // add entry for second gene
                        if (!Strings.isNullOrEmpty(model.getGene_affected_by_bkpt_to()) &&
                                !model.getGene_affected_by_bkpt_to().equals(model.getGene())) {
                            IcgcFusionModel model2 = IcgcFusionModel.generateSecondFusionModel(model);
                            modelList.add(model2);
                        }
                    } catch (InvocationTargetException | NoSuchMethodException
                            | NoSuchFieldException | InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }
            });

        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "ICGC Fusion data for " + this.icgcStudyUrl + " processed";
    }

    /*
    process ICGC clinical files
     */

    private String generateClinicalStagingFile() {
        try
                (BufferedReader rdr = new BufferedReader(new InputStreamReader
                        (IOUtils.getInputStreamFromURLOrClasspathOrFileSystem(this.icgcStudyUrl)))) {
            Observable<StringObservable.Line> lineObservable =
                    StringObservable.byLine(StringObservable.from(rdr)).skip(1);  // skip the header
            lineObservable.subscribe(new Subscriber<StringObservable.Line>() {
                List<IcgcClinicalModel> modelList = Lists.newArrayList();

                @Override
                public void onCompleted() {
                    // output clinical data to new staging file
                    final TsvFileHandler tsvFileHandler = FileHandlerService.INSTANCE.obtainFileHandlerByDataType(stagingFileDirectory,
                            StagingCommonNames.DATATYPE_CLINICAL,
                            IcgcFunctionLibrary.resolveColumnNames(IcgcClinicalModel.transformationMap), true);
                    tsvFileHandler.transformImportDataToTsvStagingFile(modelList, IcgcClinicalModel.transformationFunction);
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println(throwable.getMessage());
                }

                @Override
                public void onNext(StringObservable.Line line) {
                    try {
                        modelList.add((IcgcClinicalModel) StringUtils.columnStringToObject(IcgcClinicalModel.class,
                                line.getText(), StagingCommonNames.tabPattern,
                                IcgcFunctionLibrary.resolveFieldNames(IcgcClinicalModel.class)));
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                            NoSuchFieldException | InvocationTargetException e) {
                        e.printStackTrace();
                    }

                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Processed " + this.icgcStudyUrl + " ICGC clinical file";
    }

    private String generateMutationStagingFile() {
       /*
       mutation files are filtered for duplicate records using a BloomFilter and a subset of
       MutationModel attributes as specified in the MutationModelFunnel class
        */

        try (BufferedReader rdr = new BufferedReader(new InputStreamReader(IOUtils
                .getInputStreamFromURLOrClasspathOrFileSystem(this.icgcStudyUrl)));) {
            Observable<StringObservable.Line> lineObservable =
                    StringObservable.byLine(StringObservable.from(rdr)).skip(1);  // skip the header
            lineObservable.subscribe(new Subscriber<StringObservable.Line>() {
                List<IcgcSimpleSomaticMutationModel> modelList = Lists.newArrayList();
                BloomFilter<MutationModel> modelFilter = BloomFilter.create(new MutationModelFunnel(), 5000000);

                @Override
                public void onCompleted() {

                    final TsvFileHandler tsvFileHandler = FileHandlerService.INSTANCE.obtainFileHandlerByDataType(stagingFileDirectory,
                            StagingCommonNames.DATATYPE_MUTATION,
                            MutationModel.resolveColumnNames(), true);
                    tsvFileHandler.transformImportDataToTsvStagingFile(modelList, MutationModel.getTransformationFunction());

                }

                @Override
                public void onError(Throwable throwable) {
                    logger.error(throwable.getMessage());
                    throwable.printStackTrace();

                }

                @Override
                public void onNext(StringObservable.Line line) {

                    try {
                        IcgcSimpleSomaticMutationModel model =
                                (IcgcSimpleSomaticMutationModel) StringUtils.columnStringToObject(IcgcSimpleSomaticMutationModel.class,
                                        line.getText(), StagingCommonNames.tabPattern, IcgcFunctionLibrary.resolveFieldNames(IcgcSimpleSomaticMutationModel.class));
                        if (!modelFilter.mightContain(model)) {

                            modelFilter.put(model);
                            modelList.add(model);
                        }

                    } catch (InvocationTargetException | NoSuchMethodException
                            | NoSuchFieldException | InstantiationException | IllegalAccessException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }


                }
            });

        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return "Processed simple somatic  records for ICGC URL " + this.icgcStudyUrl;
    }

    // main class for stand alone testing
    public static void main(String... args) {

        IcgcMetadata icgcMetadata = IcgcMetadata.getIcgcMetadataById("PAEN-AU").get();
        Path tempPath = Paths.get("/tmp/icgctest");
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
        ListenableFuture<String> lf1 = null;
        ListenableFuture<String> lf2 = null;
        ListenableFuture<String> lf3 = null;
        // structural mutations

        if (!Strings.isNullOrEmpty(icgcMetadata.getStructuralmutationurl())) {
            IcgcCancerStudyETLCallable etl = new IcgcCancerStudyETLCallable(icgcMetadata.getStructuralmutationurl(), IcgcFusionModel.class,
                    StagingCommonNames.STRUCTURAL_MUTATION_TYPE, tempPath);
            lf1 = service.submit(etl);
        }

        //clinical
        if (!Strings.isNullOrEmpty(icgcMetadata.getClinicalurl())) {
            IcgcCancerStudyETLCallable etl2 = new IcgcCancerStudyETLCallable(icgcMetadata.getClinicalurl(), IcgcClinicalModel.class,
                    StagingCommonNames.CLINICAL_TYPE, tempPath);
            lf2 = service.submit(etl2);
        }


        // simple somatic mutations
        if (!Strings.isNullOrEmpty(icgcMetadata.getSomaticmutationurl())) {
            IcgcCancerStudyETLCallable etl3 = new IcgcCancerStudyETLCallable(icgcMetadata.getSomaticmutationurl(), MutationModel.class,
                    StagingCommonNames.MUTATION_TYPE, tempPath);
            lf3 = service.submit(etl3);
        }

        try {
            //logger.info(lf1.get(600, TimeUnit.SECONDS));
            logger.info(lf2.get(600, TimeUnit.SECONDS));
            logger.info(lf3.get(600, TimeUnit.SECONDS));
//            lf1.cancel(true);
            lf2.cancel(true);
            lf3.cancel(true);
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
