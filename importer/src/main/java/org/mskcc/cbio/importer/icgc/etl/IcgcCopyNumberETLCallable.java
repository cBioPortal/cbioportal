package org.mskcc.cbio.importer.icgc.etl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.*;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.util.StringUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.model.IcgcCopyNumberModel;
import org.mskcc.cbio.importer.icgc.support.IcgcFunctionLibrary;
import org.mskcc.cbio.importer.model.IcgcMetadata;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.cnv.CnvFileHandler;
import org.mskcc.cbio.importer.persistence.staging.cnv.CnvFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.cnv.CnvTransformer;
import scala.Tuple3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * Created by criscuof on 12/31/14.
 */
public class IcgcCopyNumberETLCallable extends CnvTransformer implements Callable<String> {

    private static final Logger logger = Logger.getLogger(IcgcCopyNumberETLCallable.class);
    private final String icgcCopyNumberUrl;

    public IcgcCopyNumberETLCallable (IcgcMetadata meta, Path aPath, CnvFileHandler aHandler) {
        super(aHandler);
        Preconditions.checkArgument(null != meta && !Strings.isNullOrEmpty(meta.getCopynumberurl()), "The ICGC metadata parameter is null or invalid");
        Preconditions.checkArgument(null != aPath, "A Path to the staging file directory is required");
        this.icgcCopyNumberUrl = meta.getCopynumberurl();
        this.registerStagingFileDirectory(aPath,false);
    }

    @Override
    public String call() throws Exception {
        return this.processIcgcCopyNumberFile();
    }

    /*
    process the ICGC Copy Number data directly using either the ICGC URL or a local file
    map copy number data to a gene/sample table
    persist the data to a file
     */
    private String processIcgcCopyNumberFile(){
        int lineCount = 0;
        try {
            BufferedReader rdr = new BufferedReader(new InputStreamReader(IOUtils.getInputStreamFromURLOrClasspathOrFileSystem(this.icgcCopyNumberUrl)));
            String line = "";
            while ((line = rdr.readLine()) != null) {
                if (lineCount++ > 0){
                    IcgcCopyNumberModel model = StringUtils.columnStringToObject(IcgcCopyNumberModel.class, line, StagingCommonNames.tabPattern,
                            IcgcFunctionLibrary.resolveFieldNames(IcgcCopyNumberModel.class));
                    // add data to cna table id the variation can be mapped to a gene
                    Optional<Tuple3<String,String,String> > optTuple = model.resolveCnvTuple();
                    if (optTuple.isPresent()) {
                        this.registerCnv(optTuple.get());
                    }
                }
            }
            this.persistCnvData();
           return("ICGC Copy Number URL: " +this.icgcCopyNumberUrl +" processed " +lineCount
                    +" ICGC records.");

        } catch (IOException | InvocationTargetException |NoSuchMethodException
                |NoSuchFieldException  | InstantiationException | IllegalAccessException e ) {
            logger.error("Error at line count " +lineCount +" url: " +this.icgcCopyNumberUrl);
            logger.error(e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }

    // main  method for standalone testing
    public static void main (String...args) {
        final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));

        IcgcMetadata meta = IcgcMetadata.getIcgcMetadataById("OV-AU").get();  // throws an Exception if icgcid is invalid
        Path testPath = Paths.get("/tmp/icgctest");

        ListenableFuture<String> lf = service.submit(new IcgcCopyNumberETLCallable(meta,testPath, new CnvFileHandlerImpl()));
        Futures.addCallback(lf, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                logger.info("Result: " + result);
                service.shutdown();
                System.exit(0);
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error(t.getMessage());
                t.printStackTrace();
                service.shutdown();
                System.exit(-1);
            }
        });

    }

}
