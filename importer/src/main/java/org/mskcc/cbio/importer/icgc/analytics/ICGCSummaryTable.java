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
package org.mskcc.cbio.importer.icgc.analytics;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.gdata.util.common.base.Preconditions;
import com.google.inject.internal.Lists;
import com.google.inject.internal.Maps;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;


/*
 A Java application to produce a summary table of ICGC variation classification
 counts by non-US ICGC studies. Inputs are MAF files produced by the ICGC import
 process. Output is a text file in tsv format
 */
public class ICGCSummaryTable {

    private static final Logger logger = Logger.getLogger(ICGCSummaryTable.class);

    private Table<String, String, Integer> icgcTable;
    private Set<String> varClassSet;
    private Set<String> studySet;
    private Map<String,Integer> tumorSampleIdMap;
    private Integer totalSampleCount = 0;
    private static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
    private final String mafDirectoryName;
    private final List<File> mafFileList;
    private static final String MAF_FILE_EXTENSION = ".maf";
    private static final String OUTPUT_FILE_NAME = "icgc_summary_table.tsv";
    private Set<String> tumorSampleIdSet;
    private final Joiner pathJoiner = Joiner.on(System.getProperty("file.separator"));

    public ICGCSummaryTable(final String dirName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dirName),
                "A directory for the MAF files is required");
        this.icgcTable = HashBasedTable.create();
        this.studySet = Sets.newHashSet();
        this.varClassSet = Sets.newHashSet();
        this.mafDirectoryName = dirName;
        this.mafFileList = this.resolveFileList(mafDirectoryName);
        this.icgcTable.put("total", "total", 0);
         this.tumorSampleIdMap = Maps.newHashMap();
        try {
            this.processMafFiles();
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        
    }

    /*
     private method to process all the MAF (.maf) files in the specified
     directory
     */
    private void processMafFiles() throws IOException {
        for (File mafFile : this.mafFileList) {
            logger.info("Processing MAF file " + mafFile.getAbsolutePath());
            // clear the tumor sample id set
           this.tumorSampleIdSet = Sets.newHashSet();
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(mafFile.getAbsolutePath()), Charset.defaultCharset())) {
                final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
                for (CSVRecord record : parser) {
                    this.incrementVariationCounts(record.get("Center"), record.get("Variant_Classification"));
                    this.processTumorSampleBarcode(record.get("Center"), record.get("Tumor_Sample_Barcode"));
                }
            }
        }
        // all the variant counts have been entered in the table - now write it out to a file 
        this.outputTable();
    }
    
    private void processTumorSampleBarcode(String center, String barcode){
        //the barcode may be empty
        if(Strings.isNullOrEmpty(barcode)) { return; }
        if(!this.tumorSampleIdSet.contains(barcode)){
            this.tumorSampleIdSet.add(barcode);
            this.tumorSampleIdMap.put(center, this.tumorSampleIdSet.size());
            this.totalSampleCount++;
        }
    }

    private void outputTable() throws IOException {
        Path outPath = this.resolveOutputPath();

        Files.write(outPath, this.formatTable(), Charset.defaultCharset(), StandardOpenOption.CREATE);

    }
    /*
     private method to format the variant table rows into a list of
     tab-delimited Strings  
     */

    private List<String> formatTable() {
        List<String> tableData = Lists.newArrayList();
        tableData.add(tabJoiner.join("Center", "Samples",tabJoiner.join(this.getSortedListWithTotal(varClassSet))));
        StringBuilder sb = null;
        for (String rowName : this.getSortedListWithTotal(studySet)) {
            sb = new StringBuilder(rowName);
             sb.append("\t");
            sb.append(this.tumorSampleIdMap.get(rowName));
            for (String colName : this.getSortedListWithTotal(varClassSet)) {
                sb.append("\t");
                if (this.icgcTable.contains(rowName, colName)) {
                    sb.append(this.icgcTable.get(rowName, colName));
                }
            }
            tableData.add(sb.toString());
        }
        
        //add the total sample count as a separate line
        tableData.add("Total Sample Count\t" +this.totalSampleCount);
        return tableData;
    }

    private List<String> getSortedListWithTotal(Set<String> unsortedSet) {
        List<String> sorted = Ordering.natural().sortedCopy(unsortedSet);    
        // add the "total" to the end of the table      
        sorted.add("total");
        return sorted;
    }

    /*
     private method to increment variation counts
     */
    private void incrementVariationCounts(String rowName, String columnName) {
        if (Strings.isNullOrEmpty(columnName)) {
            columnName = "unknown";
        }
        // increement row & column totals
        this.incrementTotals(rowName, columnName);
        // initialize the cell if the row and column have not been specified already
        if (!this.icgcTable.contains(rowName, columnName)) {
            this.icgcTable.put(rowName, columnName, 0);
        }
        Integer value = this.icgcTable.get(rowName, columnName);
        this.icgcTable.put(rowName, columnName, ++value);
    }

    /*
     private method to increment row, column, and table totals
     also adds new row and column names to respective lists  
     */
    private void incrementTotals(String rowName, String columnName) {
        // row total
        if (!this.icgcTable.containsRow(rowName)) {
            this.icgcTable.put(rowName, "total", 0);
            this.studySet.add(rowName);
        }
        Integer rowTotal = this.icgcTable.get(rowName, "total");
        this.icgcTable.put(rowName, "total", ++rowTotal);
        // column total
        if (!this.icgcTable.containsColumn(columnName)) {
            this.icgcTable.put("total", columnName, 0);
            this.varClassSet.add(columnName);
        }
        Integer colTotal = this.icgcTable.get("total", columnName);
        this.icgcTable.put("total", columnName, ++colTotal);
        Integer total = this.icgcTable.get("total", "total");
        this.icgcTable.put("total", "total", total + 1);
    }

    private List<File> resolveFileList(String dirName) {
        File dir = new File(dirName);
        return Arrays.asList(dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(MAF_FILE_EXTENSION);
            }
        }));
    }

    private Path resolveOutputPath() {
        String tableFilename = pathJoiner.join(this.mafDirectoryName,
                OUTPUT_FILE_NAME);
        logger.info("Summary statistics written to " + tableFilename);
        return Paths.get(tableFilename);
    }

    public static void main(String... args) {
        //Preconditions.checkArgument(args.length > 0,
        //        "Usage: java ICGCSummaryTable maf-file-directory ");

        String mafDirectory = (null != args && args.length > 0) ? args[0] : "/data/icgctest/maffiles";
        logger.info("ICGC MAF files will be read from " + mafDirectory);
        ICGCSummaryTable table = new ICGCSummaryTable(mafDirectory);
      
    }
}
