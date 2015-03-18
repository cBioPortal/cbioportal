package org.mskcc.cbio.importer.cvr.dmp.util;
//$JAVA_HOME/bin/java  -cp $PORTAL_HOME/importer/target/cbioportal-importer.jar org.mskcc.cbio.importer.cvr.dmp.util.DmpStagingFileRefactoringUtility /data/cbio-data/msk-impact/mapping/idrefactor.properties
import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.stanford.nlp.io.FileSequentialCollection;
import edu.stanford.nlp.util.PropertiesUtils;
import edu.stanford.nlp.util.StringUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;
import org.mskcc.cbio.importer.util.CancerTypeResolver;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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
 * Created by criscuof on 2/17/15.
 *
 * Java application that will:
 * (1) read in all data_clinical staging files in a specified directory
 * (2) convert the existing DMP sample and patient ids to new sample and patient ids
 * (3) use the existing cancer_type_detailed value to determine a new cancer_type value
 * (4) append the legacy sample id and patient id as new columns in the data_clinical.txt file
 * (5) output the refactored data to a new staging file in a specified output directory using the original file name
 * eg.  /tmp/clinical/data_clinical_xyz.txt ---> refactor---> /tmp/clinical/new/data_clinical_xyz.txt
 *
 */
public class DmpStagingFileRefactoringUtility {
    private static final Logger logger = Logger.getLogger(DmpStagingFileRefactoringUtility.class);
    private static final String PATIENT_ID_COLUMN_NAME = "PATIENT_ID";
    private static final String CLINICAL_DATA_SAMPLE_ID_COLUMN_NAME = "SAMPLE_ID";
    private static final String TSV_DATA_SAMPLE_ID_COLUMN_NAME = "Tumor_Sample_Barcode";
    private static final String SV_DATA_SAMPLE_ID_COLUMN_NAME = "TumorId";
    private static final String CANCER_TYPE_COLUMN_NAME = "CANCER_TYPE";
    private static final String CANCER_TYPE_DETAILED_COLUMN_NAME = "CANCER_TYPE_DETAILED";
    private static final String OTHER_PATIENT_ID_COLUMN_NAME = "OTHER_PATIENT_ID";
    private static final String OTHER_SAMPLE_ID_COLUMN_NAME = "OTHER_SAMPLE_ID";
    private static final String MUTATIONS_COMMENT = "#sequenced samples: ";
    private static final String DATA_SOURCE_NAME = "crdb-clinical-data" ;

    private Map<String,String> patientIdMap;
    private Map<String,String> sampleIdMap;
    private final Path inPath;
    private final Path outPath;

    public DmpStagingFileRefactoringUtility(Path inDirPath, Path outDirPath, String pLookupFile,
                                            String sLookupFile) {
        Preconditions.checkArgument(null != inDirPath, "A Path to the source directory is required");
        Preconditions.checkArgument(null != outDirPath, "A Path to the output directory is required");
        this.inPath = inDirPath;
        this.outPath = outDirPath;
        this.patientIdMap = Suppliers.memoize(new LookupMapSupplier(pLookupFile)).get();
        this.sampleIdMap = Suppliers.memoize(new LookupMapSupplier(sLookupFile)).get();
    }

    // revise to use local patient id map
    private String refactorPatientId(String anId){
        if(this.patientIdMap.containsKey(anId)){
            return this.patientIdMap.get(anId);
        }
        return anId;

       // Optional<String> newPatientIdOpt = DmpLegacyIdResolver.INSTANCE.resolveNewPatientIdFromLegacyPatientId(anId);
       // if(newPatientIdOpt.isPresent()){
        //    return newPatientIdOpt.get();
       // }
        //logger.error("Legacy patient id " +anId +" was not found in the patient id map");
        //if the new id isn't in the map yest, retain the use of the existing one
       // return anId;
    }

    // revise to use local sample id map
    private String refactorSampleId (String anId){
        if(this.sampleIdMap.containsKey(anId)){
            return this.sampleIdMap.get(anId);
        }
        return anId;
       // Optional<String> newSampleIdOpt = DmpLegacyIdResolver.INSTANCE.resolveNewSampleIdFromLegacySampleId(anId);
       // if(newSampleIdOpt.isPresent()){
        //    return newSampleIdOpt.get();
        //}
        //logger.error("Legacy sample id " +anId + " was not found in the sample id map");
        //if the new id isn't in the map yest, retain the use of the existing one
        //return anId;
    }

