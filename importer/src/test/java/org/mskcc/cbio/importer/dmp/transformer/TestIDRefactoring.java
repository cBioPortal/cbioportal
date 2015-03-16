package org.mskcc.cbio.importer.dmp.transformer;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import edu.stanford.nlp.io.FileSequentialCollection;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
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
 */
public class TestIDRefactoring {
    private static final Logger logger = Logger.getLogger(TestIDRefactoring.class);
    public TestIDRefactoring() {}

    private String refactorPatientId(String anId){
        return "PATIENT_" + anId;
    }
    private String refactorSampleId (String anId){
        return "SAMPLE_" +anId;
    }


     private void refactorFileFunction(Path inPath, final Path outPath){

         FileSequentialCollection fsc = new FileSequentialCollection(inPath.toFile(),
                 StagingCommonNames.stagingFileExtension,false);
         Observable<File> fileObservable = Observable.from(fsc)
                 // filter out -filtered files from a previous run
                 .filter(new Func1<File,Boolean>() {
                     @Override
                     public Boolean call(File file) {
                         return file.getName().contains("data_clinical");
                     }
                 });
         fileObservable.subscribe(new Subscriber<File>() {
             @Override
             public void onCompleted() {
                 logger.info("File completed");
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

    private void processClinicalFile(File clinicalFile, Path out){
        try {

            final Path outPath = out.resolve(clinicalFile.getName());
            Reader reader = new FileReader(clinicalFile);
            final List<String> outList = Lists.newArrayList();
            final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
            final Map<String, Integer> headerMap = parser.getHeaderMap();
            final String header = StagingCommonNames.tabJoiner.join(headerMap.keySet());
            outList.add(StagingCommonNames.tabJoiner.join(header, "SAMPLE_ID_LEGACY", "PATIENT_ID_LEGACY"));
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
                    String legacySampleId = record.get("SAMPLE_ID");
                    String legacyPatientId = record.get("PATIENT_ID");
                    String line = StagingCommonNames.tabJoiner.join(FluentIterable.from(headerMap.keySet())
                            .transform(new Function<String, String>() {
                                @Nullable
                                @Override
                                public String apply(String input) {
                                    if (input.equals("SAMPLE_ID")) {
                                        return refactorSampleId(record.get("SAMPLE_ID"));
                                    }
                                    if (input.equals("PATIENT_ID")) {
                                        return refactorPatientId((record.get("PATIENT_ID")));
                                    }
                                    return record.get(input);
                                }
                            }).toList());
                    outList.add(StagingCommonNames.tabJoiner.join(line, legacySampleId, legacyPatientId));
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main (String...args) {
        TestIDRefactoring test = new TestIDRefactoring();
        test.refactorFileFunction(Paths.get("/tmp/clinical"),Paths.get("/tmp/clinical/new"));
    }
}
