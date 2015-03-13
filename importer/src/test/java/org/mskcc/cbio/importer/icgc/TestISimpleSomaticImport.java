package org.mskcc.cbio.importer.icgc;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.etl.IcgcSimpleSomaticMutationETLCallable;
import org.mskcc.cbio.importer.model.IcgcMetadata;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
 * Created by criscuof on 2/17/15.
 */
public class TestISimpleSomaticImport {
    private final static Logger logger = Logger.getLogger(TestISimpleSomaticImport.class);
    // main class for stand alone testing
    public static void main(String... args) {
        Path tempPath = Paths.get("/tmp/icgctest");


            IcgcMetadata icgcMetadata = IcgcMetadata.getIcgcMetadataById("PAEN-AU").get();
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