    /*
    private method to refactor a legacy cancer type to an an oncotree value
    the input here is expected to be the current cancer_type_detailed value
     */
    private String refactorCancerType(String currentType, String aDetailedType) {
       Optional<String> typeOptional = CancerTypeResolver.INSTANCE.resolveCancerTypeByCancerDetailedType(aDetailedType);
        if(typeOptional.isPresent()){
            return typeOptional.get();
        }
        logger.info("Unable to find a cancer type for cancer_type_detailed " + aDetailedType
        +" retaining current type: " +currentType);
        return currentType;
    }

    /*
    Process all the staging files in the specified directory whose file name starts with data_
     */
    private void refactorFileFunction(){
        FileSequentialCollection fsc = new FileSequentialCollection(inPath.toFile(),
                StagingCommonNames.stagingFileExtension,false);
        Observable<File> fileObservable = Observable.from(fsc)
                // only retain data_clinical type files
                .filter(new Func1<File, Boolean>() {
                    @Override
                    public Boolean call(File file) {
                        return file.getName().startsWith("data_");
                    }
                });
        fileObservable.subscribe(new Subscriber<File>() {
            @Override
            public void onCompleted() {
                logger.info("All staging files processed");
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error(throwable.getMessage());
                throwable.printStackTrace();
            }

            @Override
            public void onNext(File file) {
                logger.info("Processing staging file " +file.getName());
                processStagingFile(file, outPath);
            }
        });
    }
    /*
    Private method to initialize a List of Strings for eventual output
    Responsible for generating the file header as the initial entry in the List
    In the case of a CNA file, the header is a list of DMP sample ids and they need to be refactored
    to new ids
    For the data_mutations_extended.txt file, a set of DMP sample ids is used to output a list of sample ids as a comment line
     */
    private List<String> initializeStagingFileOutputList(Map<String, Integer> headerMap,
                                                         boolean legacyColumnsFlag,
                                                         Set<String> sampleSet
                                                         ){
        final List<String> outList = Lists.newArrayList();
        final String header = StagingCommonNames.tabJoiner.join(headerMap.keySet());
        if(!sampleSet.isEmpty()) {
            StringBuilder sb = new StringBuilder(MUTATIONS_COMMENT);
            sb.append(StagingCommonNames.blankJoiner.join(sampleSet));
            outList.add(sb.toString());
            outList.add(header);
        }
        else if (legacyColumnsFlag) {
            outList.add(StagingCommonNames.tabJoiner.join(header, OTHER_SAMPLE_ID_COLUMN_NAME,
                    OTHER_PATIENT_ID_COLUMN_NAME));
        } else {
            // check if this is a CNA header (i.e. sample ids)
            if (header.contains("DMP")){
                List<String> sampleList = Lists.newArrayList();
                for (String sample :headerMap.keySet()){
                    sampleList.add(refactorSampleId(sample));
                }
                outList.add(StagingCommonNames.tabJoiner.join(sampleList));
            } else {
                outList.add(header);
            }
        }
        return outList;
    }

