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
package org.mskcc.cbio.importer.icgc.analytics;

import com.google.common.base.Strings;
import com.google.gdata.util.common.base.Preconditions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;

/*

 represents a Java application that can produce  variant classification summary 
 statitics for an ICGC based on gene and varaint type.
 the default value for variant type filter and variant classification is ALL
 
 */
public final class ICGCSummaryStatistics implements Serializable {

    private static final Logger logger = Logger.getLogger(ICGCSummaryStatistics.class);

    // Spark requires the following variables to be static
    private static SparkConf conf;
    private static JavaSparkContext ctx;

    public ICGCSummaryStatistics() {

        // initialize the Spark configuration (local)
        conf = new SparkConf().setAppName("ICGCSummaryStatistics")
                .setMaster("local")
                .set("spark.executor.memory", "4g")
                .set("spark.default.parallelism", "4");
        ctx = new JavaSparkContext(conf);

    }
    /*
     public method to summarize counts for variation classifications within
     a particular ICGC simple somatic mutation file
     */

    public Tuple2<String, Map<String, Integer>> summarizeVariationClassifications(String center, Path mafFilePath)
            throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(center),
                "An ICGC Center is required");
        Preconditions.checkArgument(null != mafFilePath,
                "A Path to a ICGC simple somatic mutation MAF file is required");
        Preconditions.checkArgument(mafFilePath.toFile().isFile()
                && mafFilePath.toFile().canRead(), "The specified MAF file: " + mafFilePath.toString() + " is not valid");
        JavaRDD<CSVRecord> records = this.getCSVRecords(mafFilePath);
        records.persist(StorageLevel.MEMORY_AND_DISK());

        JavaPairRDD<String, Integer> vcRecords
                = this.mapVariantClassifications(records);
        JavaPairRDD<String, Integer> tsbcRecords = this.mapTumorSampleBarcodes(records);
        JavaPairRDD<String, Integer> vcCounts
                = this.reduceVariantClassifications(vcRecords);
        Tuple2<String,Integer> sampleCount = this.determineSampleCounnt(tsbcRecords);
        logger.info(sampleCount._1() +" = " +sampleCount._2());
        return new Tuple2(center, vcCounts.collectAsMap());
    }

    private JavaRDD<CSVRecord> getCSVRecords(Path mafFilePath) {
        logger.info("Processing MAF file " + mafFilePath.toString());
        try (BufferedReader reader = Files.newBufferedReader(mafFilePath, Charset.defaultCharset())) {
            final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
            return ctx.parallelize(parser.getRecords());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;

    }

    private JavaPairRDD<String, Integer> mapVariantClassifications(JavaRDD<CSVRecord> records) {
        JavaPairRDD<String, Integer> vcRecords;
        vcRecords = records.mapToPair(new PairFunction<CSVRecord, String, Integer>() {

            public Tuple2<String, Integer> call(CSVRecord record) throws Exception {
                String vc = (!Strings.isNullOrEmpty(record.get("Variant_Classification")))
                        ? record.get("Variant_Classification") : "unknown";
                return new Tuple2(vc, 1);
            }
        });
        return vcRecords;
    }
    
     private JavaPairRDD<String, Integer> mapTumorSampleBarcodes(JavaRDD<CSVRecord> records) {
        JavaPairRDD<String, Integer> tsbcRecords;
        tsbcRecords = records.mapToPair(new PairFunction<CSVRecord, String, Integer>() {

            public Tuple2<String, Integer> call(CSVRecord record) throws Exception {
                String vc = (!Strings.isNullOrEmpty(record.get("Tumor_Sample_Barcode")))
                        ? record.get("Tumor_Sample_Barcode") : "unknown";
                return new Tuple2(vc, 1);
            }
        });
        return tsbcRecords;
    }

    private JavaPairRDD<String, Integer> reduceVariantClassifications(JavaPairRDD<String, Integer> vcRecords) {
        return vcRecords.reduceByKey(new Function2<Integer, Integer, Integer>() {
            // increment count for this variation classification
            @Override
            public Integer call(Integer t1, Integer t2) throws Exception {
                return t1 + t2;
            }
        });
    }
    
    private Tuple2<String,Integer> determineSampleCounnt( JavaPairRDD<String, Integer> tsbcRecords){
        JavaPairRDD<String, Integer>  uniqueTumorSampleBarcodes = tsbcRecords.reduceByKey(
            new Function2<Integer,Integer,Integer>() {
            @Override
            public Integer call(Integer t1, Integer t2) throws Exception {
                return t1 + t2;
            }
                
            });
        // all we need is the count in the reduced RDD
        Long count  = uniqueTumorSampleBarcodes.count();
        return new Tuple2("Samples", count.intValue());
    }

    public static void main(String... args) {
        try {

            ICGCSummaryStatistics test = new ICGCSummaryStatistics();
            Path maffile = Paths.get("/data/icgctest/maffiles/LIRI-JP.maf");
            Tuple2<String, Map<String, Integer>> stats = test.summarizeVariationClassifications("LIRI-JP", maffile);
            String center = stats._1();
            Integer variationTotal = 0;
            for (Map.Entry<String, Integer> entry : stats._2().entrySet()) {
                logger.info(entry.getKey() + "  " + entry.getValue());
                variationTotal += entry.getValue();
            }
            logger.info("Total variations = " + variationTotal);

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
