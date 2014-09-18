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
package org.mskcc.cbio.icgc.support;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gdata.util.common.base.Preconditions;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

/*

 represents a Java application that can produce  variant classification summary 
 statitics for an ICGC based on gene and varaint type.
 the default value for variant type filter and variant classification is ALL
 
 */
public final class ICGCSummaryStatistics implements Serializable {

    private static final String MAF_FILE_EXTENSION = ".maf";
    private static final String DEFAULT_FILTER_VALUE = "ALL";
    private static final Boolean DEFAULT_GENE_FLAG = true;
    private static final Logger logger = Logger.getLogger(ICGCSummaryStatistics.class);
    // tuple substitute for null
    private static final Tuple2<String, Integer> Tuple2Null = new Tuple2("null", 0);
    private static final Joiner scJoiner = Joiner.on(';').useForNull(" ");
    private static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
    // Spark requires the following variables to be static
    private static String variationClassification;
    private static String variationType;
    private static List<File> mafFileList;
    private static SparkConf conf;
    private static JavaSparkContext ctx;
    private static Boolean geneFlag;

    public ICGCSummaryStatistics(String dirName, String vc, String vt, Boolean gf) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dirName),
                "A directory containing MAF files is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(vt),
                "A variation type is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(vc),
                "A variation classification is required");
        variationClassification = vc;
        variationType = vt;
        mafFileList = this.resolveFileList(dirName);
        geneFlag = gf;
        // initialize the Spark configuration (local)

        conf = new SparkConf().setAppName("ICGCSummaryStatistics")
                .setMaster("local")
                .set("spark.executor.memory", "4g")
                .set("spark.default.parallelism", "4");
        ctx = new JavaSparkContext(conf);

    }

    private void processMafFiles() throws IOException {
        /*
         broadcast key attributes to all Spark nodes
         */
        final Broadcast<String> broadcastVariationClassification = ctx.broadcast(this.variationClassification);
        final Broadcast<String> broadcastVariationType = ctx.broadcast(this.variationType);
        /*
         process each maf file individually
         */

        for (File mafFile : this.mafFileList) {
            logger.info("Processing MAF file " + mafFile.getAbsolutePath());
            // resolve output file
            Path statsPath = resolveOutputPath(mafFile.getAbsolutePath());
            JavaRDD<String> records = ctx.parallelize(this.transformMafFile(mafFile));
            JavaPairRDD<String, Integer> genes;

            genes = records.mapToPair(new PairFunction<String, String, Integer>() {
                @Override
                public Tuple2<String, Integer> call(String record) throws Exception {
                    String[] columns = StringUtils.split(record, ';');
                    String center = columns[0];
                    String geneName = columns[1];
                    String mutClass = "unknown"; // the variation class may be missing
                     if (columns.length == 3) {
                        mutClass = columns[2];
                    }
                    String mutType = "UNK"; // the variation type may be missing
                    if (columns.length == 4) {
                        mutType = columns[3];
                    }
                    
                    String centerAndGeneAndClassAndType = tabJoiner.join(center, geneName, mutType, mutClass);
                    String varClass = (String) broadcastVariationClassification.value();
                    String varType = (String) broadcastVariationType.value();

                    // filter records based on classification and or type
                    if (filterMafEntry(mutClass, mutType, varClass, varType)) {
                        return new Tuple2<>(centerAndGeneAndClassAndType, 1);
                    }
                    return Tuple2Null;
                }

            });
            // filter out redundant RDD elements - empty tuple2
            JavaPairRDD<String, Integer> filteredGenes = genes.filter(
                    new Function<Tuple2<String, Integer>, Boolean>() {

                        @Override
                        public Boolean call(Tuple2<String, Integer> s) throws Exception {
                            int counter = s._2;
                            return counter > 0;
                        }
                    }
            );
            // total number of variations
            final Float variationCount = new Float(filteredGenes.count());
            // reduce by key and sum up the frequency count
            JavaPairRDD<String, Integer> counts
                    = filteredGenes.reduceByKey(new Function2<Integer, Integer, Integer>() {
                        @Override
                        public Integer call(Integer t1, Integer t2) throws Exception {
                            return t1 + t2;
                        }

                    });
            // format results
            JavaRDD<String> results = counts.map(new Function<Tuple2<String, Integer>, String>() {
                @Override
                public String call(Tuple2<String, Integer> tuple) throws Exception {
                    float percent = (Float.valueOf(tuple._2) / variationCount) * 100.0f;
                    return tabJoiner.join(tuple._1, tuple._2, percent);
                }
            });
            
          logger.info("++++++  Results count = " +results.count());
           
            // output the results
            try (
                    final BufferedWriter writer = Files.newBufferedWriter(statsPath, Charset.defaultCharset())) {
                List<Tuple2<String, Integer>> outputs = counts.collect();
                // write out column headers to tsv
                writer.append(tabJoiner.join("Center",  "Variant_type", "Variant_classification", "Variant_count", "Variant_percent"));
                writer.newLine();
                for (Tuple2<String, Integer> tuple : outputs) {
                    float percent = (Float.valueOf(tuple._2) / variationCount) * 100.0f;
                    writer.append(tabJoiner.join(tuple._1, tuple._2, percent));
                    writer.newLine();

                }
                
            }

        }
    }

    private Path resolveOutputPath(String mafFilename) {
        String statsFilename = mafFilename.replace(".maf", ".all.summary.tsv");
        if (geneFlag) {
            statsFilename = mafFilename.replace(".maf", ".bygene.summary.tsv");
        }
        logger.info("Summary statistics written to " + statsFilename);
        return Paths.get(statsFilename);
    }
    /*
     private method for logic to filter which MAF records are processed
     this code can be refactored to implement different filter logic
     */

    private boolean filterMafEntry(String colClass, String colVt, String varClass, String varType) {
        if (varType.equals(DEFAULT_FILTER_VALUE)) {
            return true;
        }
        return colVt.equals(varType);
    }

    /*
     private method to read in the MAF file and  transform the appropriate columns
     the following columns are selected: Hugo_Symbol, Variant_Classification, Variant_Type
     if the record does not contain a HugoSymbol, it's set to Unknown
     the indivual fields are separated by a semicolon
    
     */
    private List<String> transformMafFile(File mafFile) throws IOException {
        List<String> mafList = Lists.newArrayList();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(mafFile.getAbsolutePath()), Charset.defaultCharset())) {
            final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
            for (CSVRecord record : parser) {
                String hg = "ALL";
                if (DEFAULT_GENE_FLAG) {
                    hg = (Strings.isNullOrEmpty(record.get("Hugo_Symbol"))) ? "Unknown" : record.get("Hugo_Symbol");
                }
                mafList.add(scJoiner.join(record.get("Center"), hg, record.get("Variant_Classification"),
                        record.get("Variant_Type")));
            }
        }
        return mafList;
    }
    /*
     private method to return a list of MAF (.maf extentsion) from
     a specified directory
     */

    private List<File> resolveFileList(String dirName) {
        File dir = new File(dirName);
        return Arrays.asList(dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(MAF_FILE_EXTENSION);
            }
        }));
    }

    public static void main(String... args) {
        try {
            //Preconditions.checkArgument(args.length > 0,
            //        "Usage: java ICGCSummaryStatistics maf-file-directory [variant-classification variant-type gene-flag]");

            String mafDirectory = (null != args && args.length > 0) ? args[0] : "/tmp/maftest";

            String vc = (args.length > 1) ? args[1] : DEFAULT_FILTER_VALUE;
            String vt = (args.length > 2) ? args[2] : DEFAULT_FILTER_VALUE;
            Boolean gf = DEFAULT_GENE_FLAG;
            if (args.length > 3) {
                if (Lists.newArrayList("true", "false").contains(args[3])) {
                    gf = Boolean.valueOf(args[3]);
                }
            }

            logger.info("ICGCSummaryStatistics: dir = " + mafDirectory + " variant classificaton = "
                    + vc + " variation type = " + vt + " gene flag = " + gf);
            ICGCSummaryStatistics stats = new ICGCSummaryStatistics(mafDirectory, vc, vt, gf);
            stats.processMafFiles();
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