    /*
    Process a staging file. Create a header for the new staging file with two
    additional columns for the legacy sample and patient ids. For each line in the
    original file, refactor the legacy sample and patient ids to their new values and
    write out the rest of the remaining original columns as is. If the file is
    data_clinical.txt, append the legacy sample and patient ids as new columns.
    If the file contains columns for cancer_type and cancer_type_detail, refactor these values using the
    oncotree map
    Each processed file generates a refactored file with the same name in the specified output
    directory
     */
    private void processStagingFile(final File stagingFile, final Path out){
        try {
            final boolean legacyColumnsFlag = (stagingFile.getName().equals("data_clinical.txt"))?true:false;
            final boolean sampleSetRequired = (stagingFile.getName().equals("data_mutations_extended.txt"))?true:false;
            final Path outPath = out.resolve(stagingFile.getName());
            Reader reader = new FileReader(stagingFile);
            final CSVParser parser = new CSVParser(reader,
                    CSVFormat.TDF.withHeader().withAllowMissingColumnNames(true).withCommentMarker('#'));
            final Map<String, Integer> headerMap = parser.getHeaderMap();
            Set<String> sampleSet = Sets.newHashSet();
            if (sampleSetRequired) {
                sampleSet = this.refactorSampleSet(stagingFile);
            }
            final List<String> outList = this.initializeStagingFileOutputList(headerMap,legacyColumnsFlag,sampleSet);
            Observable<CSVRecord> recordObservable = Observable.from(parser.getRecords());
            recordObservable.subscribe(new Subscriber<CSVRecord>() {
                @Override
                public void onCompleted() {
                    // write out the file
                    try {
                        Files.write(outPath, outList, Charset.defaultCharset());
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }
                }
                @Override
                public void onError(Throwable throwable) {
                    logger.error(throwable.getMessage());
                    throwable.printStackTrace();
                }
                @Override
                public void onNext(final CSVRecord record) {
                    // process each column in the record in the file and refactor columns as necessary
                    String line = StagingCommonNames.tabJoiner.join(FluentIterable.from(headerMap.keySet())
                            .transform(new Function<String, String>() {
                                @Nullable
                                @Override
                                public String apply(String input) {
                                    if (input.equals(CLINICAL_DATA_SAMPLE_ID_COLUMN_NAME)) {
                                        return refactorSampleId(record.get(CLINICAL_DATA_SAMPLE_ID_COLUMN_NAME));
                                    }
                                    if (input.equals(PATIENT_ID_COLUMN_NAME)) {
                                        return refactorPatientId((record.get(PATIENT_ID_COLUMN_NAME)));
                                    }
                                    if (input.equals(TSV_DATA_SAMPLE_ID_COLUMN_NAME)) {
                                        return refactorSampleId(record.get(TSV_DATA_SAMPLE_ID_COLUMN_NAME));
                                    }
                                    if (input.equals(SV_DATA_SAMPLE_ID_COLUMN_NAME)) {
                                        return (refactorSampleId(record.get(SV_DATA_SAMPLE_ID_COLUMN_NAME)));
                                    }
                                    // refactor the cancer type column using the cancer type detailed value to
                                    // find a new cancer type from the oncotree worksheet
                                    if (input.equals(CANCER_TYPE_COLUMN_NAME)) {
                                        return refactorCancerType(record.get(CANCER_TYPE_COLUMN_NAME),
                                                record.get(CANCER_TYPE_DETAILED_COLUMN_NAME));
                                    }
                                    try {
                                        return record.get(input);
                                    } catch (IllegalArgumentException e) {
                                        logger.error(input);
                                        e.printStackTrace();
                                    }
                                    return "";
                                }
                            }).toList());

                    // add the legacy dmp sample and patient ids if this is the data_clinical.txt file
                    if (legacyColumnsFlag) {
                        String legacySampleId = record.get(CLINICAL_DATA_SAMPLE_ID_COLUMN_NAME);
                        String legacyPatientId = record.get(PATIENT_ID_COLUMN_NAME);
                        outList.add(StagingCommonNames.tabJoiner.join(line, legacySampleId, legacyPatientId));
                    } else {
                        outList.add(line);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
   private method to read in the first line of a specified TSV file and process the content as a list
   of legacy samples to be refactored. The new sample ids are returned as a Set
    */
    private Set<String> refactorSampleSet(File tsvFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(tsvFile) )){
            String comment = br.readLine();
            return getDmpSamples(comment);
        } catch (IOException e){
            logger.error(e.getMessage());
        }
        return Sets.newHashSet(); // return an empty Set if there is an error
    }

    private static Set<String> getDmpSamples(String sampleCommentLine){
        String line = (sampleCommentLine.indexOf(':')>0 ) ?
                sampleCommentLine.substring(sampleCommentLine.indexOf(':')+2)
                :sampleCommentLine;

        return FluentIterable.from(StagingCommonNames.blankSplitter.splitToList(line))
                .transform(new Function<String, String>() {
                    @Nullable
                    @Override
                    public String apply(String legacySampleId) {
                        Optional<String> idOpt = DmpLegacyIdResolver.INSTANCE.resolveNewSampleIdFromLegacySampleId(legacySampleId);
                        if (idOpt.isPresent()) {
                            return idOpt.get();
                        }
                        //logger.info("Unable to resolve legacy Sample ID: " + legacySampleId);
                        return legacySampleId;
                    }
                }).toSet();
    }

    public static void main (String...args) {
        // args[0] is the name of the full name of the properties files
        // default properties file is within the study's metadata directory
        UtilityPropertiesManager manager =
                new UtilityPropertiesManager(args);
        DmpStagingFileRefactoringUtility test = new DmpStagingFileRefactoringUtility
                (Paths.get(manager.sourcePath()),Paths.get( manager.outputPath()),
                        manager.patientIdLookupFile(), manager.sampleIdLookupFile());
        test.refactorFileFunction();
    }

    /*
    Static inner class to manage runtime properties for the utility
     */
    private static class UtilityPropertiesManager{
        private final Properties utilityProperties;

        UtilityPropertiesManager(String... args){
            String propertiesFileName = (args.length >0)? args[0]
                    :resolvePropertiesFileName();
            logger.info("Using " +propertiesFileName +" for  utility properties");
            this.utilityProperties = StringUtils.propFileToProperties(propertiesFileName);
        }
        String sampleIdLookupFile() {
           return  StagingUtils.resolveFileFromEnvironmentVariable
                    (PropertiesUtils.getString(utilityProperties,
                            "sampleid.lookup.file", "/tmp/msk-impact/metadata/sampleLookup.txt"));
        }

        String patientIdLookupFile() {
            return StagingUtils.resolveFileFromEnvironmentVariable
                    (PropertiesUtils.getString(utilityProperties,
                            "patientid.lookup.file", "/tmp/msk-impact/metadata/patientLookup.txt"));
        }
        String sourcePath() {
            return StagingUtils.resolveFileFromEnvironmentVariable
                    (PropertiesUtils.getString(utilityProperties,
                            "source.path", "/tmp/msk-impact"));
        }

        String outputPath() {
            return StagingUtils.resolveFileFromEnvironmentVariable
                    (PropertiesUtils.getString(utilityProperties,
                            "output.path", "/tmp/msk-impact/new"));
        }

        private String resolvePropertiesFileName () {
            Optional<DataSourcesMetadata> mdOpt = DataSourcesMetadata.findDataSourcesMetadataByDataSourceName(DATA_SOURCE_NAME);
            if(mdOpt.isPresent()){
                DataSourcesMetadata md = mdOpt.get();
                Path metadatadDirectoryPath = md.resolveBaseStagingDirectory().resolve("metadata");
                return  metadatadDirectoryPath.resolve("idrefactor.properties").toString();
            }
            return "";
        }
    }

    /*
    Private inner class to build in-memory Maps to convert legacy sample and patient
    identifiers to new values.
     */
    private  class LookupMapSupplier implements Supplier<Map<String,String>> {
        private final String mapFile;

        private final Logger logger = Logger.getLogger(LookupMapSupplier.class);
        LookupMapSupplier(String aFile){
            this.mapFile = aFile;
        }
        @Override
        public Map<String, String> get() {
            return this.initializeIdMap(this.mapFile);
        }

        private Map<String,String> initializeIdMap(final String idFile){
            final Map<String,String> idMap = Maps.newHashMap();
            try (BufferedReader reader = new BufferedReader((new FileReader(idFile)));){

                final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
                Observable<CSVRecord> recordObservable = Observable.from(parser.getRecords());
                recordObservable.subscribe(new Subscriber<CSVRecord>() {
                    @Override
                    public void onCompleted() {
                        logger.info("Processed " + idMap.size() + " ids in " + idFile);
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        logger.error(throwable.getMessage());
                        throwable.printStackTrace();
                    }
                    @Override
                    public void onNext(CSVRecord record) {
                        String legacyId = record.get("legacy_id");
                        String newId = record.get("new_id");
                        if (!idMap.containsKey(legacyId)) {
                            idMap.put(record.get("legacy_id"), record.get("new_id"));
                        } else {
                            logger.error("legacy id: " +legacyId +" is repeated in " +idFile +" mapped to "
                                    +idMap.get(legacyId) +" and "  +newId);
                        }
                    }
                });

            } catch (IOException e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            }
            return idMap;
        }

    }

}
