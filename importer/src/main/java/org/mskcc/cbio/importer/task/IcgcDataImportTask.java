package org.mskcc.cbio.importer.task;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.importer.IcgcCancerStudyImporter;
import org.mskcc.cbio.importer.icgc.importer.SimpleSomaticMutationImporter;
import org.mskcc.cbio.importer.icgc.support.IcgcMetadataService;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.IcgcMetadata;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
 * Created by criscuof on 12/7/14.
 */
public class IcgcDataImportTask extends AbstractScheduledService {
    private static final Logger logger = Logger.getLogger(IcgcDataImportTask.class);
    private  Path baseStagingPath;

    final ListeningExecutorService service =
            MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(12));

    public IcgcDataImportTask() {
        this.resolveBaseStagingPath();
    }

    /*
    private method to resolve the current base directory for ICGC staging files
    register in the importer spreadsheet data sources worksheet
    the directory will be created if it does not exist
     */
    private void resolveBaseStagingPath() {
        Optional<DataSourcesMetadata> optMeta = DataSourcesMetadata
                .findDataSourcesMetadataByDataSourceName(StagingCommonNames.DATA_SOURCE_ICGC);
        if (optMeta.isPresent()) {
            this.baseStagingPath = optMeta.get().resolveBaseStatgingPath();
        } else {
            this.baseStagingPath = Paths.get(StagingCommonNames.DEFAULT_BASE_DIRECTORY);
        }
            if (!Files.exists(this.baseStagingPath)) {
                try {
                    Files.createDirectories(this.baseStagingPath);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }
    }

    protected void startup() {
        logger.info("IcgcDataImportTask started");
    }

    /*
    The import task should run on a continuous basis.
    Stopping implies an error condition
     */
    protected void shutdown() {
        logger.error("ERROR: IcgcDataImportTask stopped");
    }

    /*
    operation to be run once, every time this task is invoked by the scheduler
     */
    @Override
    protected void runOneIteration() throws Exception {
        logger.info("ICGC Import process invoked");

        List<ListenableFuture<String>> futureList = Lists.newArrayList();
        // run the SimpleSomaticMutationTransformer
        futureList.add(service.submit(new SimpleSomaticMutationImporter(baseStagingPath)));
        // run the smaller importers

        for (String icgcId : IcgcMetadata.getRegisteredIcgcStudyList()) {
            futureList.add(service.submit(new IcgcCancerStudyImporter(icgcId, this.baseStagingPath)));
            logger.info("Task added for " +icgcId);
        }
        ListenableFuture<List<String>> taskResults = Futures.successfulAsList(futureList);
        Futures.addCallback(taskResults, new FutureCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> resultList) {
                for(String s : resultList){
                    logger.info(s);

                }
            }
            @Override
            public void onFailure(Throwable t) {
                logger.error(t.getMessage());
                t.printStackTrace();
            }
        });

    }

    @Override
    protected Scheduler scheduler() {
        // define how often this task should be invoked
        return Scheduler.newFixedRateSchedule(0,61, TimeUnit.MINUTES);
    }
    /*
    main method for stand alone testing outside of task scheduler
     */
    public static void main(String... args){
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext-importer.xml");
        IcgcDataImportTask importTask = (IcgcDataImportTask) applicationContext.getBean("icgcImportTask");
        try {
            importTask.runOneIteration();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

}
