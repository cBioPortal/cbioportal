package org.mskcc.cbio.importer.task;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


import java.util.List;
import java.util.concurrent.Callable;
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


    final ListeningExecutorService service =
            MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));
    // List of ICGC Import tasks - populated by Spring configuration
    private List<Callable<List<String>>> icgcImporterList;


    public IcgcDataImportTask(List<Callable<List<String>>> importerList) {
        Preconditions.checkArgument(null != importerList, "A List of callable import tasks is required");
        this.icgcImporterList = importerList;
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
        final List<String> retList = Lists.newArrayList();
        List<ListenableFuture<List<String>>> futureList = Lists.newArrayList();
        for (Callable<List<String>> task : this.icgcImporterList){
            futureList.add( service.submit(task));
            ListenableFuture<List<List<String>>> taskResults = Futures.successfulAsList(futureList);
            Futures.addCallback(taskResults, new FutureCallback<List<List<String>>>() {
                @Override
                public void onSuccess(List<List<String>> resultListList) {
                    for(List<String> sl : resultListList){
                      retList.addAll(sl);
                    }
                }
                @Override
                public void onFailure(Throwable t) {
                    logger.error(t.getMessage());
                    t.printStackTrace();
                }
            });
        }
    }

    @Override
    protected Scheduler scheduler() {
        // define how often this task should be invoked
        return Scheduler.newFixedRateSchedule(0,61, TimeUnit.SECONDS);
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
