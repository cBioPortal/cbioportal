package org.mskcc.cbio.importer.task;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.apache.log4j.Logger;

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

    @Override
    protected void runOneIteration() throws Exception {
        logger.info("The ICGC data fetcher application will now check for new/revised ICGC");

    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0,61, TimeUnit.SECONDS);
    }
}
