/*
 *  Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * 
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 *  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 *  documentation provided hereunder is on an "as is" basis, and
 *  Memorial Sloan-Kettering Cancer Center 
 *  has no obligations to provide maintenance, support,
 *  updates, enhancements or modifications.  In no event shall
 *  Memorial Sloan-Kettering Cancer Center
 *  be liable to any party for direct, indirect, special,
 *  incidental or consequential damages, including lost profits, arising
 *  out of the use of this software and its documentation, even if
 *  Memorial Sloan-Kettering Cancer Center 
 *  has been advised of the possibility of such damage.
 */
package org.mskcc.cbio.importer.icgc.importer;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.*;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.etl.IcgcStudyFileEtlCallable;
import org.mskcc.cbio.importer.icgc.support.IcgcImportService;
import org.mskcc.cbio.importer.icgc.support.IcgcMetadataService;
import org.mskcc.cbio.importer.icgc.transformer.IcgcFileTransformer;
import org.mskcc.cbio.importer.icgc.transformer.SimpleSomaticFileTransformer;
import org.mskcc.cbio.importer.model.IcgcMetadata;
import org.mskcc.cbio.importer.persistence.staging.MetadataFileHandler;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;
import org.mskcc.cbio.importer.util.PropertiesLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import scala.Tuple3;

import javax.annotation.Nullable;

/*
 responsible for invoking ETL operations for simple somatic ICGC studys
 primary input is a list of ICGC studys

 */
public class SimpleSomaticMutationImporter implements Callable<String> {

    /*
    responsible for:
        1. generating a List of URLs for ICGC simple somatic mutation files
        2. instantiating a SimpleSomaticFileTransformer object
        3. invoking multiple IcgcStudyFileETL operations to import ICGC data
     */

