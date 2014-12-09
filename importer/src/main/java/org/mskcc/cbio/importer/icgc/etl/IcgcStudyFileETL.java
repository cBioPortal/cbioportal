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
package org.mskcc.cbio.importer.icgc.etl;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.internal.Lists;
import com.google.inject.internal.Preconditions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.support.IcgcImportService;
import org.mskcc.cbio.importer.icgc.support.IcgcStudyBaseUrlMapFromFileSupplier;
import org.mskcc.cbio.importer.icgc.transformer.SimpleSomaticFileTransformer;
import org.mskcc.cbio.importer.icgc.transformer.IcgcFileTransformer;

/*
 represents an component responsible for ICGC ETL operations for a specified 
set of ICGC projects and file types
Primary inputs consist of a list of URLs to download the requisite ICGC data,
a writeable output directory, and a data transformation component that implemets
the Callable interface
 */
public class IcgcStudyFileETL {

    private static Logger logger = Logger.getLogger(IcgcStudyFileETL.class);
    private static final Integer defaultThreadCount = 4;
    private static final Integer maxThreadCount = 8;

    ListeningExecutorService service;

    public IcgcStudyFileETL() {
        this.service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(defaultThreadCount));
    }

    public IcgcStudyFileETL(Integer threadCount) {
        Integer tc = (threadCount > 0 && threadCount <= maxThreadCount) ? threadCount : defaultThreadCount;
        this.service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(tc));
        logger.info("tread count set to " + tc);
    }

    public List<Path> processICGCStudies(List<String> urlList, Path destinationDirectory,
            IcgcFileTransformer transformer) {
        Preconditions.checkArgument(null != urlList && urlList.size() > 0,
                "A list of ICGC Study URLs is required");
        Preconditions.checkArgument(null != destinationDirectory,
                "A destination directory for ICGC study data is required");
        Preconditions.checkArgument(Files.isDirectory(destinationDirectory)
                && Files.isDirectory(destinationDirectory),
                "The specified directory " + destinationDirectory + " is invalid");
        Preconditions.checkArgument(null != transformer, 
                "An ICGC data transformer instance is required");
        

        return this.process(urlList, destinationDirectory, transformer);

    }
/**
 * private method to perform asynchronous ETL operations on a collection of
 * ICGC study files. The extraction operation is identical for all types of
 * study files. The transformation process requires a study-type specific 
 * IcgcFileTransformer implementation.
 * @param urlList
 * @param destinationDirectory
 * @param transformer
 * @return 
 */
    private List<Path> process(final List<String> urlList, final Path destinationDirectory,
           final IcgcFileTransformer transformer  ) {
        
       
        List<Path> mafPathList = Lists.newArrayList();
        
        for (String url : urlList) {
            logger.info("Processing url " +url +" to " + destinationDirectory.toString() 
            +" using transformer " +transformer.getClass().getName());
            
            ListenableFuture<Path> icgcFetchFuture = service.submit(new ICGCStudyFetcher(url, destinationDirectory));
            AsyncFunction<Path, Path> gzipToTsv = new AsyncFunction<Path, Path>() {
                @Override
                public ListenableFuture<Path> apply(Path aPath) throws Exception {
                    return service.submit(new ICGCFileDecompressor(aPath));
                }
            };
            AsyncFunction<Path,Path> tsvToMaf = new AsyncFunction<Path,Path>() {

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
                mafPathList.add(mafPath);
            } catch (InterruptedException | ExecutionException ex) {
                logger.error(ex.getMessage());
            }
        }

        return mafPathList;
    }

    public void dispose() {
        service.shutdown();
    }
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
            List<String> urlParts = IcgcImportService.pathSplitter.splitToList(this.icgcStudyUrl);
            Path outPath = Paths.get(IcgcImportService.pathJoiner
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

            } catch (IOException  e) {
               logger.error(e.getMessage());
            }
            return this.compressedFilePath;
        }
    }
    /*
     main method to facilitate standalone testing
     */

    public static void main(String... args) {
        IcgcStudyFileETL ex = new IcgcStudyFileETL(2);
        Supplier<Map<String, String>> supplier = Suppliers.memoize(new IcgcStudyBaseUrlMapFromFileSupplier());
        Map<String, String> urlMap = supplier.get();
        
         List<String> urlList = FluentIterable.from(Lists.newArrayList(urlMap.values()))
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String f) {
                        return f.replaceAll(IcgcImportService.INSTANCE.MUTATION_TYPE,
                                IcgcImportService.INSTANCE.SIMPLE_SOMATIC_MUTATION_TYPE);
                    }
                }).toList();
        Path p = Paths.get("/data/icgctest");
        
       
        List<Path> tsvPathList = ex.processICGCStudies(urlList, p, new SimpleSomaticFileTransformer());
        for (Path path : tsvPathList) {
            logger.info(path.toString());
        }
        ex.dispose();
    }

}
