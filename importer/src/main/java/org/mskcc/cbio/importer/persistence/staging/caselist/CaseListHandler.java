package org.mskcc.cbio.importer.persistence.staging.caselist;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multimap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

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
 * Created by criscuof on 1/25/15.
 */
public class CaseListHandler {
    /*
    responsible for generating a case list file for a cancer study
    reads in a data_clinical.txt file, creates a case list map, and writes out the
    case list file
    currently supports only all cases
    replaces any existing case list file for the same cancer study
     */
    private static final Logger logger = Logger.getLogger(CaseListHandler.class);
    private final Multimap<String,String> caseListMap = ArrayListMultimap.create();
    private static final String CAST_LIST_FILE_NAME = "case_lists/cases_all.txt";
    private final CancerStudyMetadata csMetadata;
    private final Path stagingFilePath;

    public CaseListHandler(Path stagingFileBasePath, String cancerStudyStableId){
        Preconditions.checkArgument(null != stagingFileBasePath,
                "A Path to the base staging file directory is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(cancerStudyStableId),
                "A Importer Cancer Study name is required");
        Preconditions.checkArgument(CancerStudyMetadata.findCancerStudyMetaDataByStableId(cancerStudyStableId).isPresent(),
                cancerStudyStableId +" is not a registered cancer study");
        this.csMetadata = CancerStudyMetadata.findCancerStudyMetaDataByStableId(cancerStudyStableId).get();
        this.stagingFilePath = stagingFileBasePath.resolve(csMetadata.getStudyPath());
    }

    private void outputCaseListFile(){
        
    }


    /*
    private method to complete the case list map
    key = tumor type
    value = list of samples with that tumor type
     */
    private void completeCaseListMap() {
        Path sourceDataPath = this.stagingFilePath.resolve("data_clinical.txt");
        try (BufferedReader reader = Files.newBufferedReader(this.stagingFilePath.resolve("data_clinical.txt"),
                Charset.defaultCharset());  ) {
            final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
            FluentIterable.from(parser)
                    .transform(new Function<CSVRecord, Integer>() {
                                   @Nullable
                                   @Override
                                   public Integer apply(CSVRecord record) {
                                       caseListMap.put(record.get("CANCER_TYPE"),record.get("SAMPLE_ID"));
                                       return 1;
                                   }
                               }
                    ).toList();

        } catch (IOException e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }






}
