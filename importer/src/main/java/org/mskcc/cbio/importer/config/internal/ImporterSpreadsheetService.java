package org.mskcc.cbio.importer.config.internal;

import com.google.common.base.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.IcgcMetadata;

import java.io.IOException;
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
 * Created by criscuof on 12/5/14.
 */
public enum ImporterSpreadsheetService {
    INSTANCE;

    private static final Logger logger = Logger.getLogger(ImporterSpreadsheetService.class);
    private static final String spreadsheetName = "portal_importer_configuration";
    private final SpreadsheetService spreadsheetService = Suppliers.memoize(new SpreadsheetSupplier()).get();


    public WorksheetEntry getWorksheet( String worksheetName) throws Exception {
      Preconditions.checkArgument(!Strings.isNullOrEmpty(worksheetName),
              "A worksheet name is required");
        SpreadsheetEntry spreadsheet = this.getSpreadsheetEntry();
        if (spreadsheet != null) {
            WorksheetFeed worksheetFeed = spreadsheetService.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
            for (WorksheetEntry worksheet : worksheetFeed.getEntries()) {
                if (worksheet.getTitle().getPlainText().equals(worksheetName)) {
                    return worksheet;
                }
            }
        }
        logger.info("Failed to find worksheet" +worksheetName);
        return null;

    }
    /*
      public method to map a specified worksheet from the importer spreadsheet to a Google Guava Table
      Row indexing starts at 1
       */
    public Table<Integer,String,String> getWorksheetTableByName(String worksheetName){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(worksheetName),
                "A worksheet name is required");
        Table<Integer,String,String> worksheetTable = HashBasedTable.create(200,200);
        try {
            WorksheetEntry worksheet = this.getWorksheet(worksheetName);
            if (worksheet != null) {
                List<String> columnNames = this.getWorksheetColumnNames(worksheetName);
                ListFeed feed = spreadsheetService.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
                if (feed != null && feed.getEntries().size() > 0) {
                    Integer rowCount = 1;
                    for (ListEntry entry : feed.getEntries()) {
                       CustomElementCollection columns = entry.getCustomElements();
                        for(String columnName : columnNames){
                            String value = (!Strings.isNullOrEmpty(columns.getValue(columnName)) )?
                                    columns.getValue(columnName): "";
                            worksheetTable.put(rowCount, columnName, value);
                        }
                        rowCount++;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return worksheetTable;
    }

    public List<String> getWorksheetValuesByColumnName(String worksheetName, String columnName){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(worksheetName),
                "A worksheet name is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(columnName),
                "A column name is required");
        List<String> columnList = Lists.newArrayList();
        Table<Integer,String,String> worksheetTable = this.getWorksheetTableByName(worksheetName);
        if(null != worksheetTable){
            if(worksheetTable.containsColumn(columnName)){
                for (Integer row : worksheetTable.rowKeySet()) {
                    columnList.add(worksheetTable.get(row, columnName));
                }
            } else {
                logger.error("Worksheet: " +worksheetName +" does not contain column: " +columnName);
            }
        } else {
            logger.info("The importer spreadsheet does not contain worksheet: " +worksheetName);
        }
        return columnList;
    }

    /*
    Public method to return a list of worksheet column names for a specified
    importer worksheet
     */
    public List<String> getWorksheetColumnNames( String worksheetName){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(worksheetName),
                "A worksheet name is required");
        List<String> columnNames = Lists.newArrayList();
        try {
            WorksheetEntry worksheet = this.getWorksheet(worksheetName);
            if (worksheet != null) {
                ListFeed feed = spreadsheetService.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
                if (feed != null && feed.getEntries().size() > 0) {
                    for (ListEntry entry : feed.getEntries()) {
                        columnNames.addAll(entry.getCustomElements().getTags());
                        }
                    }
          }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return columnNames;
    }

    private SpreadsheetEntry getSpreadsheetEntry() {
        try {
            FeedURLFactory factory = FeedURLFactory.getDefault();
            SpreadsheetFeed feed = this.spreadsheetService.getFeed(factory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);
            for (SpreadsheetEntry entry : feed.getEntries()) {
                if (entry.getTitle().getPlainText().equals(this.spreadsheetName)) {
                    return entry;
                }
            }
        } catch (IOException | ServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Optional<Map<String,String > > getWorksheetRowByColumnValue(String worksheetName,String columnName, String value) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(worksheetName),
                "A Google worksheet name is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(columnName),
                "A worksheet column name is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(value),
                "A  column value is required");
        Table<Integer, String, String> table = ImporterSpreadsheetService.INSTANCE.getWorksheetTableByName(worksheetName);
        String columnKey = columnName;
        if (table.containsColumn(columnKey)) {
            Map<Integer, String> columnMap = table.column(columnKey);
            if (columnMap.containsValue(value)) {
                for (Map.Entry<Integer, String> entry : columnMap.entrySet()) {
                    if (entry.getValue().equals(value)) {
                        return Optional.of(table.row(entry.getKey()));
                    }
                }
            } else {
                logger.error("The column " + columnName + " does not contain a value for " + value);
            }
        } else {
            logger.error("The table does not contain column" + columnKey);
        }
        return Optional.absent(); // return an empty object
    }

    private class SpreadsheetSupplier implements Supplier<SpreadsheetService> {
        private  SpreadsheetService spreadsheetService;
        private String googleId;
        private String googlePw;
        private String appName;
        private String spreadsheetName;

        public SpreadsheetSupplier(){
            this.initGoogleProperties();
        }

        @Override
        public SpreadsheetService get() {
               return this.spreadsheetService;
        }

        private void initGoogleProperties()  {
            Properties properties = new Properties();

            try {
                properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("importer.properties"));
                this.googleId = properties.getProperty("google.id");
                this.googlePw = properties.getProperty("google.pw");
                this.appName = properties.getProperty("importer.spreadsheet_service_appname");
                this.spreadsheetName = properties.getProperty("importer.spreadsheet");
                this.spreadsheetService = new SpreadsheetService(this.appName);
                this.spreadsheetService.setUserCredentials(this.googleId, this.googlePw);

            } catch (IOException |com.google.gdata.util.AuthenticationException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }
 /*
 main method to support stand alone testing
  */
    public static void main(String...args) {
       testColumnNames();
        testIcgcWorksheet();
    }

    private static void testColumnNames() {
        for(String value :  ImporterSpreadsheetService.INSTANCE.getWorksheetValuesByColumnName("icgc","icgcid")){
            logger.info(value);
        }
    }

    private static void testIcgcWorksheet() {
        try {
            Table<Integer,String,String> table = ImporterSpreadsheetService.INSTANCE.getWorksheetTableByName("icgc");
            // find the download directory for the BRCA-UK study
            String columnKey = "icgcid";
            String studyID = "BOCA-UK";
            if(table.containsColumn(columnKey)) {
                Map<Integer,String> columnMap = table.column(columnKey);
                if(columnMap.containsValue(studyID)) {
                    for(Map.Entry<Integer,String> entry : columnMap.entrySet()){
                        if(entry.getValue().equals(studyID)){
                            logger.info("Value " + studyID +" found in row " +entry.getKey().toString());
                            final Map<String, String> rowMap = table.row(entry.getKey());
                            for(Map.Entry<String ,String> rowEntry : rowMap.entrySet()){
                                logger.info(rowEntry.getKey() +": " +rowEntry.getValue());
                            }
                            // create new IcgcMetedata instance from row using Apache Bean utilities
                            IcgcMetadata meta = new IcgcMetadata(rowMap);
                            logger.info("Fom metadata, download directory = " +meta.getDownloaddirectory());

                        }
                    }
                } else {
                    logger.error("The table does not contain a value for " +studyID);
                }
            } else {
                logger.error("The table does not contain column" +columnKey);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
