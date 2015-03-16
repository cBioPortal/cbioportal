package org.mskcc.cbio.importer.icgc.etl;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.util.StringUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.model.IcgcSimpleSomaticMutationModel;
import org.mskcc.cbio.importer.icgc.support.IcgcFunctionLibrary;
import org.mskcc.cbio.importer.model.IcgcMetadata;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.filehandler.FileHandlerService;
import org.mskcc.cbio.importer.persistence.staging.filehandler.TsvFileHandler;
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
public class IcgcSimpleSomaticMutationETLCallable  implements Callable<String> {
    /*
    responsible for obtaining data from the ICGC Data Repository using a
    supplied URL and transforming them into a staging file
     */
    private static final Logger logger = Logger.getLogger(IcgcSimpleSomaticMutationETLCallable.class);
    private final String icgcStudyUrl;
    private Path stagingFileDirectory;



    public IcgcSimpleSomaticMutationETLCallable (final String somaticMutationUrl, Path aPath){
        Preconditions.checkArgument(aPath != null, "A Path to an import staging directory is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty((somaticMutationUrl)),
                "A URL to an ICGC simple somatic mutation file is required");
        this.icgcStudyUrl = somaticMutationUrl;
        this.stagingFileDirectory = aPath;
    }

    @Override
    public String call() throws Exception {
        logger.info("Processing  study: " + this.icgcStudyUrl +" for simple somatic mutations");
        return this.generateMutationStagingFile();
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
        return "Processed simple somatic records for ICGC URL " + this.icgcStudyUrl;
    }

    // main class for stand alone testing
    public static void main(String... args) {
        Path tempPath = Paths.get("/tmp/icgctest");
        for (String icgcId : IcgcMetadata.getRegisteredIcgcStudyList()) {

            IcgcMetadata icgcMetadata = IcgcMetadata.getIcgcMetadataById(icgcId).get();
            logger.info("Processing ICGC somatic mutations for " +icgcMetadata.getStudyname());
            ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
            ListenableFuture<String> lf1 = null;
            // simple somatic mutations
            if (!Strings.isNullOrEmpty(icgcMetadata.getSomaticmutationurl())) {
                IcgcSimpleSomaticMutationETLCallable etl1 = new IcgcSimpleSomaticMutationETLCallable(icgcMetadata.getSomaticmutationurl(),
                        tempPath.resolve(icgcMetadata.getDownloaddirectory()));
                lf1 = service.submit(etl1);
            }
            try {
                logger.info(lf1.get(30, TimeUnit.MINUTES));
                lf1.cancel(true);
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


}
