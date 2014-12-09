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
import com.google.common.collect.FluentIterable;
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
import org.mskcc.cbio.importer.icgc.support.ClinicalTransformationMapSupplier;
import scala.Tuple2;
import scala.Tuple3;

public class ClinicalDataFileTransformer implements IcgcFileTransformer {

    private static final Logger logger = Logger.getLogger(ClinicalDataFileTransformer.class);
    private static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
    private Path icgcFilePath;
    private final String txtExtension = ".txt";
    private Path txtFilePath;

    /*
     the Suppliers respresent single item caches for data structures that see
     significant reuse
     */
   
    private final Supplier<Map<String, 
        Tuple3<Function<Tuple2<String, Optional<String>>,String>,String,Optional<String>>>  > clinicalTransformationMapSupplier =
            Suppliers.memoize(new ClinicalTransformationMapSupplier());

    public ClinicalDataFileTransformer() {

    }

    @Override
    public Path call() throws Exception {
        Preconditions.checkState(null != this.icgcFilePath,
                "An ICGC clinical data file must be provided before initiating transformation");
        logger.info("Transformation of " + this.icgcFilePath.toString() + " has been initiated");
        this.transformIcgcClinicalData();
        return this.txtFilePath;
    }

    @Override
    public void setIcgcFilePath(final Path aPath) {
        Preconditions.checkArgument(null != aPath, "A Path to an ICGC Cancer clinical file is required");
        Preconditions.checkArgument(Files.exists(aPath, LinkOption.NOFOLLOW_LINKS), aPath + " is not a file");
        Preconditions.checkArgument(Files.isReadable(aPath), aPath + " is not readable");
        this.icgcFilePath = aPath;
        System.out.println("Input path = " + this.icgcFilePath.toString());
        this.txtFilePath = this.generateTxtFile();
        System.out.println("Output path = " + this.txtFilePath.toString());

    }

    private void generateMetaData(Path icgcPath) {

    }
    // predicate for filtering out control specimens
    // n.b. british spelling 
    Predicate controlSpecimenFilter = new Predicate<CSVRecord>() {
        public boolean apply(CSVRecord record) {
            return (record.get("specimen_type").contains("tumour"));
        }
    };

    /*
     private method to read each line in an ICGC clinical file and invoke its transformation
     to a new text file record
     */
    private void transformIcgcClinicalData() {

        try (BufferedReader reader = Files.newBufferedReader(this.icgcFilePath, Charset.defaultCharset());
                BufferedWriter writer = Files.newBufferedWriter(this.txtFilePath, Charset.defaultCharset());) {
            final Set<String> clinicalAttributeSet = this.clinicalTransformationMapSupplier.get().keySet();
            // to control the order in which the map keys are outputted, they are prefixed with a  2 digit numeric value
            // remove those numberic prefixes prior to output
            List<String> columnHeaderList = FluentIterable.from(this.clinicalTransformationMapSupplier.get().keySet())
                    .transform(new Function<String,String>() {
                @Override
                public String apply(String s) {
                    return s.substring(2);
                }
            })
                    .toList();
            // write the clinical column headers
             writer.append(tabJoiner.join(columnHeaderList));
            writer.newLine();
            final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
           
            // get the Map of clinical attributes to transformation functions and attributes
            final Map<String, Tuple3<Function<Tuple2<String, Optional<String>>,String>,String,Optional<String>>>  transformationMap = 
                    this.clinicalTransformationMapSupplier.get();
              
            // filter out control specimens  and transform the data to a List of tab-delimited Strings
            // function has a side effect of writing out the tab-delimited string to a file
            List<String> clinicalList = FluentIterable.from(parser)
                    .filter(controlSpecimenFilter)
                    .transform(new Function<CSVRecord, String>() {
                        @Override
                        public String apply(CSVRecord record) {
                            final Map<String, String> recordMap = record.toMap();
                            List<String> clinicalRecord = FluentIterable.from(clinicalAttributeSet)
                            .transform(new Function<String, String>() {
                                @Override
                                public String apply(String clinicalAttribute) {     
                                   Tuple3<Function<Tuple2<String, Optional<String>>,String>,String,Optional<String>> clinicalAttributeTransformationTuple = 
                                           transformationMap.get(clinicalAttribute);
                                   String attribute1Value = recordMap.get(clinicalAttributeTransformationTuple._2());  // resolve the first function argument
                                   
                                   Optional optionalAttribute2Value = (  clinicalAttributeTransformationTuple._3().isPresent()) ?
                                           Optional.of(recordMap.get(clinicalAttributeTransformationTuple._3().get())) : Optional.absent();
                                   // invoke the function encapsulated within the tuple
                                   return clinicalAttributeTransformationTuple._1().apply(new Tuple2(attribute1Value, optionalAttribute2Value));
                                   
                                }
                            }).toList();
                            String retRecord = tabJoiner.join(clinicalRecord);
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
     * private method to generate a new file for the clinical data whose name is
     * based on the name of the ICGC file
     *
     * @return
     */
    private Path generateTxtFile() {
        String icscExtension = com.google.common.io.Files.getFileExtension(this.icgcFilePath.toString());
        String txtFilename = this.icgcFilePath.toString().replace(icscExtension, txtExtension);
        System.out.println(this.icgcFilePath.toString() + " will be mapped to " + txtFilename);
        return Paths.get(txtFilename);
    }

    /*
     main method for standalone testing
     */
    public static void main(String... args) {
       ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
       ClinicalDataFileTransformer transformer =new ClinicalDataFileTransformer();
       String fn = "/tmp/clinical.PACA-CA.tsv";
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
