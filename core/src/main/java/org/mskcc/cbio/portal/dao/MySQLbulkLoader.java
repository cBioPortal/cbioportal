/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
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
*/

package org.mskcc.cbio.portal.dao;

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
import java.util.HashMap;
import java.util.Map;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.GlobalProperties;

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
   
   private static final Map<String,MySQLbulkLoader> mySQLbulkLoaders = new HashMap<String,MySQLbulkLoader>();
   /**
    * Get a MySQLbulkLoader
    * @param dbName database name
    * @return 
    */
   public static MySQLbulkLoader getMySQLbulkLoader(String dbName) {
        MySQLbulkLoader mySQLbulkLoader = mySQLbulkLoaders.get(dbName);
        if (mySQLbulkLoader==null) {
            mySQLbulkLoader =  new MySQLbulkLoader(dbName);
            mySQLbulkLoaders.put(dbName, mySQLbulkLoader);
        }
        return mySQLbulkLoader;
   }
   
   public static int flushAll() throws DaoException {
        try {
            int n = 0;
            for (MySQLbulkLoader mySQLbulkLoader : mySQLbulkLoaders.values()) {
                n += mySQLbulkLoader.loadDataFromTempFileIntoDBMS();
            }
            
            mySQLbulkLoaders.clear();
            
            return n;
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
        }
    }

   private String tempFileName = null;
   private File tempFileHandle = null;
   private BufferedWriter tempFileWriter = null;
   private String tableName;
   private final String tempTableSuffix = ".tempTable";
   private int rows;
   // TODO: make configurable
   private static final long numDebuggingRowsToPrint = 0;
   
   private MySQLbulkLoader( String tableName ){
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
      String tmp = GlobalProperties.getTemporaryDir();
      tempFileHandle = File.createTempFile( tableName, tempTableSuffix, new File(tmp) );

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
         tempFileWriter.write( escapeValue(fieldValues[0]) );
         for( int i=1; i<fieldValues.length; i++ ){
            tempFileWriter.write( "\t" );
            tempFileWriter.write( escapeValue(fieldValues[i]) );
         }
         tempFileWriter.newLine();

         if( rows++ < numDebuggingRowsToPrint ){
            StringBuffer sb = new StringBuffer( escapeValue(fieldValues[0]) );
            for( int i=1; i<fieldValues.length; i++ ){
               sb.append( "\t" ).append( escapeValue(fieldValues[i]) );
            }
            System.err.println( "MySQLbulkLoader: Wrote " + sb.toString() + " to '" + tempFileName + "'.");
         }
      } catch (IOException e) {
         System.err.println( "Unable to write to temp file.\n");
         e.printStackTrace();
      }
   }
   
   private String escapeValue(String value) {
       if (value==null) {
           return "\\N";
       }
       
       return value.replace("\r", "").replaceAll("\n", "\\\\n").replace("\t", "\\t");
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
   private int loadDataFromTempFileIntoDBMS() throws DaoException, IOException {
      Connection con = null;
      Statement stmt;

      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         try {
            // close the file, flushing all buffers before loading the DBMS
             tempFileWriter.flush();
             tempFileWriter.close();
         } catch (IOException e) {
            throw new DaoException(e);
         }

         con = JdbcUtil.getDbConnection(MySQLbulkLoader.class);
         stmt = con.createStatement();
         
         // will throw error if attempts to overwrite primary keys in table
         String command = "LOAD DATA LOCAL INFILE '" + tempFileName + "' INTO TABLE " + tableName;
         stmt.execute( command );
         int updateCount = stmt.getUpdateCount();
         System.out.println(""+updateCount+" records inserted into "+tableName);
         int nLines = FileUtil.getNumLines(tempFileHandle);
         if (nLines!=updateCount) {
             System.err.println("Error: but there are "+nLines+" lines in the temp file "+tempFileName);
         } else {
             tempFileHandle.delete();
         }

         // reopen empty temp file -- not necessary, this loader will be removed.
         //this.tempFileWriter = new BufferedWriter(new FileWriter( this.tempFileHandle, false));

         return updateCount;

      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(MySQLbulkLoader.class, con, pstmt, rs);
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
