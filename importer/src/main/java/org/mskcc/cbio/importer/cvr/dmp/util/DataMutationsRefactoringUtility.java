package org.mskcc.cbio.importer.cvr.dmp.util;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import rx.Observable;
import rx.Subscriber;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
 * Created by <a href="mailto:criscuof@mskcc.org">Fred Criscuolo</a>  on 2/24/15.
 *
 */
public class DataMutationsRefactoringUtility {
    /*
    Java application to refactor DMP legacy sample ids to new values
    1. Read in data_mutations_extended.txt file from specified directory
    2. Refactor legacy ids listed in comment line
    3. Refactor legacy ids in Tumor_Sample_Barcode column
    4. write out refactored data to data_mutations_extended.txt file in specified directory
     */

    private static final Logger logger = Logger.getLogger(DataMutationsRefactoringUtility.class);
    private static final String STAGING_FILE_NAME = "data_mutations_extended.txt";
    private static final String SAMPLE_ID_COLUMN_NAME = "Tumor_Sample_Barcode";
    private final Path legacyFilePath;
    private final Path refactoredFilePath;


    public DataMutationsRefactoringUtility(Path inDirPath, Path outDirPath){
        Preconditions.checkArgument(null!=inDirPath,"A Path to the source directory is required");
        Preconditions.checkArgument(null!=outDirPath,"A Path to the output directory is required");
       this.legacyFilePath = inDirPath.resolve(STAGING_FILE_NAME);
        this.refactoredFilePath = outDirPath.resolve(STAGING_FILE_NAME);
    }

    /*
    package method to process the mutation data and header
    reads each line, and refactors the Tumor_Sample_Barcode (i.e. sample id) column
    writes out the refactored data to the specified output Path
     */
    void refactorRegisteredStagingFile() {
        final List<String> newLineList = Lists.newArrayList();
        StringBuilder sb = new StringBuilder("#sequenced samples ");
        sb.append(StagingCommonNames.blankJoiner.join(this.refactorSampleSet()));
        newLineList.add(sb.toString());
        CSVFormat f = CSVFormat.TDF.withHeader().withCommentMarker('#');
        try (FileReader fr = new FileReader(this.legacyFilePath.toFile())) {
            final CSVParser parser = new CSVParser(new FileReader(this.legacyFilePath.toFile()),
                    f);
            // copy the file headings to the List
            newLineList.add(StagingCommonNames.tabJoiner.join(parser.getHeaderMap().keySet()));
            Observable<CSVRecord> recordObservable = Observable.from(parser.getRecords());
            recordObservable.subscribe(new Subscriber<CSVRecord>() {
                @Override
                public void onCompleted() {
                    try {
                        Files.write(refactoredFilePath, newLineList, Charset.defaultCharset());
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
                public void onNext(CSVRecord record) {
                    newLineList.add(refactorMutationRecord(record));
                }
            });
        } catch(IOException e){
            logger.error(e.getMessage());
        }
    }

    private String refactorMutationRecord(CSVRecord record){
        String legacySampleId = record.get(SAMPLE_ID_COLUMN_NAME);
        Optional<String> idOpt = DmpLegacyIdResolver.INSTANCE.
                resolveNewSampleIdFromLegacySampleId(legacySampleId);
        if (idOpt.isPresent()) {
           String newLine =  record.toString().replace(legacySampleId,idOpt.get());
            return StagingCommonNames.tabJoiner.join(StagingCommonNames.commaSplitter.splitToList(newLine.toString()));
        }
        return StagingCommonNames.tabJoiner.join(StagingCommonNames.commaSplitter.splitToList(record.toString()));
    }

    private Set<String> refactorSampleSet() {
        try (BufferedReader br = new BufferedReader(new FileReader(this.legacyFilePath.toFile()) )){
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
                       logger.info("Unable to resolve legacy Sample ID: " + legacySampleId);
                       return legacySampleId;
                   }
               }).toSet();
    }

        public static void main (String...args) {
            DataMutationsRefactoringUtility utility =
                    new DataMutationsRefactoringUtility(Paths.get("/tmp/msk-impact"),Paths.get("/tmp/msk-impact/new"));
            utility.refactorRegisteredStagingFile();
        }
}
