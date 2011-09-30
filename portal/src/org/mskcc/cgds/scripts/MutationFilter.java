package org.mskcc.cgds.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ExtendedMutation;

/**
 * Filter mutations as they're imported into the CGDS dbms.
 * <p>
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class MutationFilter {
   
   // lists of Entrez gene IDs
   private HashSet<Long> cancerSpecificGermlineWhiteList = new HashSet<Long>();
   private HashSet<Long> somaticWhiteList = new HashSet<Long>();
   
   // text lists of the gene lists, for reporting
   private ArrayList<String> cancerSpecificGermlineWhiteListGeneNames = new ArrayList<String>();
   private ArrayList<ArrayList<String>> somaticWhitelistsGeneNames = new ArrayList<ArrayList<String>>();
   
   // default acceptance decision used by acceptMutation()
   private boolean defaultDecision = false;
   
   private int accepts=0;
   private int germlineWhitelistAccepts=0;
   private int somaticWhitelistAccepts=0;
   private int unknownAccepts=0;
   private int decisions=0;

   /**
    * Construct a MutationFilter with no white lists. 
    * This filter will 
    * <br>
    * REJECT Silent, LOH, Intron and Wildtype mutations, and
    * <br>
    * KEEP all other mutations.
    */
   public MutationFilter( ) {
      defaultDecision = true;
      internalConstructor( (String)null );
   }
   
   /**
    * Construct a MutationFilter with a germline whitelist and, perhaps, some somatic whitelists.
    * Whitelists contain Gene symbols.
    * <p>
    * This filter will 
    * <br>
    * REJECT Silent, LOH, Intron and Wildtype mutations,
    * <br>
    * KEEP valid, somatic mutations,
    * <br>
    * KEEP Germline, non-missense mutations on the germline whitelist,
    * <br>
    * KEEP Somatic mutations on any of the somatic whitelists, which typically contain widely known oncogenes 
    * and genes highly mutated in the cancer, and 
    * <br>
    * KEEP all other mutations if acceptRemainingMutationsBool is true, otherwise REJECT them. 
    * <p>
    * @param acceptRemainingMutationsBool whether to accept mutations not processed by earlier rules
    * @param germlineWhiteListFile filename for the germline whitelist; null if not provided
    * @param somaticGeneListFiles filenames for the somatic whitelist files; null if not provided
    */
   public MutationFilter( boolean acceptRemainingMutationsBool, 
            String germlineWhiteListFile, String... somaticGeneListFiles ) {
      
      this.defaultDecision = acceptRemainingMutationsBool;
      internalConstructor( germlineWhiteListFile, somaticGeneListFiles );
   }
   
   private void internalConstructor( 
            String germlineWhiteListFile,
            String... somaticGeneListFiles) {

      // read germlineWhiteListFile (e.g., ova: BRCA1 BRCA2)
      if( null != germlineWhiteListFile){
         cancerSpecificGermlineWhiteList = getContents(germlineWhiteListFile,
                 this.cancerSpecificGermlineWhiteListGeneNames);
      }

      // read somaticGeneListFiles
      // typically, one global oncogene whitelist and one cancer specific somatic gene whitelist determined
      // from gdac.broadinstitute.org_<cancer>.Mutation_Significance.Level_4.<date><version>/sig_genes.txt
      if( null != somaticGeneListFiles){

         HashSet<Long> tmp = new HashSet<Long>();         
         for( String file : somaticGeneListFiles){
            ArrayList<String> somaticList = new ArrayList<String>();
            HashSet<Long> hs = getContents( file, somaticList );
            this.somaticWhitelistsGeneNames.add(somaticList);
            tmp.addAll(hs);
         }
         somaticWhiteList = tmp;
      }
   }
   
   /**
    * Indicate whether the specified mutation should be accepted as input to
    * the CGDS Database.
    * <p>
    * @param mutation
    *           an ExtendedMutation.
    * <br>
    * @return true if the mutation should be imported into the dbms
    */
   public boolean acceptMutation(ExtendedMutation mutation) {
      this.decisions++;
      
      /*
       * Mutation types from Firehose:
         +------------------------+
         | De_novo_Start          | 
         | Frame_Shift_Del        | 
         | Frame_Shift_Ins        | 
         | Indel                  | 
         | In_Frame_Del           | 
         | In_Frame_Ins           | 
         | Missense               | 
         | Missense_Mutation      | 
         | Nonsense_Mutation      | 
         | Nonstop_Mutation       | 
         | Splice_Site            | 
         | Stop_Codon_Del         | 
         | Translation_Start_Site | 
         +------------------------+
       */
      
      // Don't accept an empty mutation
      if( mutation.getMutationStatus() == null ||
               mutation.getMutationType() == null ||
               mutation.getValidationStatus() == null ){
         return false;
      }
      
      // Do not accept Silent or Intronic Mutations
      if( safeStringTest( mutation.getMutationType(), "Silent" ) ||
               safeStringTest( mutation.getMutationType(), "Intron" ) ){
         return false;
      }

      // Do not accept LOH or Wildtype Mutations
      if( safeStringTest( mutation.getMutationStatus(), "LOH" ) ||
               safeStringTest( mutation.getMutationStatus(), "Wildtype" ) ){
         return false;
      }

      // KEEP: valid, somatic mutations
      if( safeStringTest( mutation.getValidationStatus(), "Valid" ) &&
               safeStringTest( mutation.getMutationStatus(), "Somatic" ) ){
         this.accepts++;
         return true;
      }
      
      // KEEP: Germline, non-missense mutations on a germline whitelist
      if( safeStringTest( mutation.getMutationStatus(), "Germline" ) ){
         if( safeStringTest( mutation.getMutationType(), "Missense" ) ){
            return false;
         }
         if( cancerSpecificGermlineWhiteList.contains(
                  new Long( mutation.getEntrezGeneId() ) ) ){
            this.accepts++;
            this.germlineWhitelistAccepts++;
            return true;
         }         
      }
      
      // KEEP: Somatic mutations on the somatic whitelist
      if( safeStringTest( mutation.getMutationStatus(), "Somatic" ) ){
         if( somaticWhiteList.contains( new Long( mutation.getEntrezGeneId() ) ) ){
            this.accepts++;
            this.somaticWhitelistAccepts++;
            return true;
         }         
      }
      
      // KEEP: Unknown mutations on the somatic whitelist
      if( safeStringTest( mutation.getMutationStatus(), "Unknown" ) ){
         if( somaticWhiteList.contains( new Long( mutation.getEntrezGeneId() ) ) ){
            this.accepts++;
            this.unknownAccepts++;
            return true;
         }         
      }
      
      // everything else
      if( this.defaultDecision ){
         this.accepts++;         
      }
      return defaultDecision;
   }
   
   /**
    * Provide number of decisions made by this MutationFilter.
    * @return the number of decisions made by this MutationFilter
    */
   public int getDecisions(){
      return this.decisions;
   }

   /**
    * Provide number of ACCEPT (return true) decisions made by this MutationFilter.
    * @return the number of ACCEPT (return true) decisions made by this MutationFilter
    */
   public int getAccepts(){
      return this.accepts;
   }

   /**
    * Provide number of germline whitelist ACCEPT (return true) decisions made by this MutationFilter.
    * @return the number of germline whitelist ACCEPT (return true) decisions made by this MutationFilter
    */
   public int getGermlineWhitelistAccepts(){
      return this.germlineWhitelistAccepts;
   }

   /**
    * Provide number of somatic whitelist ACCEPT (return true) decisions made by this MutationFilter.
    * @return the number of somatic whitelist ACCEPT (return true) decisions made by this MutationFilter
    */
   public int getSomaticWhitelistAccepts(){
      return this.somaticWhitelistAccepts;
   }

   /**
    * Provide number of unknown whitelist ACCEPT (return true) decisions made by this MutationFilter.
    * @return the number of unknown ACCEPT (return true) decisions made by this MutationFilter
    */
   public int getUnknownAccepts(){
      return this.unknownAccepts;
   }

   /**
    * Provide number of REJECT (return false) decisions made by this MutationFilter.
    * @return the number of REJECT (return false) decisions made by this MutationFilter
    */
   public int getRejects(){
      return this.decisions - this.accepts;
   }
   
   public String getStatistics(){
      return "Mutation filter decisions: " + this.getDecisions() +
            "\nRejects: " + this.getRejects() +
            "\nAccepts: " + this.getAccepts() + 
            "\nGermline whitelist accepts: " + this.getGermlineWhitelistAccepts() +
            "\nSomatic whitelist accepts: " + this.getSomaticWhitelistAccepts() +
            "\nUnknown accepts: " + this.getUnknownAccepts();
   }

   /**
   * Fetch the entire contents of a text file, and return it in a HashSet.
   * This style of implementation does not throw Exceptions to the caller.
   *
   * @param filename  is a file which already exists and can be read.
   */
   private HashSet<Long> getContents( String filename, ArrayList <String> geneNames ) {

      //...checks on filename are elided
      HashSet<Long> contents = new HashSet<Long>();
      
     try {
        
        File aFile = new File( filename );
       //use buffering, reading one line at a time
       //FileReader always assumes default encoding is OK!
       BufferedReader input =  new BufferedReader(new FileReader(aFile));
       try {
         String line = null; //not declared within while loop
         /*
         * readLine is a bit quirky :
         * it returns the content of a line MINUS the newline.
         * it returns null only for the END of the stream.
         * it returns an empty String if two newlines appear in a row.
         */
         DaoGeneOptimized aDaoGene = DaoGeneOptimized.getInstance();

         while (( line = input.readLine()) != null){

            // convert Hugo symbol to Entrez ID
            CanonicalGene aCanonicalGene = aDaoGene.getGene( line.trim() );
            if( null != aCanonicalGene ){
               contents.add( new Long( aCanonicalGene.getEntrezGeneId() ) );
               geneNames.add( line.trim() );
            }else{
               System.err.println( "MutationFilter: Gene " + line.trim() + " not in dbms." );
            }
         }
      } catch (DaoException e) {
         System.err.println( "dbms access problem.");
         e.printStackTrace();
      } finally {
         input.close();
       }
     } catch (FileNotFoundException e){
        throw new IllegalArgumentException( "Gene list '" + filename + "' not found.");
     } catch (IOException ex){
        ex.printStackTrace();
     }
     return contents;
   }
   
   /**
    * Carefully look for pattern in data.
    * <p>
    * @param data
    * @param pattern
    * @return false if data is null; true if data starts with pattern, independent of case
    */
   private boolean safeStringTest( String data, String pattern ){
      if( null == data){
         return false;
      }
      return data.toLowerCase().startsWith( pattern.toLowerCase() );
   }
   
   @Override
   public String toString(){
      StringBuffer sb = new StringBuffer();
      sb.append( "Default decision: " + this.defaultDecision + "\n" );
      sb.append( "Germline whitelist: " + this.cancerSpecificGermlineWhiteListGeneNames.toString() + "\n" );
      int i=1;
      for( ArrayList<String> somaticWhiteList :  this.somaticWhitelistsGeneNames){
         sb.append( "Somatic whitelist " + i++ + ": " + somaticWhiteList.toString() + "\n" );
      }
      return( sb.toString() );
   }
}