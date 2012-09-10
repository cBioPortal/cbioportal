package org.mskcc.cbio.cgds.scripts;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Date;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.mskcc.cbio.cgds.dao.MySQLbulkLoader;
import org.mskcc.cbio.cgds.model.GeneticAlterationType;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.util.FileUtil;
import org.mskcc.cbio.cgds.util.GeneticProfileReader;
import org.mskcc.cbio.cgds.util.ProgressMonitor;

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

   private static void quit(String msg){
      if( null != msg ){
         System.err.println( msg );
      }
      System.err.println( usageLine );
      try {
         parser.printHelpOn(System.err);
      } catch (IOException e) {
         e.printStackTrace();
      }
      System.exit(1);      
   }

   public static void main(String[] args) throws Exception {
       args = new String[]{"--data"
               , "/Users/jj/projects/GDAC-staging/prad_mich/data_CNA.txt"
               , "--meta"
               , "/Users/jj/projects/GDAC-staging/prad_mich/meta_CNA.txt"
               , "--dbmsAction"
               , "clobber"};
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