    private static Logger logger = Logger.getLogger(SimpleSomaticMutationImporter.class);
    private static final Integer ETL_THREADS = 4;
    private boolean processCompleteFlag = false;
    private final Path baseStagingPath;
    private final Set<String> completedFiles = Sets.newHashSet();
    final ListeningExecutorService service =
            MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(ETL_THREADS));

    public SimpleSomaticMutationImporter(Path aPath) {
        Preconditions.checkArgument(null != aPath);
        this.baseStagingPath = aPath;
    }

    @Override
    public String call() throws Exception {
        this.generateMetaDataFiles();
        return StagingCommonNames.lineJoiner.join(this.processSimpleSomaticMutations());
    }

    /*
    private method to create a Collection of the attributes needed to import & transform
    ICGC files
    use a Tuple3 as a data value object containing: (1) Path to the study's staging file directory,
    (2) the URL to the ICGC source file, and (3) a ICGCFileTransformer implementation
     */
    private List<Tuple3<Path, String, IcgcFileTransformer>> resolveImportAttributeList() {
        // get simple somatic mutation URLs for registered studies
        final Map<String, String> mutationUrlMap = IcgcImportService.INSTANCE.getIcgcMutationUrlMap();
        return FluentIterable.from(mutationUrlMap.keySet())
                .transform(new Function<String, Tuple3<Path, String, IcgcFileTransformer>>() {
                    @Nullable
                    @Override
                    public Tuple3<Path, String, IcgcFileTransformer> apply(String studyId) {
                        final IcgcMetadata meta = IcgcMetadataService.INSTANCE.getIcgcMetadataById(studyId);
                        final Path stagingDirectoryPath = Paths.get(StagingCommonNames.pathJoiner.join(baseStagingPath,
                                meta.getDownloaddirectory()));
                        final String url = mutationUrlMap.get(studyId);
                        final IcgcFileTransformer transformer = (IcgcFileTransformer) new SimpleSomaticFileTransformer(
                                new MutationFileHandlerImpl(), stagingDirectoryPath);
                        return new Tuple3<Path, String, IcgcFileTransformer>(stagingDirectoryPath, url,
                                transformer);
                    }
                }).toList();
    }

    private void generateMetaDataFiles(){
        for(String studyId : IcgcMetadataService.INSTANCE.getRegisteredIcgcStudyList()){
            IcgcMetadata meta = IcgcMetadataService.INSTANCE.getIcgcMetadataById(studyId);
            final Path stagingDirectoryPath = Paths.get(StagingCommonNames.pathJoiner.join(baseStagingPath,
                    meta.getDownloaddirectory()));
            try {
                Files.createDirectories(stagingDirectoryPath);
            } catch (IOException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
            Path metadataPath = stagingDirectoryPath.resolve("meta_mutations_extended.txt");
            MetadataFileHandler.INSTANCE.generateMetadataFile(this.generateMetadataMap(meta),
                    metadataPath);
        }

    }

    private Map<String,String> generateMetadataMap(IcgcMetadata meta){
        Map<String,String> metaMap = Maps.newTreeMap();
        metaMap.put("001cancer_study_identifier:", meta.getStudyname());
        metaMap.put("002stable_id:",meta.getStudyname()+"_mutations");
        metaMap.put("003genetic_alteration_type:","MUTATION_EXTENDED");
        metaMap.put("004show_profile_in_analysis_tab:","true");
        metaMap.put("005profile_description:",meta.getDescription());
        metaMap.put("006profile_name:","mutations");
        metaMap.put("007datatype:","MAF");
        return metaMap;
    }
    /*
    public method to initiate processing of simple somatic mutations for ICGC studies registered
    in the cbio portal
    Returns a List of Strings (i.e. messages) for successfully processed studies
     */
    public List<String> processSimpleSomaticMutations() {
        final List<String> retList = Lists.newArrayList();
        List<ListenableFuture<String>> futureList = Lists.newArrayList();

        final List<Tuple3<Path, String, IcgcFileTransformer> >tupleList = this.resolveImportAttributeList();
        final Integer studyCount =  tupleList.size();
        for (Tuple3<Path, String, IcgcFileTransformer> tuple3 : tupleList) {
            futureList.add(service.submit(new IcgcStudyFileEtlCallable(tuple3._1(),
                    tuple3._2(), tuple3._3())));

            ListenableFuture<List<String>> etlResults = Futures.successfulAsList(futureList);
            Futures.addCallback(etlResults, new FutureCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> resultList) {
                    completedFiles.addAll(resultList);
                    logger.info("Transformation success callback - completed count = " + completedFiles.size());
                    if(completedFiles.size() == studyCount ) {
                        processCompleteFlag = true;
                        logger.info("Process complete flag set to true");
                    }
                }
                @Override
                public void onFailure(Throwable t) {
                    logger.error(t.getMessage());
                }
            });
        }
        return Lists.newArrayList(completedFiles);
    }

    /*
    main method for testing
    */
    public static void main(String... args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext-importer.xml");
        SimpleSomaticMutationImporter importer = (SimpleSomaticMutationImporter)
                applicationContext.getBean("icgcSimpleSomaticImporter");
        List<ListenableFuture<String>> futureList = Lists.newArrayList();
        futureList.add(importer.service.submit(importer));
        ListenableFuture<List<String>> etlResults = Futures.successfulAsList(futureList);
        Futures.addCallback(etlResults, new FutureCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> resultList) {
                    for (String result : resultList) {
                        logger.info("Transformed study: " +result);
                }
            }
            @Override
            public void onFailure(Throwable t) {
                logger.error(t.getMessage());
            }
        });
        logger.info("futures submitted");
        // loop until work is done
        while(!importer.processCompleteFlag){
            try {
                Thread.sleep(120000L);
                logger.info("Waiting.....completion flag = " +importer.processCompleteFlag);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // all transformations have completed - shutdown the executor service  and exit
        for(String s : importer.completedFiles){
            logger.info("Completed staging file: " +s);
        }
        importer.service.shutdownNow();
        logger.info("service shutdown");
        logger.info("FINIS...");
        System.exit(0);
    }

}
