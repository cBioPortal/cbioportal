package org.mskcc.cbio.importer.task;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.fetcher.internal.DmpDarwinFetcherImpl;

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
public class DmpDarwinImportTask extends AbstractScheduledService {
    private static final Logger logger = Logger.getLogger(DmpDarwinImportTask.class);
    private final Fetcher fetcher;
    private String dataSource ;

    public DmpDarwinImportTask(Fetcher fetcher){
        Preconditions.checkArgument(null != fetcher ,
               "A Fetcher implementation is required" );
        this.fetcher = fetcher;
    }

    protected void startup() {
        logger.info("DMPWebServiceImportTask started");
    }

    /*
    The import task should run on a continuous basis.
    Stopping implies an error condition
     */
    protected void shutdown() {
        logger.error("ERROR: DMPWebServiceImportTask stopped");
    }

    @Override
    protected void runOneIteration() throws Exception {
        logger.info("The DMP Web service fetcher application will now check for new/revised DMP " +
                "& Darwin data");
        this.fetcher.fetch(this.dataSource, "");

    }

    @Override
    //TODO: make the time interval a property
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0,60, TimeUnit.SECONDS);
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}
