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
   private HashSet<Long> cancer_specific_germline_white_list = new HashSet<Long>(); 

   // text lists of the gene lists, for reporting
   private ArrayList<String> cancer_specific_germline_white_list_gene_names = new ArrayList<String>();

   private int accepts=0;
   private int germlineWhitelistAccepts=0;
   private int somaticWhitelistAccepts=0;
   private int unknownAccepts=0;
   private int decisions=0;
   private int silentOrIntronRejects=0;
   private int lohOrWildTypeRejects=0;
   private int emptyAnnotationRejects=0;
   private int missenseGermlineRejects=0;

   /**
    * Construct a MutationFilter with no white lists. 
    * This filter will 
    * <br>
    * REJECT Silent, LOH, Intron and Wildtype mutations, and
    * <br>
    * KEEP all other mutations.
    */
   public MutationFilter( ) {
      __internalConstructor(null);
   }
   
   /**
    * Construct a MutationFilter with a germline whitelist.
    * Whitelists contain Gene symbols.
    * <p>
    * <p>
    * @param germline_white_list_file filename for the germline whitelist; null if not provided
    */
   public MutationFilter(String germline_white_list_file) {
      __internalConstructor( germline_white_list_file);
   }
   
   private void __internalConstructor(String germline_white_list_file) throws IllegalArgumentException{

      // read germline_white_list_file (e.g., ova: BRCA1 BRCA2)
      if( null != germline_white_list_file ){
         cancer_specific_germline_white_list = getContents(
                 germline_white_list_file, this.cancer_specific_germline_white_list_gene_names );
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

      // out.println("Filter out Mutations: " + mutation.toString());
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
            
      // Do not accept Silent or Intronic Mutations
      if( safeStringTest( mutation.getMutationType(), "Silent" ) ||
               safeStringTest( mutation.getMutationType(), "Intron" ) ){
         silentOrIntronRejects++;
         return false;
      }

      // Do not accept LOH or Wildtype Mutations
      if( safeStringTest( mutation.getMutationStatus(), "LOH" ) ||
               safeStringTest( mutation.getMutationStatus(), "Wildtype" ) ){
         lohOrWildTypeRejects++;
         return false;
      }

      // Do not accept Germline Missense Mutations or Germline Mutations that are not on the white list
      if( safeStringTest( mutation.getMutationStatus(), "Germline" ) ){
         if( safeStringTest( mutation.getMutationType(), "Missense" ) ){
            missenseGermlineRejects++;
            return false;
         }
         if(cancer_specific_germline_white_list != null && cancer_specific_germline_white_list.size() > 0) {
             if (!cancer_specific_germline_white_list.contains(
                  new Long( mutation.getEntrezGeneId() ) ) ){
                return false;
             }
         }         
      }

     this.accepts++;
     return true;
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
     * Provide number of REJECT decisions for Silent or Intron Mutations.
     * @return number of REJECT decisions for Silent or Intron Mutations.
     */
   public int getSilentOrIntronRejects() {
       return this.silentOrIntronRejects;
   }

    /**
     * Provide number of REJECT decisions for LOH or Wild Type Mutations.
     * @return number of REJECT decisions for LOH or Wild Type Mutations.
     */
   public int getLohOrWildTypeRejects() {
       return this.lohOrWildTypeRejects;
   }

    /**
     * Provide number of REJECT decisions for Emtpy Annotation Mutations.
     * @return number of REJECT decisions for Empty Annotation Mutations.
     */
   public int getEmptyAnnotationRejects() {
       return this.emptyAnnotationRejects;
   }

    /**
     * Provide number of REJECT decisions for Missense Germline Mutations.
     * @return number of REJECT decisions for Missense Germline Mutations.
     */
   public int getMissenseGermlineRejects() {
       return this.missenseGermlineRejects;
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
            "\nSilent or Intron Rejects:  " + this.getSilentOrIntronRejects() +
            "\nLOH or Wild Type Rejects:  " + this.getLohOrWildTypeRejects() +
            "\nEmpty Annotation Rejects:  " + this.getEmptyAnnotationRejects() +
            "\nMissense Germline Rejects:  " + this.getMissenseGermlineRejects();
   }

   /**
   * Fetch the entire contents of a text file, and return it in a HashSet.
   * This style of implementation does not throw Exceptions to the caller.
   *
   * @param filename  is a file which already exists and can be read.
   */
   private HashSet<Long> getContents( String filename, ArrayList <String> geneNames ) throws IllegalArgumentException{

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
         // out.println( aFile.getName() + " contains: ");
         DaoGeneOptimized aDaoGene = DaoGeneOptimized.getInstance();
         //DaoGene aDaoGene = new DaoGene();
         
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
      }
       finally {
         input.close();
       }
     }
     catch (FileNotFoundException e){
        throw new IllegalArgumentException( "Gene list '" + filename + "' not found.");
      }
     catch (IOException ex){
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
      sb.append( "Germline whitelist: " + this.cancer_specific_germline_white_list_gene_names.toString() + "\n" );
      return( sb.toString() );
   }

}