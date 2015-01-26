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
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.model.SimpleSomaticModel;
import org.mskcc.cbio.importer.icgc.support.IcgcSimpleSomaticRecord;
import org.mskcc.cbio.importer.icgc.support.IcgcSimpleSomaticRecordFunnel;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationModel;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationTransformer;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SimpleSomaticFileTransformer extends MutationTransformer implements IcgcFileTransformer {

    private static final Logger logger = Logger.getLogger(SimpleSomaticFileTransformer.class);
    private static Boolean DELETE_EXISTING_FILE = true;

    private Path icgcFilePath;
    private BloomFilter<IcgcSimpleSomaticRecord> icgcRecordFilter;

    public SimpleSomaticFileTransformer(TsvStagingFileHandler aHandler, Path stagingFileDirectory) {
        super(aHandler);
        Preconditions.checkArgument(null != stagingFileDirectory,
                "A Path to a staging file directory is required");
        try {
            Files.createDirectories(stagingFileDirectory);
            aHandler.registerTsvStagingFile(stagingFileDirectory.resolve(StagingCommonNames.MUTATIONS_STAGING_FILENAME),
                    MutationModel.resolveColumnNames());
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public SimpleSomaticFileTransformer(Path stagingFileDirectory) {
        super(stagingFileDirectory.resolve(StagingCommonNames.MUTATIONS_STAGING_FILENAME),DELETE_EXISTING_FILE);

    }

    @Override
    public void setIcgcFilePath(final Path aPath) {
        if (StagingUtils.isValidInputFilePath(aPath)) {
            this.icgcFilePath = aPath;
            logger.info("Input path = " + this.icgcFilePath.toString());
        }
    }

    /*
    method to transform individual simple somatic mutation records.
    some icgc files are too large to transform as a single List of model objects
     */
    private void processSimpleSomaticModelData() {
        // maintain a collection of the last 200 valid MAF entries using an EvictingQueue to evaluate
        // possible false positives from the Bloom Filter
        final EvictingQueue<IcgcSimpleSomaticRecord> mafQueue = EvictingQueue.create(200);

        try (BufferedReader reader = Files.newBufferedReader(this.icgcFilePath, Charset.defaultCharset());) {
            // generate a new Bloom Filter
            this.icgcRecordFilter = BloomFilter.create(new IcgcSimpleSomaticRecordFunnel(), 5000000);
            final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
            /*
            process the uncompressed TSV file downloaded from ICGC
                -filter out duplicate records
                -transform the simple somatic ICGC attributes to a SimpleSomaticModel instance
                - write out the MAF file
            */
            logger.info("Staring transformation of " + this.icgcFilePath.toString());
            FluentIterable.from(parser)
                    .filter(new Predicate<CSVRecord>() {
                        @Override
                        public boolean apply(CSVRecord record) {
                            final IcgcSimpleSomaticRecord icgcRecord = new IcgcSimpleSomaticRecord(record.toMap());
                            return (!icgcRecordFilter.mightContain(icgcRecord)
                                    || !mafQueue.contains(icgcRecord));
                        }

                    })
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
                    .transform(new Function<CSVRecord, Integer>() {
                        @Override
                        public Integer apply(CSVRecord record) {
                            //debug

                            final Map<String, String> recordMap = record.toMap();
                            tsvFileHandler.transformImportDataToTsvStagingFile(Lists.newArrayList(new SimpleSomaticModel(recordMap)),
                                    MutationModel.getTransformationFunction());

                            return 1;
                        }
                    }).toList();

            logger.info("transformation complete ");
        } catch (Exception ex) {
            logger.error("++++Transformation error for  " +this.icgcFilePath.toString() );
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void processSimpleSomaticData() {
        // maintain a collection of the last 200 valid MAF entries using an EvictingQueue to evaluate
        // possible false positives from the Bloom Filter
        final EvictingQueue<IcgcSimpleSomaticRecord> mafQueue = EvictingQueue.create(200);

        try (BufferedReader reader = Files.newBufferedReader(this.icgcFilePath, Charset.defaultCharset());) {
            // generate a new Bloom Filter
            this.icgcRecordFilter = BloomFilter.create(new IcgcSimpleSomaticRecordFunnel(), 5000000);
            final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
            /*
            process the uncompressed TSV file downloaded from ICGC
                -filter out duplicate records
                -transform the simple somatic ICGC attributes to a SimpleSomaticModel instance
                - write out the MAF file
            */
            List<SimpleSomaticModel> somaticList = FluentIterable.from(parser)
                    .filter(new Predicate<CSVRecord>() {
                        @Override
                        public boolean apply(CSVRecord record) {
                            final IcgcSimpleSomaticRecord icgcRecord = new IcgcSimpleSomaticRecord(record.toMap());
                            return (!icgcRecordFilter.mightContain(icgcRecord)
                                    || !mafQueue.contains(icgcRecord));
                        }

                    })
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
                    .transform(new Function<CSVRecord, SimpleSomaticModel>() {
                        @Override
                        public SimpleSomaticModel apply(CSVRecord record) {
                            final Map<String, String> recordMap = record.toMap();
                            return new SimpleSomaticModel(recordMap);
                        }
                    }).toList();
            this.tsvFileHandler.transformImportDataToTsvStagingFile(somaticList, MutationModel.getTransformationFunction());
            logger.info("transformation complete ");
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public Path call() throws Exception {
        logger.info("Transformer invoked");
        //this.processSimpleSomaticData();
        this.processSimpleSomaticModelData();
        return this.icgcFilePath;
    }

    /*
     main method for standalone testing
     */
    public static void main(String... args) {
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
        Path tsvPath = Paths.get("/tmp/icgctest/BOCA-UK");
        try {
            if (!Files.exists(tsvPath)) {
                Files.createDirectories(tsvPath);

            }
            SimpleSomaticFileTransformer transformer = new SimpleSomaticFileTransformer(
                    tsvPath);
            //String fn = "/Users/criscuof/cbio-portal-data/icgc/pbca/icgc/au/simple_somatic_mutation.open.PBCA-DE.tsv";
            String fn = "/tmp/simple_somatic_mutation.open.BOCA-UK.tsv";
            transformer.setIcgcFilePath(Paths.get(fn));
            ListenableFuture<Path> p = service.submit(transformer);


            logger.info("Path " + p.get(30, TimeUnit.MINUTES));
            p.cancel(true);
            service.shutdown();
            logger.info("service shutdown ");
        } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        logger.info("FINIS");


    }


}
