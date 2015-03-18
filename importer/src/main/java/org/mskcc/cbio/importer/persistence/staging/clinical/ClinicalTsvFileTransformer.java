package org.mskcc.cbio.importer.persistence.staging.clinical;


import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import rx.Observable;
import rx.Subscriber;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardOpenOption.*;

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
 * Created by Fred Criscuolo on 3/2/15.
 * criscuof@mskcc.org
 */
public class ClinicalTsvFileTransformer {
    /*
    Responsible for generic transformation of a clinical data file in Excel format
    to a TSV file
    Notes: ALL columns in the Excel file are copied as is
                  column names can be supplied
     */

    private static final Logger logger = Logger.getLogger(ClinicalTsvFileTransformer.class);
    private final Path excelPath;
    private final Path tsvPath;
    private List<String> headingsList;

    /**
     *
     * @param filePath - Path to Excel input file
     * @param dataSource - data source name from importer worksheet
     * @param clinicalFileName - name of output file
     * @param hList - list of column headings
     */
    public ClinicalTsvFileTransformer(Path filePath, String dataSource, String clinicalFileName,
                                        List<String> hList){
        Preconditions.checkArgument(null!=filePath,
                "A Path to a clinical data file in Excel format is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dataSource),
                "An importer data source name is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clinicalFileName),
                "A file name for the clinical staging file is required");
        this.excelPath = filePath;
        this.tsvPath = this.resolveOutputPath(dataSource, clinicalFileName);
        // list can be empty but not null
        Preconditions.checkArgument(null != hList, " A list of replacement column headings was not provided");
        this.headingsList = hList;
    }

    /*
    Private method to resolve the output Path using the download directory
    specified for this data source and the supplied file name
     */
    private Path resolveOutputPath(String dataSource, String fileName){
        Optional<DataSourcesMetadata> metadataOptional =
                DataSourcesMetadata.findDataSourcesMetadataByDataSourceName(dataSource);
        Preconditions.checkState(metadataOptional.isPresent(),
                dataSource +" is not a registered importer data source");
        return metadataOptional.get().resolveBaseStagingDirectory().resolve(fileName);
    }
    private void transform() {
        try(FileReader reader = new FileReader(this.excelPath.toFile())) {
            // Even if the file is a TDF, opening it as an EXCEL file avoids errors with empty columns
            final CSVParser parser = new CSVParser(new FileReader(this.excelPath.toFile()),
                    CSVFormat.EXCEL.withIgnoreEmptyLines(true).withHeader());
            // write header to new file
            Files.write(this.tsvPath, Lists.newArrayList(generateHeader(parser.getHeaderMap())), Charset.defaultCharset(),
                    new StandardOpenOption[]{CREATE_NEW, DSYNC});
            // append data
            Files.write(this.tsvPath, Lists.transform(parser.getRecords(), transformationFunction), Charset.defaultCharset(),
                    new StandardOpenOption[]{ APPEND, DSYNC});
        } catch (IOException e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateHeader(Map<String,Integer>columnMap){
        if(this.headingsList.isEmpty()){
            for (Map.Entry<String,Integer> entry : columnMap.entrySet()){
                headingsList.add(entry.getKey());
            }
        }
        return StagingCommonNames.tabJoiner.join(headingsList);
    }

   public final Function<CSVRecord,String> transformationFunction =new Function<CSVRecord,String>(){
       @Nullable
       @Override
       public String apply(CSVRecord record) {
           return StagingCommonNames.tabJoiner.join(
                   StagingCommonNames.commaSplitter.split(record.toString()));
       }
   };
    // main method for stand alone testing
    public static void main (String...args) {
        List<String> emptyHeaderList = Lists.newArrayList();
       // Path sourcePath = Paths.get("/tmp/Clinical-FM-data.txt");
        Path sourcePath = Paths.get("/tmp/Clinical-FM-data.xls");
        String dataSource = "foundation-dev";
        String outFile = "data_clinical.txt";
        ClinicalTsvFileTransformer test = new ClinicalTsvFileTransformer(sourcePath,dataSource,outFile, emptyHeaderList);
        test.transform();

    }

}
