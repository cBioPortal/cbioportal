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

package org.mskcc.cbio.portal.scripts;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Date;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.GeneticProfileReader;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * Import 'profile' files that contain data matrices indexed by gene, case. 
 * <p>
 * @author ECerami
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class ImportProfileData{

    public static final int ACTION_CLOBBER = 1;
    private static String usageLine;
    private static OptionParser parser;

    private static void quit(String msg)
    {
        if( null != msg ){
            System.err.println( msg );
        }
        System.err.println( usageLine );
        try {
            parser.printHelpOn(System.err);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   public static void main(String[] args) throws Exception {
       Date start = new Date();

       // use a real options parser, help avoid bugs
       usageLine = "Import 'profile' files that contain data matrices indexed by gene, case.\n" +
       		"command line usage for importProfileData:";
       /*
        * usage:
        * --data <data_file.txt> --meta <meta_file.txt> --dbmsAction [clobber (default)]  --loadMode
        *  [directLoad|bulkLoad (default)] " +
        * --germlineWhiteList <filename> --acceptRemainingMutations --somaticWhiteList <filename>
        * --somaticWhiteList <filename>
        */

       parser = new OptionParser();
       OptionSpec<Void> help = parser.accepts( "help", "print this help info" );
       OptionSpec<String> data = parser.accepts( "data",
               "profile data file" ).withRequiredArg().describedAs( "data_file.txt" ).ofType( String.class );
       OptionSpec<String> meta = parser.accepts( "meta",
               "meta (description) file" ).withRequiredArg().describedAs( "meta_file.txt" ).ofType( String.class );
       OptionSpec<String> dbmsAction = parser.accepts( "dbmsAction",
               "database action; 'clobber' deletes exsiting data" )
          .withRequiredArg().describedAs( "[clobber (default)]" ).ofType( String.class );
       OptionSpec<String> loadMode = parser.accepts( "loadMode", "direct (per record) or bulk load of data" )
          .withRequiredArg().describedAs( "[directLoad|bulkLoad (default)]" ).ofType( String.class );
       OptionSpec<String> germlineWhiteList = parser.accepts( "germlineWhiteList",
               "list of genes whose non-missense germline mutations should be loaded into the dbms; optional" )
          .withRequiredArg().describedAs( "filename" ).ofType( String.class );
       OptionSet options = null;
      try {
         options = parser.parse( args );
         //exitJVM = !options.has(returnFromMain);
      } catch (OptionException e) {
          quit( e.getMessage() );
      }
      
      if( options.has( help ) ){
          quit( "" );
      }
       
       File dataFile = null;
       if( options.has( data ) ){
          dataFile = new File( options.valueOf( data ) );
       }else{
           quit( "'data' argument required.");
       }

       File descriptorFile = null;
       if( options.has( meta ) ){
          descriptorFile = new File( options.valueOf( meta ) );
       }else{
           quit( "'meta' argument required.");
       }

       int updateAction = ACTION_CLOBBER;
       if( options.has( dbmsAction ) ){
          String actionArg = options.valueOf( dbmsAction );
          if (actionArg.equalsIgnoreCase("clobber")) {
             updateAction = ACTION_CLOBBER;
         } else {
              quit( "Unknown dbmsAction action:  " + actionArg );
         }
          System.err.println(" --> updateAction:  " + actionArg);
       }
       
       MySQLbulkLoader.bulkLoadOn();
       if( options.has( loadMode ) ){
          String actionArg = options.valueOf( loadMode );
          if (actionArg.equalsIgnoreCase("directLoad")) {
             MySQLbulkLoader.bulkLoadOff();
          } else if (actionArg.equalsIgnoreCase( "bulkLoad" )) {
             MySQLbulkLoader.bulkLoadOn();
          } else {
              quit( "Unknown loadMode action:  " + actionArg );
          }
       }

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        System.err.println("Reading data from:  " + dataFile.getAbsolutePath());
        GeneticProfile geneticProfile = null;
         try {
            geneticProfile = GeneticProfileReader.loadGeneticProfile( descriptorFile );
         } catch (java.io.FileNotFoundException e) {
             quit( "Descriptor file '" + descriptorFile + "' not found." );
         }

        int numLines = FileUtil.getNumLines(dataFile);
        System.err.println(" --> profile id:  " + geneticProfile.getGeneticProfileId());
        System.err.println(" --> profile name:  " + geneticProfile.getProfileName());
        System.err.println(" --> genetic alteration type:  " + geneticProfile.getGeneticAlterationType());
        System.err.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        
        if (geneticProfile.getGeneticAlterationType().equals(GeneticAlterationType.MUTATION_EXTENDED)) {
   		   
   	      String germlineWhitelistFilename = null;
            if( options.has( germlineWhiteList ) ){
               germlineWhitelistFilename = options.valueOf( germlineWhiteList );
            }
   
            ImportExtendedMutationData importer = new ImportExtendedMutationData( dataFile,
                  geneticProfile.getGeneticProfileId(), pMonitor, 
                  germlineWhitelistFilename);
            System.out.println( importer.toString() );
            importer.importData();
        }
	    else if (geneticProfile.getGeneticAlterationType().equals(GeneticAlterationType.FUSION)) {
	        ImportFusionData importer = new ImportFusionData(dataFile,
				geneticProfile.getGeneticProfileId(),
				pMonitor);
	        importer.importData();
        } else {
            ImportTabDelimData importer = new ImportTabDelimData(dataFile, geneticProfile.getTargetLine(),
                    geneticProfile.getGeneticProfileId(), pMonitor);
            importer.importData();
        }
      
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
        Date end = new Date();
        long totalTime = end.getTime() - start.getTime();
        System.out.println ("Total time:  " + totalTime + " ms");
    }
    
}