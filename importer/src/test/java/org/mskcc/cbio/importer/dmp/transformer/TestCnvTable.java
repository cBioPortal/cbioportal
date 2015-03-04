package org.mskcc.cbio.importer.dmp.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.inject.internal.Preconditions;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.dmp.model.DmpData;
import org.mskcc.cbio.importer.cvr.dmp.transformer.DmpCnvTransformer;
import org.mskcc.cbio.importer.persistence.staging.cnv.CnvFileHandlerImpl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
 * Created by criscuof on 1/26/15.
 */
public class TestCnvTable {
    private final static Logger logger = Logger.getLogger(TestCnvTable.class);

    Path cnvPath;

    public TestCnvTable(Path aPath){
        this.cnvPath = aPath;
    }

    public Table<String, String, String> initializeCnvTable() {
        Preconditions.checkState(null != this.cnvPath,
                "The Path to the data_CNA.txt file has not been specified");
        logger.info("Reading in existing cnv data from " +this.cnvPath.toString());
        Table<String, String,String> cnvTable = HashBasedTable.create();
        // determine if there are persisted cnv data; if so read into Table data structure
        if (Files.exists(cnvPath, LinkOption.NOFOLLOW_LINKS)) {
            Reader reader = null;
            try {
                reader = new FileReader(this.cnvPath.toFile());
                final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
                Map<String, Integer> headerMap = parser.getHeaderMap();
                List<String> columnList = Lists.newArrayList();
                for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                    columnList.add(entry.getKey());
                }
                // determine the column name used for the Hugo Symbol
                String geneColumnName = columnList.remove(0);
                // process each data row in table

                for (CSVRecord record : parser.getRecords()) {
                    String geneName = record.get(geneColumnName);
                    logger.info(geneName);
                    for (String sampleName : columnList) {
                        String value = null;
                        try {
                            value = record.get(sampleName);
                        } catch (Exception e) {
                            logger.error("Missing value for gene "+geneName +" sample " +sampleName);
                            value = "0";
                        }
                        cnvTable.put(geneName, sampleName, value);
                    }
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
            }

        }
        return cnvTable;
    }

    // main method for stand alone testing
    public static void main(String...args){
        ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        String tempDir = "/tmp/msk-impact/msk-impact";
        File tmpDir = new File(tempDir);
        tmpDir.mkdirs();
        Path stagingFileDirectory = Paths.get(tempDir).resolve("data_CNA.txt");
       TestCnvTable test = new TestCnvTable(stagingFileDirectory);
        try {
            test.initializeCnvTable();

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }
}
