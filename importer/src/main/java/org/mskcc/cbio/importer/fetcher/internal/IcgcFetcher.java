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
package org.mskcc.cbio.importer.fetcher.internal;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.gdata.util.common.base.Preconditions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.icgc.support.IcgcCancerStudyCliinicalSampleUrlSupplier;
import org.mskcc.cbio.icgc.support.IcgcSimpleSomaticCancerStudyUrlSupplier;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.FileTransformer;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.transformer.ICGCSimpleSomaticFileTransformer;

/*
 * responsible for fetching non-US ICGC cancer study files
decompressing them, and invoking transformation from tsv formatted files into 
MAF formatted files
 */
public class IcgcFetcher implements Fetcher {

    private final FileTransformer transformer;
    private Supplier<List<String>> urlSupplier;
    private static final Splitter dotSplitter = Splitter.on('.');
    private static final Logger logger = Logger.getLogger(IcgcFetcher.class);
    private static final String simpleSomaticMutationType = "simple_somatic_mutation.open";

    /*
    constructor for production use in importer framework
    
    */
    
    public IcgcFetcher (Config aConfig, FileTransformer aTransformer){
        this(aTransformer);
        Preconditions.checkArgument(null != aConfig ,
                " A org.mskcc.cbio.importer.Config implementation is required ");
        
    }
    public IcgcFetcher(FileTransformer aTransformer) {
        Preconditions.checkArgument(null != aTransformer, "A FileTransformer implementation is required");
        this.transformer = aTransformer;
    }
    
    
    /*
    interface method that will obtain the current data from the ICGC repository
    the initial request is for all the clinical sample file for all non-US
    ICGC cancer studies
    subsequent requests are for supported mutation types. 
    */
     @Override
    public void fetch(String dataSource, String desiredRunDate) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dataSource), 
                "A file containing a list of ICGC cancer studies is required");
        this.urlSupplier = Suppliers.memoize(new IcgcCancerStudyCliinicalSampleUrlSupplier(dataSource));
        for (String urlName : urlSupplier.get()) {
            System.out.println("Getting ICGC clinicaldata from " + urlName);
            URL url = new URL(urlName);
            File compressedFile = this.resolveCompressedFile(urlName);
            FileUtils.copyURLToFile(url, compressedFile);
            File tsvFile = this.resolveTsvFile(urlName);
            // uncompress file
            this.gunzipIt(compressedFile, tsvFile);
            // for each cancer study fetch registered mutation types

        }
    }

    private void fetchAndTransformSimpleSomaticMutationData(List<String> icgcUrlList) throws Exception {
       
        for (String urlName : this.convertIcgcUrlByMutationType(icgcUrlList, LATEST_RUN_INDICATOR)) {
            System.out.println("Getting simple somatic ICGC data from " + urlName);
            URL url = new URL(urlName);
            File compressedFile = this.resolveCompressedFile(urlName);
            FileUtils.copyURLToFile(url, compressedFile);
            File tsvFile = this.resolveTsvFile(urlName);
            // uncompress file
            this.gunzipIt(compressedFile, tsvFile);
            this.transformer.transform(Paths.get(tsvFile.getAbsolutePath()));
            // retain the compressed file for verification if necessary
            // delete the uncompressed file to save space
            Files.delete(tsvFile.toPath());

        }
    }
    
    
    private List<String> convertIcgcUrlByMutationType(List<String> clinicalSampleUrlList, final String mutationType){
        return FluentIterable.from(clinicalSampleUrlList)
                    .transform(new Function<String, String>() {

                        public String apply(String url) {
                            return url.replaceAll("clinicalsample", mutationType);
                        }

                    }).toList();

    }

    private File resolveCompressedFile(String url) {
        List<String> pathList = dotSplitter.splitToList(url);
        return new File("/tmp/" + pathList.get(pathList.size() - 3) + ".tsv.gz");
    }
    
    private File resolveTsvFile(String url){
        List<String> pathList = dotSplitter.splitToList(url);
        return new File("/tmp/" + pathList.get(pathList.size() - 3) + ".tsv");
    }

    @Override
    public void fetchReferenceData(ReferenceMetadata referenceMetadata) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void gunzipIt(File compressedFile, File tsvFile) {

        byte[] buffer = new byte[1024];

        try {
            GZIPInputStream gzis
                    = new GZIPInputStream(new FileInputStream(compressedFile));
            FileOutputStream out
                    = new FileOutputStream(tsvFile);
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    /*
    main method for testing as application
    */
    public static void main (String...args) {
        String defaultSource = "/tmp/ICGC_SimpleSomatic_Studies.txt";
        String dataSource = (args.length > 0)?args[0]: defaultSource;
        logger.info("Source for ICGC study names: " +dataSource);
        IcgcFetcher fetcher = new IcgcFetcher(new ICGCSimpleSomaticFileTransformer());
        try {
            fetcher.fetch(dataSource,null);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

}
