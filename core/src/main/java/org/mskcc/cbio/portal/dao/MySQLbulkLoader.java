/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.util.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

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
   private static boolean relaxedMode = false;
   private String[] fieldNames = null;
   
   private static final Map<String,MySQLbulkLoader> mySQLbulkLoaders = new LinkedHashMap<String,MySQLbulkLoader>();
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
   
   /**
    * Flushes all pending data from the bulk writer. Temporarily disables referential
    * integrity while it does so, largely because MySQL uses a weird model and expects
    * referential integrity at each record, not just at the end of the transaction.
    * @return the number of rows added
    * @throws DaoException
    */
   public static int flushAll() throws DaoException {
	   int checks = 0;
       PreparedStatement stmt = null;
       boolean executedSetFKChecks = false;
       Connection con = null;
       try {
            con = JdbcUtil.getDbConnection(MySQLbulkLoader.class);
            stmt = con.prepareStatement("SELECT @@foreign_key_checks;");
            ResultSet result = stmt.executeQuery();
            
            result.first();
            checks = result.getInt(1);

            stmt = con.prepareStatement("SET foreign_key_checks = ?;");
            stmt.setLong(1, 0);
            stmt.execute();
            executedSetFKChecks = true;
            
            int n = 0;
            for (MySQLbulkLoader mySQLbulkLoader : mySQLbulkLoaders.values()) {
                n += mySQLbulkLoader.loadDataFromTempFileIntoDBMS();
            }
            
            return n;
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
        } catch (SQLException e) {
        	throw new DaoException(e);
        }
        finally {
            mySQLbulkLoaders.clear();
            if (executedSetFKChecks && stmt != null) {
                try {
                    stmt.setLong(1, checks);
                    stmt.execute();
                }
                catch (SQLException e) {
                    throw new DaoException(e);
                }            	
            }
            JdbcUtil.closeAll(MySQLbulkLoader.class, con, stmt, null);
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

      tempFileHandle = File.createTempFile( tableName, tempTableSuffix, FileUtils.getTempDirectory() );

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
         tempFileWriter.write("\n");;

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
         
         String command = "LOAD DATA LOCAL INFILE '" + tempFileName.replace("\\", "\\\\") + "'" + " INTO TABLE " + tableName;
         //if optional fieldNames is set, then use it. This is useful for tables with auto-increment (in such cases the auto-increment
         //field should not be part of the fieldNames list as it will also not be in the lines of the file tempFileName):
         if (fieldNames != null) {
             command += " (" + Arrays.asList(fieldNames).stream().collect(Collectors.joining(",")) + ")";
         }
         stmt.execute( command );
         
         int updateCount = stmt.getUpdateCount();
         ProgressMonitor.setCurrentMessage(" --> records inserted into `"+tableName + "` table: " + updateCount);
         int nLines = FileUtil.getNumLines(tempFileHandle);
         if (nLines!=updateCount && !relaxedMode) {
             String otherDetails = "";
        	 if (stmt.getWarnings() != null) {
        		 otherDetails = "More error/warning details: " + stmt.getWarnings().getMessage();
             }
             throw new DaoException("DB Error: only "+updateCount+" of the "+nLines+" records were inserted in `" + tableName + "`. " + otherDetails + 
            		 " See tmp file for more details: " + tempFileName);
             
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
   
   public static void relaxedModeOn() {
       MySQLbulkLoader.relaxedMode = true;
   }
   
      public static void relaxedModeOff() {
       MySQLbulkLoader.relaxedMode = false;
   }

    public void setFieldNames(String[] fieldNames) {
        this.fieldNames = fieldNames;
    }

}
