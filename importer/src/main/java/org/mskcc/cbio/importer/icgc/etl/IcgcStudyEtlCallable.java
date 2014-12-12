package org.mskcc.cbio.importer.icgc.etl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.transformer.IcgcFileTransformer;
import org.mskcc.cbio.importer.icgc.transformer.SimpleSomaticFileTransformer;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;

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
 * Created by criscuof on 12/10/14.
 */
public class IcgcStudyEtlCallable implements Callable<String> {
    private static Logger logger = Logger.getLogger(IcgcStudyEtlCallable.class);
    private ListeningExecutorService service;
    private static final Integer defaultThreadCount = 3;
    private Path stagingFilePath;
    private String sourceUrl;
    private IcgcFileTransformer transformer;
    /*
    this class is responsible for extracting the file from the ICGC site, decompressing it,
    transforming it to a staging data format, and outputting the transformed data to the
    specified staging path
     */

    public IcgcStudyEtlCallable(Path stagingFileDirectory, String aUrl, IcgcFileTransformer transformer) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aUrl),
                "A URL to an ICGC file is required");
        Preconditions.checkArgument(null != transformer, "An IcgcFileTransformer implemntation is required");
        if (StagingUtils.isValidStagingDirectoryPath(stagingFileDirectory)) {
            this.service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(defaultThreadCount));
            this.stagingFilePath = stagingFileDirectory;
            this.sourceUrl = aUrl;
            this.transformer = transformer;
        }
    }

    @Override
    public String call() throws Exception {
        ListenableFuture<Path> icgcFetchFuture = service.submit(new ICGCStudyFetcher(this.sourceUrl, this.stagingFilePath));
        AsyncFunction<Path, Path> gzipToTsv = new AsyncFunction<Path, Path>() {
            @Override
            public ListenableFuture<Path> apply(Path aPath) throws Exception {
                return service.submit(new ICGCFileDecompressor(aPath));
            }
        };
        AsyncFunction<Path, Path> tsvToMaf = new AsyncFunction<Path, Path>() {

            @Override
            public ListenableFuture<Path> apply(Path tsvPath) throws Exception {
                transformer.setIcgcFilePath(tsvPath);
                return service.submit(transformer);
            }
        };

        ListenableFuture<Path> tsvFuture = Futures.transform(icgcFetchFuture, gzipToTsv);

        ListenableFuture<Path> mafFuture = Futures.transform(tsvFuture, tsvToMaf);

        Futures.addCallback(mafFuture, new FutureCallback<Path>() {
            @Override
            public void onSuccess(Path v) {
                logger.info("Success: File " + v.toString() + " created");
            }
            @Override
            public void onFailure(Throwable thrwbl) {
                logger.error(thrwbl.getMessage());
            }

        });
        try {
            // add maf path to list
            Path mafPath = mafFuture.get();
            logger.info("ICGC study data transformed to " + mafPath.toString());
            //mafPathList.add(mafPath);
        } catch (InterruptedException | ExecutionException ex) {
            logger.error(ex.getMessage());
        }
        return this.stagingFilePath.toString();
    }

    public void dispose() {
        service.shutdown();
    }
    /*
    private inner class definitions
     */
    /*
     private inner class responsible for decompressing compressed ICGC
     study file
     */

    private class ICGCFileDecompressor implements Callable<Path> {

        private final File compressedFile;

        public ICGCFileDecompressor(Path aFilePath) {
            this.compressedFile = aFilePath.toFile();
        }

        public Path call() throws Exception {
            logger.info("Compressed filename " + this.compressedFile);
            return this.gunzipIt();
        }

        private Path gunzipIt() {
            String tsvFilename = this.compressedFile.getAbsolutePath().replace(".gz", "");
            File tsvFile = new File(tsvFilename);
            byte[] buffer = new byte[1024];

            try {
                FileOutputStream out;
                try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(compressedFile))) {
                    out = new FileOutputStream(tsvFile);
                    int len;
                    while ((len = gzis.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
                out.close();
                logger.info("TSV file = " + tsvFilename);

            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
            return tsvFile.toPath();
        }
    }

    /*
    private inner class for fetching a compressed ICGC study file based
    on a supplied ICGC URL
    */
    private class ICGCStudyFetcher implements Callable<Path> {

        private final String icgcStudyUrl;
        private Path compressedFilePath;

        public ICGCStudyFetcher(final String aUrl, final Path aDirPath) {
            this.icgcStudyUrl = aUrl;
            try {
                this.compressedFilePath = this.resolveCompressedFilePath(aDirPath);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }

        /*
         private method to resolve complete path for compressed output file from
         ICGC url and desitination path
         */
        private Path resolveCompressedFilePath(Path aBasePath) throws IOException {
            //the file name is the last part of the url minus the gz extension
            List<String> urlParts = StagingCommonNames.pathSplitter.splitToList(this.icgcStudyUrl);
            Path outPath = Paths.get(StagingCommonNames.pathJoiner
                    .join(aBasePath.toString(), urlParts.get(urlParts.size() - 1)));
            if (Files.deleteIfExists(outPath)) {
                logger.warn("Existing compressed  file " + this.compressedFilePath + " has been deleted");
            }
            logger.info("Compressed file for " + this.icgcStudyUrl + " is " + outPath.toString());
            return Files.createFile(outPath);
        }

        @Override
        public Path call() throws Exception {
            try {
                URL url = new URL(this.icgcStudyUrl);
                FileUtils.copyURLToFile(url, this.compressedFilePath.toFile());
                logger.info("File " + compressedFilePath.toString() + " created");

            } catch (IOException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
            return this.compressedFilePath;
        }
    }

    public static void main (String...args)  {
        final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(4));
       List<String> testUrlList = Lists.newArrayList("https://dcc.icgc.org/api/v1/download?fn=/current/Projects/BLCA-CN/simple_somatic_mutation.open.BLCA-CN.tsv.gz",
               "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/BOCA-UK/simple_somatic_mutation.open.BOCA-UK.tsv.gz",
               " https://dcc.icgc.org/api/v1/download?fn=/current/Projects/BRCA-UK/simple_somatic_mutation.open.BRCA-UK.tsv.gz",
               "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/PAEN-AU/simple_somatic_mutation.open.PAEN-AU.tsv.gz",
               "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/RECA-EU/simple_somatic_mutation.open.RECA-EU.tsv.gz"
               );
        Path icgcPath1 = Paths.get("/tmp/icgc1");
        Path icgcPath2 = Paths.get("/tmp/icgc2");
        Path icgcPath3 = Paths.get("/tmp/icgc3");
        Path icgcPath4 = Paths.get("/tmp/icgc4");
        Path icgcPath5 = Paths.get("/tmp/icgc5");

        boolean processing = true;
        IcgcFileTransformer transformer1 = (IcgcFileTransformer) new SimpleSomaticFileTransformer(new MutationFileHandlerImpl(), icgcPath1);
        IcgcFileTransformer transformer2 = (IcgcFileTransformer) new SimpleSomaticFileTransformer(new MutationFileHandlerImpl(), icgcPath2);
        IcgcFileTransformer transformer3 = (IcgcFileTransformer) new SimpleSomaticFileTransformer(new MutationFileHandlerImpl(), icgcPath3);
        IcgcFileTransformer transformer4 = (IcgcFileTransformer) new SimpleSomaticFileTransformer(new MutationFileHandlerImpl(), icgcPath4);
        IcgcFileTransformer transformer5 = (IcgcFileTransformer) new SimpleSomaticFileTransformer(new MutationFileHandlerImpl(), icgcPath5);



        IcgcStudyEtlCallable etl01 = new IcgcStudyEtlCallable(icgcPath1, testUrlList.get(2), transformer1);
        IcgcStudyEtlCallable etl02 = new IcgcStudyEtlCallable(icgcPath2, testUrlList.get(1), transformer2);
        IcgcStudyEtlCallable etl03 = new IcgcStudyEtlCallable(icgcPath3, testUrlList.get(0), transformer3);
        IcgcStudyEtlCallable etl04 = new IcgcStudyEtlCallable(icgcPath4, testUrlList.get(0), transformer4);
        IcgcStudyEtlCallable etl05 = new IcgcStudyEtlCallable(icgcPath5, testUrlList.get(0), transformer5);

        List<ListenableFuture<String>> futureList = Lists.newArrayList();
        futureList.add(service.submit(etl01));
        futureList.add(service.submit(etl02));
        futureList.add(service.submit(etl03));
        futureList.add(service.submit(etl04));
        futureList.add(service.submit(etl05));
       ListenableFuture<List<String>> etlResults = Futures.successfulAsList(futureList);
        Futures.addCallback(etlResults, new FutureCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> resultList) {
                for(String result: resultList){
                    logger.info(result);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error(t.getMessage());
            }
        });
        logger.info("futures submitted");
        while(!etlResults.isDone()){
            try {
                Thread.sleep(15000L);
                logger.info("Waiting.....");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        service.shutdown();
        logger.info("service shutdown");
        logger.info("FINIS...");
        return;

    }
}
