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
package org.mskcc.cbio.importer.transformer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.icgc.support.ICGCRecord;
import org.mskcc.cbio.icgc.support.ICGCRecordFunnel;
import org.mskcc.cbio.icgc.support.MafAttributeSupplier;
import org.mskcc.cbio.icgc.support.MafAttributeToIcgcAttributeMapSupplier;
import org.mskcc.cbio.icgc.support.MafTransformationFunctionMapSupplier;
import org.mskcc.cbio.importer.FileTransformer;
import scala.Tuple2;

public class ICGCSimpleSomaticFileTransformer implements FileTransformer {

    private static final Logger logger = Logger.getLogger(ICGCSimpleSomaticFileTransformer.class);
    private static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
    private Path icgcFilePath;
    private final String mafExtension = "maf";
    private Path mafFilePath;
    private Path dupFilePath;
    private BloomFilter<ICGCRecord> icgcRecordFilter ;
    /*
     the Suppliers respresent single item caches for data structures that see
     significant reuse
     */
    private Supplier<List<String>> mafAttributeSupplier = Suppliers.memoize(new MafAttributeSupplier());
    private Supplier< Map<String, Tuple2<String, Optional<String>>>> mafAtttributetoIcscAttributeSupplier
            = Suppliers.memoize(new MafAttributeToIcgcAttributeMapSupplier());
    private Supplier< Map< String, Function<Tuple2<String, Optional<String>>, String>>> mafTransformationFunctionMapSupplier
            = Suppliers.memoize(new MafTransformationFunctionMapSupplier());

    public ICGCSimpleSomaticFileTransformer() {
    }

    @Override
    public void transform(Path aPath) throws IOException {
        Preconditions.checkArgument(null != aPath, "A Path to an ICGC Cancer study file is required");
        Preconditions.checkArgument(Files.exists(aPath, LinkOption.NOFOLLOW_LINKS), aPath + " is not a file");
        Preconditions.checkArgument(Files.isReadable(aPath), aPath + " is not readable");
        this.icgcFilePath = aPath;
        System.out.println("Input path = " + this.icgcFilePath.toString());
        this.mafFilePath = this.generateMafFile();
        System.out.println("Output path = " + this.mafFilePath.toString());
        this.dupFilePath = this.generateDuplicateFile();
        this.transformICGCData();

    }

    private void generateMetaData(Path igcgPath) {

    }
    /*
     private method to read each line in a ICGC file and invoke its transformation
     to a new MAF record
     */

    private void transformICGCData() {

        try (BufferedReader reader = Files.newBufferedReader(this.icgcFilePath, Charset.defaultCharset());
                BufferedWriter writer = Files.newBufferedWriter(this.mafFilePath, Charset.defaultCharset());
                BufferedWriter dupWriter = Files.newBufferedWriter(this.dupFilePath, Charset.defaultCharset())) {
            // write the MAF column headers
            writer.append(tabJoiner.join(mafAttributeSupplier.get()));
            writer.newLine();
            // write duplicate record header
            dupWriter.append(ICGCRecord.getColumnNamesTSV());
            dupWriter.newLine();
            // generate a new Bloom Filter
            this.icgcRecordFilter = BloomFilter.create(ICGCRecordFunnel.INSTANCE, 5000);
            final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
            int tsvCount = 0;
            int mafCount = 0;
            int dupCount = 0;
            for (CSVRecord record : parser) {
                tsvCount++;
                Map<String, String> recordMap = record.toMap();
                // confirm that this ICGC record is not a duplicate of a previous one
                ICGCRecord icgcRecord = new ICGCRecord(recordMap);
                if(!this.icgcRecordFilter.mightContain(icgcRecord)) {
                    this.icgcRecordFilter.put(icgcRecord);
                    this.processIcgcRecord(recordMap, writer);
                    mafCount++;
                } else {
                   dupWriter.append(icgcRecord.toTSV());
                   dupWriter.newLine();
                   dupCount++;
                }
                    
            }
            // output line counts
          logger.info("Line counts for " +this.icgcFilePath +" tsv= " +tsvCount +"  maf = "
            +mafCount +" dup = " +dupCount);
          
            writer.flush();
            dupWriter.flush();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
    /*
     extract attributes from the ICGC record, invoke the appropriate transformation
     function, and add the new attribute to the MAF record
     10September2014 - refactor to use Tuple2 data structure for ICGC column names and functions
     this is to support transformations that require 2 ICGC parameters
    
     */

    private void processIcgcRecord(Map<String, String> recordMap, BufferedWriter writer) throws IOException {
      List<String> mafValueList =Lists.newArrayList();
        // the icgc attribute(s) needed to generate a maf value
        Map<String, Tuple2<String, Optional<String>>> mafToIcscMap = mafAtttributetoIcscAttributeSupplier.get();
        // the function used to transform the icgc attribute(s) to a maf value
        Map< String, Function<Tuple2<String, Optional<String>>, String>> functionMap = mafTransformationFunctionMapSupplier.get();

        
        for (String mafAttribute : mafAttributeSupplier.get()) {
            Tuple2<String, Optional<String>> icgcAttributeNames = mafToIcscMap.get(mafAttribute);
            String attributeValue1 = recordMap.get(icgcAttributeNames._1);
            String attributeValue2 = (icgcAttributeNames._2.isPresent()) ? recordMap.get(icgcAttributeNames._2.get()) : "";

            // pass in the icgc attributes to the function as a Tuple2
            String mafValue = functionMap.get(mafAttribute).apply(new Tuple2(attributeValue1, Optional.of(attributeValue2)));

            mafValueList.add(mafValue);
            
        }
        // write out MAF record
        writer.append(tabJoiner.join(mafValueList));
       writer.newLine();

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
    
    private Path generateDuplicateFile() {
         String icscExtension = com.google.common.io.Files.getFileExtension(this.icgcFilePath.toString());
         String dupFileName = this.icgcFilePath.toString().replace(icscExtension, "dup.tsv");
         System.out.println("duplicates found in " +this.icgcFilePath.toString() + " will be saved in " + dupFileName);
        return Paths.get(dupFileName);
    }

    @Override
    public String getPrimaryIdentifier() {
        throw new UnsupportedOperationException("Not supported for this Transformer implementation.");
    }

    @Override
    public Integer getPrimaryEntityCount() {
        throw new UnsupportedOperationException("Not supported for this Transformer implementation..");
    }

    /*
     main method to support standalone testing
     */
    public static void main(String... args) {
        ICGCSimpleSomaticFileTransformer transformer = new ICGCSimpleSomaticFileTransformer();
        try {
            transformer.transform(Paths.get("/tmp/BRCA-UK.tsv"));
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

}
