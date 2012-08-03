package org.mskcc.cbio.cgds.dao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * To speed up CGDS data loading, bulk load from files using MySQL "LOAD DATA INFILE" functionality.
 * Intercept each record write in the normal load, buffer it in a temp file, and load the temp file when done.
 * NOT thread-safe.
 * @author arthur goldberg
 * In the future, would be cooler to implement this by overloading the JDBC Connection.prepareStatement
 * and PreparedStatement.setX() calls.
 */
public class MySQLbulkLoader {
   private static boolean bulkLoad = false;

   private String tempFileName = null;
   private File tempFileHandle = null;
   private BufferedWriter tempFileWriter = null;
   private String tableName;
   private final String tempTableSuffix = ".tempTable";
   private int rows;
   // TODO: make configurable
   private static long numDebuggingRowsToPrint = 0;
   
   MySQLbulkLoader( String tableName ){
      try {
          openTempFile( tableName );
         this.tableName = tableName;
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   /**
    * Open temp file for table 'tableName'.
    * note that auto_increment fields must be handled specially.
    * @param tableName
    * @throws FileNotFoundException
    * @throws IOException
    * @throws IllegalArgumentException
    */
   private void openTempFile(String tableName) throws IOException {

      // TODO: create special directory for temp dbms load files; perhaps make OS portable
      File tempFileHandle = File.createTempFile( tableName, tempTableSuffix, new File("/tmp" ) );
      this.tempFileHandle = tempFileHandle;

      // delete file when JVM exits
      tempFileHandle.deleteOnExit();

      tempFileName = tempFileHandle.getAbsolutePath();

      if (!tempFileHandle.exists()) {
         throw new FileNotFoundException("File does not exist: " + tempFileHandle);
      }
      if (!tempFileHandle.isFile()) {
         throw new IllegalArgumentException("Should not be a directory: " + tempFileHandle);
      }
      if (!tempFileHandle.canWrite()) {
         throw new IllegalArgumentException("File cannot be written: " + tempFileHandle);
      }

      // to improve performance use buffering; FileWriter always assumes default encoding is OK!
      this.tempFileWriter = new BufferedWriter(new FileWriter(tempFileHandle, false));
   }
   
   /**
    * write a record's fields, in order, to the table's temp file. if no fields are provided, writes no record.
    * fields are TAB separated. 
    * @param fieldValues
    */
   public void insertRecord( String... fieldValues) {
      if( fieldValues.length == 0 ){
         return;
      }
      try {
         tempFileWriter.write( fieldValues[0] );
         for( int i=1; i<fieldValues.length; i++ ){
            tempFileWriter.write( "\t" );
            tempFileWriter.write( fieldValues[i] );
         }
         tempFileWriter.newLine();

         if( rows++ < numDebuggingRowsToPrint ){
            StringBuffer sb = new StringBuffer( fieldValues[0] );
            for( int i=1; i<fieldValues.length; i++ ){
               sb.append( "\t" ).append( fieldValues[i] );
            }
            System.err.println( "MySQLbulkLoader: Wrote " + sb.toString() + " to '" + tempFileName + "'.");
         }
      } catch (IOException e) {
         System.err.println( "Unable to write to temp file.\n");
         e.printStackTrace();
      }
   }
   
   /**
    * load the temp file maintained by the MySQLbulkLoader into the DMBS.
    * truncates the temp file, and leaves it open for more insertRecord() operations.
    * returns number of records inserted.
    * 
    * TODO: perhaps instead of having each program that uses a DAO that uses bulk loading call 'completeInsert', 
    * get MySQLbulkLoader created by a factory, and have the factory remember to load all the tables from all
    * the temp files before the program exits. 
    * @return number of records inserted
    * @throws DaoException
    * @throws IOException 
    */
   public int loadDataFromTempFileIntoDBMS() throws DaoException, IOException {
      Connection con = null;
      Statement stmt = null;

      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         try {
            // close the file, flushing all buffers before loading the DBMS
             tempFileWriter.close();
         } catch (IOException e) {
            throw new DaoException(e);
         }

         con = JdbcUtil.getDbConnection();
         stmt = con.createStatement();
         
         // will throw error if attempts to overwrite primary keys in table
         String command = "LOAD DATA LOCAL INFILE '" + tempFileName + "' INTO TABLE " + tableName;
         long startTime = System.currentTimeMillis();
         boolean rv = stmt.execute( command );
         // TODO: throw exception if rv == true
         int updateCount = stmt.getUpdateCount();
         long duration = (System.currentTimeMillis() - startTime)/1000;

         // reopen empty temp file
         this.tempFileWriter = new BufferedWriter(new FileWriter( this.tempFileHandle, false));
         return updateCount;

      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   public String getTempFileName() {
      return tempFileName;
   }

   public String getTableName() {
      return tableName;
   }

   public static boolean isBulkLoad() {
      return bulkLoad;
   }

   public static void bulkLoadOn() {
      MySQLbulkLoader.bulkLoad = true;
   }
   
   public static void bulkLoadOff() {
      MySQLbulkLoader.bulkLoad = false;
   }
   
}
