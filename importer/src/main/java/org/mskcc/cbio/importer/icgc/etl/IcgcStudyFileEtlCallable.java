package org.mskcc.cbio.importer.icgc.etl;

import com.google.common.base.Optional;
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
public class IcgcStudyFileEtlCallable implements Callable<String> {
    private static Logger logger = Logger.getLogger(IcgcStudyFileEtlCallable.class);
    private ListeningExecutorService service;
    private static final Integer defaultThreadCount = 3;
    private Path stagingFilePath;
    private String sourceUrl;
    private IcgcFileTransformer transformer;
    /*
    this class is responsible for extracting a file from the ICGC data portal, decompressing it,
    transforming it to a staging data format, and outputting the transformed data to the
    specified staging path

    the constructor for this class requires
    1. a Path pointing to the icgc study-specific staging file directory
    2. a URL referencing the ICGC file to download and tramsform
    3. an IcgcFileTransformer implementation responsible for transforming the ICGC data
     */

    public IcgcStudyFileEtlCallable(Path stagingFileDirectory, String aUrl, IcgcFileTransformer transformer) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aUrl),
                "A URL to an ICGC file is required");
        Preconditions.checkArgument(null != transformer, "An IcgcFileTransformer implementation is required");
        if (StagingUtils.isValidStagingDirectoryPath(stagingFileDirectory)) {
            this.service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(defaultThreadCount));
            this.stagingFilePath = stagingFileDirectory;
            this.sourceUrl = aUrl;
            this.transformer = transformer;
        }
    }

    @Override
    public String call() throws Exception {
        ListenableFuture<Optional<Path>> icgcFetchFuture = service.submit(new ICGCStudyFetcher(this.sourceUrl,
                this.stagingFilePath));

        // function takes the Optional<Path> from the URL fetch and decompresses the
        // referenced file
        AsyncFunction<Optional<Path>, Path> gzipToTsv = new AsyncFunction<Optional<Path>, Path>() {
            @Override
            public ListenableFuture<Path> apply(Optional<Path> aPath) throws Exception {
                return service.submit(new ICGCFileDecompressor(aPath));
            }
        };
        // function sets the Path to the decompressed ICGC file in the transformer
        // then invokes the transformer
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
                logger.info("Success:  TSV File " + v.toString() + " transformed");
            }
            @Override
            public void onFailure(Throwable thrwbl) {
                logger.error(thrwbl.getMessage());
            }

        });
        try {
            Path mafPath = mafFuture.get();
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
     An Optional is used to deal with an incomplete file transfer from ICGC
     */

    private class ICGCFileDecompressor implements Callable<Path> {

        private final File compressedFile;

        public ICGCFileDecompressor(Optional<Path> aFilePathOptional) {
            if (aFilePathOptional.isPresent()){
                this.compressedFile = aFilePathOptional.get().toFile();
            } else {
                this.compressedFile = null;
            }
        }

        public Path call() throws Exception {
            return this.gunzipIt();
        }

        /*
        private method to decompress a gzip file
        the new file has the same name minus the .gz extension (file.txt.gz -> file.txt)
        returns a Path to the decompressed file
         */
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
            } catch (IOException ex) {
                logger.error(ex.getMessage());
                ex.printStackTrace();
            }
            return tsvFile.toPath();
        }
    }

    /*
    private inner class for fetching a compressed ICGC study file based
    on a supplied ICGC URL
    */
    private class ICGCStudyFetcher implements Callable<Optional<Path>> {

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

        /*
        public method to transfer a copy of a specified ICGC file
        disrupted transfers are retried a specified number of times
        An Optional is used to indicate that a transfer operation may
        not be successful
         */
        @Override
        public Optional<Path> call() throws Exception {
            int retryCount = 0;
            while (retryCount < 3) {
                try {
                    URL url = new URL(this.icgcStudyUrl);
                    FileUtils.copyURLToFile(url, this.compressedFilePath.toFile());
                    logger.info("File " + compressedFilePath.toString() + " created");
                    // successful transfer - outta here
                    return Optional.of(this.compressedFilePath);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    retryCount++;
                    logger.info("Failed transfer for " +this.icgcStudyUrl +" retry count = " +retryCount);
                }
            }
            logger.error("Failed to obtain file " +this.icgcStudyUrl);
            return Optional.absent();
        }
    }

    /*
    main method for stand alone testing
     */

    public static void main (String...args)  {
        final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(4));
       List<String> testUrlList = Lists.newArrayList("https://dcc.icgc.org/api/v1/download?fn=/current/Projects/ESAD-UK/simple_somatic_mutation.open.ESAD-UK.tsv.gz"
               );
        Path icgcPath1 = Paths.get("/tmp/icgctest");

        if (!Files.exists(icgcPath1)) {
            try {
                Files.createDirectories(icgcPath1);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        boolean processing = true;
        //IcgcFileTransformer transformer1 = (IcgcFileTransformer) new SimpleSomaticFileTransformer(new MutationFileHandlerImpl(), icgcPath1);
        IcgcFileTransformer transformer1 = (IcgcFileTransformer) new SimpleSomaticFileTransformer( icgcPath1);
        IcgcStudyFileEtlCallable etl01 = new IcgcStudyFileEtlCallable(icgcPath1, testUrlList.get(0), transformer1);
        List<ListenableFuture<String>> futureList = Lists.newArrayList();
        futureList.add(service.submit(etl01));

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
                Thread.sleep(60000L);
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
