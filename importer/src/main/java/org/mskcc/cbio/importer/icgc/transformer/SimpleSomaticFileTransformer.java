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
package org.mskcc.cbio.importer.icgc.transformer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gdata.util.common.base.Preconditions;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.support.IcgcSimpleSomaticRecord;
import org.mskcc.cbio.importer.icgc.support.IcgcSimpleSomaticRecordFunnel;
import org.mskcc.cbio.importer.icgc.support.SimpleSomaticTransformationMapSupplier;
import scala.Tuple2;
import scala.Tuple3;

public class SimpleSomaticFileTransformer implements IcgcFileTransformer {

    private static final Logger logger = Logger.getLogger(SimpleSomaticFileTransformer.class);
    private static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
    private Path icgcFilePath;
    private final String mafExtension = "maf";
    private Path mafFilePath;
   
    private BloomFilter<IcgcSimpleSomaticRecord> icgcRecordFilter;
    /*
     the Suppliers respresent single item caches for data structures that see
     significant reuse
     */

    private final Supplier<Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>>> simplesomaticTransformationMaprSupplier
            = Suppliers.memoize(new SimpleSomaticTransformationMapSupplier());

    public SimpleSomaticFileTransformer() {

    }

    @Override
    public Path call() throws Exception {
        this.filterAndTransformSimpleSomaticData();
        return this.mafFilePath;
    }

    @Override
    public void setIcgcFilePath(final Path aPath) {
        Preconditions.checkArgument(null != aPath, "A Path to an ICGC Cancer study file is required");
        Preconditions.checkArgument(Files.exists(aPath, LinkOption.NOFOLLOW_LINKS), aPath + " is not a file");
        Preconditions.checkArgument(Files.isReadable(aPath), aPath + " is not readable");
        this.icgcFilePath = aPath;
        logger.info("Input path = " + this.icgcFilePath.toString());
        this.mafFilePath = this.generateMafFile();
        logger.info("Output path = " + this.mafFilePath.toString());
       
    }

    private void generateMetaData(Path icgcPath) {
    }
    /*
     private method to generate an ordered list of MAF file attributes also 
     used as report column names
     numeric prefixes used for ordering the key set are removed
     */

    private List<String> getMafFileAttributeList() {
        return FluentIterable.from(this.simplesomaticTransformationMaprSupplier.get().keySet())
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return s.substring(2);
                    }
                }).toList();
    }
    /*
     private method to read each line in a ICGC file and invoke its transformation
     to a new MAF record
     */

    private void filterAndTransformSimpleSomaticData() {
        // maintain a collection of the last 200 valid MAF entries using an EvictingQueue to evaluate
        // possible flase positives from the Bloom Filter
        final EvictingQueue<IcgcSimpleSomaticRecord> mafQueue = EvictingQueue.create(200);
        final Set<String> somaticAttributeSet = this.simplesomaticTransformationMaprSupplier.get().keySet();
        final Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>> transformationMap
                = this.simplesomaticTransformationMaprSupplier.get();
        try (BufferedReader reader = Files.newBufferedReader(this.icgcFilePath, Charset.defaultCharset());
                BufferedWriter writer = Files.newBufferedWriter(this.mafFilePath, Charset.defaultCharset());) {

            // write the MAF file column headers
            writer.append(tabJoiner.join(this.getMafFileAttributeList()));
            writer.newLine();
            
            // generate a new Bloom Filter
            this.icgcRecordFilter = BloomFilter.create(IcgcSimpleSomaticRecordFunnel.INSTANCE, 5000000);
            final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
            /*
            process the uncompressed TSV file downlaoded from ICGC
                -filter out duplicate records
                -transform the simple somatic ICGC attributes to MAF records
                - write out the MAF file
            */
            
            List<String> somaticList = FluentIterable.from(parser)
                    .filter(new Predicate<CSVRecord>() {
                        @Override
                        public boolean apply(CSVRecord record) {
                            final IcgcSimpleSomaticRecord icgcRecord = new IcgcSimpleSomaticRecord(record.toMap());
                            return (!icgcRecordFilter.mightContain(icgcRecord)
                            || !mafQueue.contains(icgcRecord));
                        }

                    })
                    // 
                    // use a filter to update the BloomFilter and maf queue contents
                    .filter(new Predicate<CSVRecord>() {
                        @Override
                        public boolean apply(CSVRecord record) {
                            final IcgcSimpleSomaticRecord icgcRecord = new IcgcSimpleSomaticRecord(record.toMap());
                            mafQueue.add(icgcRecord);
                            icgcRecordFilter.put(icgcRecord);
                            return true;
                        }
                    })
                    .transform(new Function<CSVRecord, String>() {

                        @Override
                        public String apply(CSVRecord record) {
                            final Map<String, String> recordMap = record.toMap();
                            List<String> mafAttributes = FluentIterable.from(somaticAttributeSet)
                            .transform(new Function<String, String>() {
                                @Override
                                public String apply(String somaticAttribute) {
                                    Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>> somaticAttributeTransformationTuple
                                    = transformationMap.get(somaticAttribute);
                                    String attribute1Value = recordMap.get(somaticAttributeTransformationTuple._2());  // resolve the first function argument

                                    Optional optionalAttribute2Value = (somaticAttributeTransformationTuple._3().isPresent())
                                            ? Optional.of(recordMap.get(somaticAttributeTransformationTuple._3().get())) : Optional.absent();
                                    // invoke the function encapsulated within the tuple
                                    return somaticAttributeTransformationTuple._1().apply(new Tuple2(attribute1Value, optionalAttribute2Value));
                                }
                            }).toList();

                            String retRecord = tabJoiner.join(mafAttributes);
                            // a functional programming no no
                            // but it avoids a redundant loop
                            try {
                                writer.append(retRecord);
                                writer.newLine();
                            } catch (IOException ex) {
                                logger.error(ex.getMessage());
                            }
                            return retRecord;
                        }
                    }).toList();

            writer.flush();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }

}

    /**
     * private method to generate a new file for the MAF data whose name is
     * based on the name of the ICGC file
     *
     * @return
     */
    private Path generateMafFile() {
        String icscExtension = com.google.common.io.Files.getFileExtension(this.icgcFilePath.toString());
        String mafFileName = this.icgcFilePath.toString().replace(icscExtension, mafExtension);
        System.out.println(this.icgcFilePath.toString() + " will be mapped to " + mafFileName);
        return Paths.get(mafFileName);
    }

    
    /*
     main method for standalone testing
     */
    public static void main(String... args) {
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
        SimpleSomaticFileTransformer transformer = new SimpleSomaticFileTransformer();
        String fn = "/tmp/BLCA-CN.tsv";
        transformer.setIcgcFilePath(Paths.get(fn));
        service.submit(transformer);
        try {
            Thread.sleep(60000); // shutdown after 1 minute
            service.shutdown();
            logger.info("Test completed");
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage());
        }

    }

}
