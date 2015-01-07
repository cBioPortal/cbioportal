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

package org.mskcc.cbio.importer.persistence.staging.cnv;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.inject.internal.Preconditions;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;

/*
public class responsible for read/write operations to/from the
CNV staging file belonging to a specific cancer study.
CNV data is maintained in a row (i.e. gene name) by column (i.e. sample) matrix
*/
public class CnvFileHandlerImpl implements CnvFileHandler {
    private  Path cnvPath;
    private boolean fileRegistered = false;
     private final static Logger logger = Logger.getLogger(CnvFileHandlerImpl.class);
     
    public CnvFileHandlerImpl(){  
    }
    
     @Override
    public void initializeFilePath(Path filePath) {
       Preconditions.checkArgument(null != filePath,
                "A Path to the CNV staging file  is required");
        // set Path for  CNV data
        this.cnvPath =filePath;
         this.fileRegistered = true;
    }

    public boolean isFileRegistered() { return this.fileRegistered;}
    

    @Override
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
                    for (String sampleName : columnList) {
                        cnvTable.put(geneName, sampleName, record.get(sampleName));
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
    /*
     method to write out updated CNV data as TSV file
     rows = gene names
     columns = DMP sample ids
     values  = gene fold change
     since legacy entries may have been updated, previous file contents are overwritten
     */

    @Override


    public void persistCnvTable(Table<String, String, String> cnvTable) {
        Preconditions.checkArgument(null != cnvTable, "A Table of CNV data is required");
        Preconditions.checkState(null != this.cnvPath,
                "The Path to the data_CNA.txt file has not been specified");
        try (BufferedWriter writer = Files.newBufferedWriter(
                cnvPath, Charset.defaultCharset())) {
            Set<String> geneSet = cnvTable.rowKeySet();
            Set<String> sampleSet = cnvTable.columnKeySet();
            // write out the headers
            writer.append(StagingCommonNames.tabJoiner.join(StagingCommonNames.HUGO_COLUMNNAME,
                    StagingCommonNames.tabJoiner.join(sampleSet)) + "\n");
            // write out values
            for (String gene : geneSet) {
                String geneLine = gene;
                for (String sample : sampleSet) {
                    String value = (cnvTable.get(gene, sample) != null) ? cnvTable.get(gene, sample).toString() : "0";
                    geneLine = StagingCommonNames.tabJoiner.join(geneLine, value);

                }
                writer.append(geneLine + "\n");
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

   


}
