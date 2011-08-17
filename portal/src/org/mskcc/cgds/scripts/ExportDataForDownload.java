package org.mskcc.cgds.scripts;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.mskcc.cgds.dao.*;
//TODO: ACCESS CONTROL: change to CancerStudy
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.GeneticProfile;

//TODO: NOW: put in cvs
//TODO: put in unit tests
/**
 * Export all profile data to tsv files suitable for downloading.
 * Also produce html index to the exported files. 
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class ExportDataForDownload {
   
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

   /**
    * @param args
    */
   public static void main(String[] args) {
      
      usageLine = "command line usage for ExportProfileData:";

      parser = new OptionParser();
      OptionSpec<String> htmlIndexFile = parser.accepts( "htmlIndexFile", "output file to store html index of files produced" ).
         withRequiredArg().describedAs( "htmlIndexFile" ).ofType( String.class );
      OptionSpec<String> directory = parser.accepts( "directory", "directory to hold download files produced" ).
         withRequiredArg().describedAs( "directory" ).ofType( String.class );
      OptionSpec<String> baseURL = parser.accepts( "baseURL", "base for html links" ).withRequiredArg().
      describedAs( "baseURL" ).ofType( String.class );

      OptionSet options = null;
      try {
         options = parser.parse( args );
      } catch (OptionException e) {
         quit( e.getMessage() );
      }
      
      if( !options.has( baseURL ) || !options.has( directory ) || !options.has( htmlIndexFile ) ){
         quit( "All arguments required.");
      }

      // TODO: maybe write markdown instead of html
      // TODO: export extended mutations; clinical; if in this class, rename it
      // TODO: write targz of all files for each cancer
      // TODO: split miRNA and mRNA
      // TODO: *.seg files: like this gbm one: gdac.broadinstitute.org_gbm.Merge_mirna__h_mirna_8x15k__unc_edu__Level_3__unc_DWD_Batch_adjusted__data.Level_3.2011021700.0.0.tar.gz

      generateExportProfileData( options.valueOf( htmlIndexFile ), 
               options.valueOf( directory ), 
               options.valueOf( baseURL ) );
   }
   
   /**
    * Export all profile data to downloadable *.tsv files.
    * Files are stored in directory.
    * Write an HTML directory of the files into htmlIndexFile. 
    * 
    * @param htmlIndexFile
    * @param directory
    * @param baseURL
    */
   public static void generateExportProfileData( String htmlIndexFile, String directory, String baseURL ){
      
      /*
       * get all cancer types
       * for each cancer type, get all profiles
       * filename = cancer.name + profile.name
       * for each profile, get all cases
       * print header row
       * for each profile, get all genes
       * for each gene, get all values
       * print gene and values
       */
      
      long beginning = System.currentTimeMillis();
      try {

         FileWriter outFile = new FileWriter( htmlIndexFile );
         PrintWriter htmlIndexFilePrintWriter = new PrintWriter(outFile);

         DaoGeneOptimized aDaoGene = DaoGeneOptimized.getInstance();
         ArrayList<CanonicalGene> AllGenes = aDaoGene.getAllGenes();
         // map from EntrezID to HugoSymbol, so we don't have to look up each one
         // TODO: measure how much time this saves, if any
         HashMap<Long, String> geneSymbolMap = new HashMap<Long, String>();
         for( CanonicalGene aCanonicalGene : AllGenes ){
            geneSymbolMap.put( aCanonicalGene.getEntrezGeneId(), aCanonicalGene.getHugoGeneSymbol() );
         }

         ArrayList<CancerStudy> theCancerStudies = DaoCancerStudy.getAllCancerStudies();
         htmlIndexFilePrintWriter.print( "<UL>" );

         for( CancerStudy aCancerStudy : theCancerStudies ){

            System.out.println( "\nProcessing " + aCancerStudy.getName() + "\n" );

            htmlIndexFilePrintWriter.print(  "<LI>" + aCancerStudy.getName() + "</LI>\n"  );
            
            DaoGeneticProfile theDaoGeneticProfile = new DaoGeneticProfile();
            ArrayList<GeneticProfile> theGeneticProfiles =
               theDaoGeneticProfile.getAllGeneticProfiles( aCancerStudy.getInternalId() );
            htmlIndexFilePrintWriter.print( "<UL>" );
            
            // for each of this cancer's genetic profiles
            for( GeneticProfile aGeneticProfile : theGeneticProfiles ){
               
               StringBuffer monitorOutput = new StringBuffer();
               monitorOutput.append( "\tProcessing " + aGeneticProfile.getProfileName() );

               DaoCase theDaoCase = new DaoCase();
               ArrayList<String> theCaseIds =
                  theDaoCase.getAllCaseIdsInProfile( aGeneticProfile.getGeneticProfileId() );

               // don't write empty files
               if( 0 == theCaseIds.size() ){
                  monitorOutput.append( "\tNo cases ... skipping.\n" );
                  System.out.println( monitorOutput.toString() );
                  continue;
               }
               monitorOutput.append( ", with " + theCaseIds.size() + " cases" );
               
               //  TODO:  The code below needs to be updated to get genes in each type of profile
               ArrayList< Long > theGenes = null;

               // don't write empty files
               if( 0 == theGenes.size() ){
                  monitorOutput.append( "\tNo genes ... skipping.\n" );
                  System.out.println( monitorOutput.toString() );
                  continue;
               }
               monitorOutput.append( " and " + theGenes.size() + " genes.\n" );
               
               long start = System.currentTimeMillis();
               
               String filename = aGeneticProfile.getStableId() + TabbedFileSuffix;
               String pathname = directory + "/" + filename;
               FileWriter outDownloadFile = new FileWriter( pathname );
               PrintWriter out = new PrintWriter( outDownloadFile );
                              
               // link to baseURL + filename
               htmlIndexFilePrintWriter.print( "<LI><A HREF=\"" + baseURL + filename + "\">" + 
                        aGeneticProfile.getProfileName() + "</A></LI>\n" );
               
               // TODO: write metadata row:  # date generated, cgds dbms source, documentation of data encoding, 
               DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
               Date date = new Date();
               out.println( "# " + dateFormat.format(date) + ", cgds dbms source, documentation of data encoding, etc.");
               
               // write header row
               out.print( "Hugo Gene Symbol\tGene ID");
               for( String aCase : theCaseIds ){
                  out.print( "\t" + aCase );
               }
               out.println( );

               DaoGeneticAlteration aDaoGeneticAlteration = DaoGeneticAlteration.getInstance();               
               int genesWithoutSymbols = 0;
               for( Long aGeneID : theGenes ){
                  HashMap<String, String> theGeneticAlterationMap =
                     aDaoGeneticAlteration.getGeneticAlterationMap( aGeneticProfile.getGeneticProfileId(), aGeneID.longValue() ); 
                  
                  // output gene symbol and ID
                  String Symbol = geneSymbolMap.get(aGeneID);
                  if( null == Symbol ){
                     out.print( "---" ); // TODO: what should be done if a gene doesn't have a symbol?
                     genesWithoutSymbols++;
                  }else{
                     out.print( Symbol ); 
                  }
                  out.print( "\t" + aGeneID.longValue() );
                  
                  // output data
                  for( String aCase : theCaseIds ){
                     String value = theGeneticAlterationMap.get(aCase);
                     out.print( "\t" );
                     if( null == value ){
                        out.print( NotAvailable );
                     }else{
                        out.print( value );
                     }
                  }
                  out.println( );
               }

               out.close();
               if( 0 < genesWithoutSymbols ){
                  monitorOutput.append( "\tWarning: " + genesWithoutSymbols + " Gene IDs without matching symbols.\n" );                  
               }
               monitorOutput.append( "\tWrote " + pathname + "\n" );
               
               long duration = System.currentTimeMillis() - start;
               monitorOutput.append( "\tProcessed " + ((1000L*(long)theGenes.size()*(long)theCaseIds.size())/duration) + " values per second.\n");

               System.out.println( monitorOutput.toString() );
            }
            
            // TODO NOW: export extended mutations; clinical

            // clinical data
            // TODO need to be able to get all cases for a cancer
            /*
            // get cases
            DaoClinicalData aDaoClinicalData = new DaoClinicalData();
            DaoCaseList aDaoCaseList = new DaoCaseList();
            ArrayList<ClinicalData> allClinicalData = aDaoClinicalData.getCases(
                     getCaseListByStableId         
            
            );
            
            for( ClinicalData aClinicalData : allClinicalData ){
               
            }

            // open file

            String filename = aCancerStudy.getCancerStudyId() + "_clinicalData" + TabbedFileSuffix;
            String pathname = directory + "/" + filename;
            FileWriter outDownloadFile = new FileWriter( pathname );
            PrintWriter out = new PrintWriter( outDownloadFile );
                           
            // link to baseURL + filename
            htmlIndexFilePrintWriter.print( "<LI><A HREF=\"" + baseURL + filename + "\">" + 
                     "Clinical data" + "</A></LI>\n" );
            
            // write file
            // create entry in html
            // close file

             */

            
            htmlIndexFilePrintWriter.print( "</UL>\n" );
         }
         htmlIndexFilePrintWriter.print( "</UL>\n" );
         htmlIndexFilePrintWriter.close();
      
      } catch (DaoException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      System.out.println( "\nTotal time " + (System.currentTimeMillis() - beginning)/1000L + " seconds.");
   }
   
   static final String NotAvailable = "NA";
   static final String TabbedFileSuffix = ".tsv";
}
