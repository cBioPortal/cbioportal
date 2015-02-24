package org.mskcc.cbio.importer.cvr.dmp.util;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import edu.stanford.nlp.io.FileSequentialCollection;
import edu.stanford.nlp.util.StringUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.util.CancerTypeResolver;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
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
 * Java application that will read in all data_clinical staging files in a specified directory,
 * convert the existing DMP sample and patient ids to new sample and patient ids, append the legacy
 * sample and patient ids as new columns, output the refactored file to a new directory with the same
 * file name as the original file
 *

 */
public class ClinicalDataRefactoringUtility {
    private static final Logger logger = Logger.getLogger(ClinicalDataRefactoringUtility.class);
    private static final String PATIENT_ID_COLUMN_NAME = "PATIENT_ID";
    private static final String SAMPLE_ID_COLUMN_NAME = "SAMPLE_ID";
    private static final String CANCER_TYPE_COLUMN_NAME = "CANCER_TYPE";
    private static final String CANCER_TYPE_DETAILED_COLUMN_NAME = "CANCER_TYPE_DETAILED";
    private static final String OTHER_PATIENT_ID_COLUMN_NAME = "OTHER_PATIENT_ID";
    private static final String OTHER_SAMPLE_ID_COLUMN_NAME = "OTHER_SAMPLE_ID";


    public ClinicalDataRefactoringUtility() {
    }
    private String refactorPatientId(String anId){
        Optional<String> newPatientIdOpt = DmpLegacyIdResolver.INSTANCE.resolveNewPatientIdFromLegacyPatientId(anId);
        if(newPatientIdOpt.isPresent()){
            return newPatientIdOpt.get();
        }
        logger.error("Legacy patient id " +anId +" was not found in the patient id map");
        //if the new id isn't in the map yest, retain the use of the existing one
        return anId;
    }
    private String refactorSampleId (String anId){
        Optional<String> newSampleIdOpt = DmpLegacyIdResolver.INSTANCE.resolveNewSampleIdFromLegacySampleId(anId);
        if(newSampleIdOpt.isPresent()){
            return newSampleIdOpt.get();
        }
        logger.error("Legacy sample id " +anId + " was not found in the sample id map");
        //if the new id isn't in the map yest, retain the use of the existing one
        return anId;
    }

    private String refactorCancerTypeDetail(String aTypeDetailed) {
        return aTypeDetailed;

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
    Process all the staging files in the specified directory whose filename
    contains the substring "data_clinical"
     */
    private void refactorFileFunction(Path inPath, final Path outPath){
        FileSequentialCollection fsc = new FileSequentialCollection(inPath.toFile(),
                StagingCommonNames.stagingFileExtension,false);
        Observable<File> fileObservable = Observable.from(fsc)
                // only retain data_clinical type files
                .filter(new Func1<File, Boolean>() {
                    @Override
                    public Boolean call(File file) {
                        return file.getName().contains("data_clinical");
                    }
                });
        fileObservable.subscribe(new Subscriber<File>() {
            @Override
            public void onCompleted() {
                logger.info("All clinical files processed");
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error(throwable.getMessage());
                throwable.printStackTrace();
            }

            @Override
            public void onNext(File file) {
                logger.info("Processing file " +file.getName());
                processClinicalFile(file, outPath);
            }
        });
    }

    /*
    Process a clinical staging file. Create a header for the new staging file with two
    additional columns for the legacy sample and patient ids. For each line in the
    original file, refactor the legacy sample and patient ids to their new values and
    write out the rest of the remaining original columns as is. If the file is
    data_clinical.txt, append the legacy sample and patient ids as new columns.
    If the file contains columns for cancer_type and cancer_type_detail, refactor these values using the
    oncotree map
    Each processed file generates a refactored file with the same name in the specified output
    directory
     */
    private void processClinicalFile(File clinicalFile, final Path out){
        try {

            final boolean legacyColumnsFlag = (clinicalFile.getName().equals("data_clinical.txt"))?true:false;
            final Path outPath = out.resolve(clinicalFile.getName());
            Reader reader = new FileReader(clinicalFile);
            final List<String> outList = Lists.newArrayList();
            final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
            final Map<String, Integer> headerMap = parser.getHeaderMap();
            final String header = StagingCommonNames.tabJoiner.join(headerMap.keySet());
            outList.add(StagingCommonNames.tabJoiner.join(header, OTHER_SAMPLE_ID_COLUMN_NAME,
                    OTHER_PATIENT_ID_COLUMN_NAME));
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
                    String legacySampleId = record.get(SAMPLE_ID_COLUMN_NAME);
                    String legacyPatientId = record.get(PATIENT_ID_COLUMN_NAME);
                    String line = StagingCommonNames.tabJoiner.join(FluentIterable.from(headerMap.keySet())
                            .transform(new Function<String, String>() {
                                @Nullable
                                @Override
                                public String apply(String input) {
                                    if (input.equals(SAMPLE_ID_COLUMN_NAME)) {
                                        return refactorSampleId(record.get(SAMPLE_ID_COLUMN_NAME));
                                    }
                                    if (input.equals(PATIENT_ID_COLUMN_NAME)) {
                                        return refactorPatientId((record.get(PATIENT_ID_COLUMN_NAME)));
                                    }
                                    // refactor the cancer type column using the cancer type detailed value to
                                    // find a new cancer type from the oncotree worksheet
                                    if (input.equals(CANCER_TYPE_COLUMN_NAME)) {
                                        return refactorCancerType(record.get(CANCER_TYPE_COLUMN_NAME),record.get(CANCER_TYPE_DETAILED_COLUMN_NAME));
                                    }
                                    return record.get(input);
                                }
                            }).toList());

                    // add the legacy dmp sample and patient ids if this is the data_clinical.txt file
                    if (legacyColumnsFlag) {
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

    public static void main (String...args) {
        ClinicalDataRefactoringUtility test = new ClinicalDataRefactoringUtility();
        Properties utilityProperties = StringUtils.propFileToProperties("/tmp/clinical/idrefactor.properties");
        String sourcePath = edu.stanford.nlp.util.PropertiesUtils.getString(utilityProperties,
                "source.path", "/tmp/clinical");
        String outputPath = edu.stanford.nlp.util.PropertiesUtils.getString(utilityProperties,
                "output.path", "/tmp/clinical/new");
        logger.info("The data_clinical files in " +sourcePath +" will be processed");
        logger.info("The refactored data_clinical files will be written to "
            +outputPath);
        test.refactorFileFunction(Paths.get(sourcePath),Paths.get(outputPath));
    }
}
