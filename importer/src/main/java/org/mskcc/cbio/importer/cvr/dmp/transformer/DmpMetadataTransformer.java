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
package org.mskcc.cbio.importer.cvr.dmp.transformer;

import com.google.common.base.*;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.gdata.util.common.base.Preconditions;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.dmp.model.DmpData;
import org.mskcc.cbio.importer.cvr.dmp.model.MetaData;
import org.mskcc.cbio.importer.cvr.dmp.model.Result;
import org.mskcc.cbio.importer.cvr.dmp.util.DMPCommonNames;
import org.mskcc.cbio.importer.cvr.dmp.util.DmpUtils;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.clinical.ClinicalDataFileHandler;
import scala.Tuple2;
import scala.Tuple3;

public class DmpMetadataTransformer implements DMPDataTransformable {

    
    private final ClinicalDataFileHandler fileHandler;
    private final static Logger logger = Logger.getLogger(DmpMetadataTransformer.class);
    private static final String clinicalDataFilename = "data_clinical.txt";

    private final Supplier<Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>>> transformationMaprSupplier
            = Suppliers.memoize(new DmpMetadataTransformationsSupplier());
    
    public DmpMetadataTransformer(ClinicalDataFileHandler aHandler, Path stagingDirectoryPath) {
        Preconditions.checkArgument(null != aHandler, "A MafFileHandler implementation is required");
        Preconditions.checkArgument(null != stagingDirectoryPath,
                "A Path to the staging file directory is required");
        Preconditions.checkArgument(Files.isDirectory(stagingDirectoryPath, LinkOption.NOFOLLOW_LINKS),
                "The specified Path: " + stagingDirectoryPath + " is not a directory");
        Preconditions.checkArgument(Files.isWritable(stagingDirectoryPath),
                "The specified Path: " + stagingDirectoryPath + " is not writable");
        this.fileHandler = aHandler;
        //register a new or existing data_clinical.txt file with the file handler implementation
        this.fileHandler.registerClinicalDataStagingFile(stagingDirectoryPath.resolve(clinicalDataFilename),
                this.resolveColumnNames());
    }
     /*
    private method to determine the column headings for the data_clinical.txt file
    */
     private List<String> resolveColumnNames() {
       return FluentIterable.from(this.transformationMaprSupplier.get().keySet())
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return (s.substring(3)); // strip off the three digit numeric prefix
                    }
                }).toList();
    }
     /*
     public method to transform and output the DMP metadata into a data_clinical.txt file
     */
    @Override
    public void transform(DmpData data) {
        Preconditions.checkArgument(null != data, "A DmpData object is required");     
        this.fileHandler.transformImportDataToStagingFile(data.getResults(), transformationFunction);
    }

    /*
    Function to transform a DMP result object to a tsv String for output
    */
    Function<Result,String> transformationFunction = new Function<Result,String>() {

        @Override
        public String apply(Result result) {
            final MetaData meta = result.getMetaData();
            Set<String> attributeList = transformationMaprSupplier.get().keySet();
                        List<String> mafAttributes = FluentIterable.from(attributeList)
                        .transform(new Function<String, String>() {
                            @Override
                            public String apply(String attribute) {
                                Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>> tuple3
                                = transformationMaprSupplier.get().get(attribute);
                                String attribute1 = DmpUtils.pojoStringGetter(tuple3._2(), meta);

                                Optional<String> optAttribute2 = (Optional<String>) ((tuple3._3().isPresent())
                                        ? Optional.of(DmpUtils.pojoStringGetter(tuple3._3().get(), meta))
                                        : Optional.absent());

                                return tuple3._1().apply(new Tuple2(attribute1, optAttribute2));
                            }
                        }).toList();
                        String retRecord = StagingCommonNames.tabJoiner.join(mafAttributes);
                        return retRecord;
        }
    };
   
    
    /*
    Private class that encapsulates the individual DMP metadata attribute to clinical data attribute 
    transformation
    Currently up to two DMP metadata attributes can be used to resolve a 
    clinical data attribute
    The keys in the transformation map have a numeric prefix to control their ordering within 
    key sets from the tree map. Otherwise they would be returned in alphabetical order.
    */
    private class DmpMetadataTransformationsSupplier implements
            Supplier<Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>>> {

        private final Optional<String> absent = Optional.absent();

        @Override
        public Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>> get() {
            Map<String, Tuple3<Function<Tuple2<String, Optional<String>>, String>, String, Optional<String>>> transformationMap = Maps.newTreeMap();
            transformationMap.put("001SAMPLE_ID", new Tuple3<>(copyAttribute, "getDmpSampleId", absent)); //1
            transformationMap.put("002PATIENT_ID", new Tuple3<>(copyAttribute, "getDmpPatientId", absent)); //2
            transformationMap.put("003CANCER_TYPE", new Tuple3<>(copyAttribute, "getTumorTypeName", absent)); //3
            transformationMap.put("004SAMPLE_TYPE", new Tuple3<>(resolveSampleType, "getIsMetastasis", absent)); //4
            transformationMap.put("005SAMPLE_CLASS", new Tuple3<>(getSampleClass, "getDmpSampleId", absent)); //5
            transformationMap.put("006METASTATIC_SITE", new Tuple3<>(resolveMetastaticSite, "getIsMetastasis", Optional.of("getMetastasisSite")));
            transformationMap.put("007PRIMARY_SITE", new Tuple3<>(unsupported, "getDmpSampleId", absent)); //7
            transformationMap.put("008CANCER_TYPE_DETAILED", new Tuple3<>(unsupported, "getDmpSampleId", absent)); //8
            transformationMap.put("009KNOWN_MOLECULAR_CLASSIFIER", new Tuple3<>(unsupported, "getDmpSampleId", absent)); //9

            return transformationMap;
        }

        Function<Tuple2<String, Optional<String>>, String> resolveMetastaticSite
                = new Function<Tuple2<String, Optional<String>>, String>() {

                    @Override
                    public String apply(Tuple2<String, Optional<String>> f) {
                        return (f._1().equalsIgnoreCase(DMPCommonNames.IS_METASTASTIC_SITE))
                                ? f._2().get() : "Not Applicable";
                    }
                };

        /*
         resolve the sample type based on the is_metastastic attribute value
         */
        Function<Tuple2<String, Optional<String>>, String> resolveSampleType
                = new Function<Tuple2<String, Optional<String>>, String>() {

                    @Override
                    public String apply(Tuple2<String, Optional<String>> f) {
                        return (f._1().equalsIgnoreCase(DMPCommonNames.IS_METASTASTIC_SITE))
                                ? "Metastasis" : "Primary";
                    }

                };

        Function<Tuple2<String, Optional<String>>, String> getSampleClass
                = new Function<Tuple2<String, Optional<String>>, String>() {

                    @Override
                    // for now return a default value
                    public String apply(Tuple2<String, Optional<String>> f) {
                        return DMPCommonNames.DEFAULT_SAMPLE_TYPE;

                    }
                };

        /*
         function to provide an empty string for unsupported MAF columns
         */
        Function<Tuple2<String, Optional<String>>, String> unsupported
                = new Function<Tuple2<String, Optional<String>>, String>() {

                    @Override
                    public String apply(Tuple2<String, Optional<String>> f) {
                        return "";
                    }

                };

        Function<Tuple2<String, Optional<String>>, String> copyAttribute
                = new Function<Tuple2<String, Optional<String>>, String>() {

                    @Override
                    /*
                     simple copy of ICGC attribute to MAF file
                     */
                    public String apply(Tuple2<String, Optional<String>> f) {
                        if (null != f._1) {
                            return f._1;
                        }
                        return "";

                    }

                };
    }

}
