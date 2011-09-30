package org.mskcc.cgds.scripts;

import java.io.*;
import java.util.*;

/**
 * 
 * The syntax is simple:
 * 
 * java ComputeZscoreUnit <copy_number_file> <expression_file> <output_file>
 * 
 * The output is written onto a file named "zscores.txt"
 * 
 * It will work no matter how many columns there are before the actual values,
 * the two requisites on the format of the input files are:
 * 
 * - the first column gives the gene identifier (in the current files this is the gene symbol)
 * - sample names start with the "TCGA" prefix
 * 
 * Algorithm
 * Input copy number (CNA) and expression (exp) files
 * concept:
 * for each gene{
 *    identify diploid cases in CNA
 *    obtain mean and sd of exp of diploid cases
 *    for each case{
 *       zScore <- (value - mean)/sd
 *    }
 * }
 * implementation:
 * read CNA: build hash geneCopyNumberStatus: gene -> Array of (caseID, value ) pairs
 * read exp: skip normal cases; 
 * for each gene{
 *    get mean and s.d. of elements of diploids
 *    get zScore for each case
 * }
 *
 * @author Giovanni Ciriello
 * @author Arthur Goldberg  goldberg@cbio.mskcc.org
 */
public class ComputeZScoreUnit{

   private static HashMap<String, ArrayList<String[]>> geneCopyNumberStatus;
   private static int samples;
   private static String zScoresFile;
   
   public static void main (String[]args) throws Exception{
      // TODO: argument error checking; use command line parser
      String copyNumberFile = args[0];
      String expressionFile = args[1];
      zScoresFile = args[2];
      geneCopyNumberStatus = readCopyNumberFile(copyNumberFile);
      computeZScoreXP(expressionFile); 
   }   
   
   public static void computeZScoreXP(String file) throws IOException{
      
      PrintWriter out = new PrintWriter(new FileWriter( zScoresFile ));
      BufferedReader in = new BufferedReader(new FileReader(file));
      String header = in.readLine();
      String[]values = header.split("\t");
      
      int firstSamplePosition = 0;
      
      // Assume: case IDs contain TCGA 
      // TODO: generalize to other case ID formats
      search:
      for(int i=0;i<values.length;i++) {
         if(values[i].indexOf("TCGA") != -1){
            firstSamplePosition = i;
            break search;
         }
      }
      samples = values.length;// - firstSamplePosition;
      String[]samples = new String[ComputeZScoreUnit.samples];  // the names of all samples
      HashSet<String> normalSamples = new HashSet<String>();
      
      // make set of case IDs of normal samples 
      for(int i=firstSamplePosition;i<values.length;i++){
         if(isNormal(values[i])){
            normalSamples.add(truncatedSampleName(values[i]));
         }
         samples[i] = truncatedSampleName(values[i]);
      }
      
      // list normal samples (cases) at start of output
      out.print("GeneSymbol\t");
      for(int i=firstSamplePosition;i<samples.length;i++) {
         if(!normalSamples.contains(samples[i])) {
            out.print(samples[i]+"\t");
         }
      }
      out.println();
      ComputeZScoreUnit.samples = ComputeZScoreUnit.samples -normalSamples.size()-firstSamplePosition;
      System.out.println(file+")\t"+ ComputeZScoreUnit.samples +" samples ("+normalSamples.size()+" normal)");
      
      // discards second line from expr file: should be: "Composite Element REF  signal   ... "
      in.readLine();
      
      String line;
      int genes = 0;
      int genesWithValidScores = 0;
      int genesFound=0;
      int rowsWithSomeDiploidCases = 0;
      
      // process expression file
      while((line = in.readLine())!=null){
         
         values = line.split("\t");

         // todo: instead of assuming 1st column is geneID, use last column 
         String id = values[0];  // geneID in 1st column
         
         // ignore gene's data if its copy number status is unknown
         // TODO: fix, as this really isn't right; if there are some normal samples, then we shouldn't need CN status 
         if(geneCopyNumberStatus.containsKey(id)){
            genesFound++;

            ArrayList<String[]> tumorSampleExpressions = new ArrayList<String[]>();
            for(int i=firstSamplePosition;i<values.length;i++){
               if(!normalSamples.contains(samples[i])){
                  String[] p = new String[2];
                  p[0] = samples[i];
                  p[1] = values[i];
                  tumorSampleExpressions.add(p);
               }
            }
            
            ArrayList<String[]> cnStatus = geneCopyNumberStatus.get(id);
            double[]zscores = getZscore(tumorSampleExpressions,cnStatus);
            
            if(zscores != null){
               rowsWithSomeDiploidCases++;
               out.print(id+"\t");

               for(int k =0;k<zscores.length;k++) {
                  // TODO: 9999 indicates an invalid exp value; make a constant
                  if(zscores[k] != 9999){
                     // limit precision
                     out.format( "%.4f\t", zscores[k] );
                  } else{
                     out.print("NA\t");
                  }
               }
               out.println();
               genesWithValidScores++;
            }else{
               out.print(id+"\t");
               for(int k =0;k< ComputeZScoreUnit.samples;k++) {
                  out.print("NA\t");
               }
               out.println();
            }
            genes++;
         }else{
            out.print(id+"\t");
            for(int k =0;k< ComputeZScoreUnit.samples;k++) {
               out.print("NA\t");
            }
            out.println();
         }
      }
      if( 0 == genesFound ){
         System.err.println( "No genes in expression file '" + file + "' found in copy number file." );
      }
      if( 0 == rowsWithSomeDiploidCases ){
         System.err.println( "No diploid cases found. Check copy number file." );
      }
      out.close();
   }
   
   /**
    * given array lists of expression and copy number data for a set of cases for one gene
    * return array of z-Scores for the expression data
    * 
    * assumes that cases appear in same sequence in cn and exp data
    * 
    * @param exp arrayList of String[] = [ sampleID, value ] 
    * @param cn same
    * @return array of z-Scores for the expression data; null if there were no diploid values
    */
   private static double[] getZscore(ArrayList<String[]> xp, ArrayList<String[]> cn){
      double[]z = null;
      double[]diploid = new double[samples];
      HashSet<String> diploidSamples = new HashSet<String>();

      for(int i=0;i<cn.size();i++){
         
         if(cn.get(i)[1].equals("0")) { // CN value of 0 indicates diploid; todo, make a constant
            diploidSamples.add(cn.get(i)[0]);  // entry [0] is the sampleID; todo, put in a named record (class)
         }
      }
      int xPos = 0;
      int count = 0;

      // for each expression measurement
      for(int i=0;i<xp.size();i++){
         // if the sample is diploid
         if(diploidSamples.contains(xp.get(i)[0])){
            count++;
            // and the expression value is not NA or NaN or null
            if(xp.get(i)[1].compareTo("NA")!=0 && xp.get(i)[1].compareTo("NaN")!=0 
                              && xp.get(i)[1].compareTo("null")!=0){
               // then add the measurement to the array of diploid values
               diploid[xPos++] = Double.parseDouble(xp.get(i)[1]);
            }
         }
      }

      // if there are some diploid values
      if(xPos != 0){
         // todo: do we want to do this if there's only 1 diploid value? std will then be 0, and zScores infinite
         // remove empty elements at end of diploid
         diploid = resize(diploid,xPos);
         // get mean and s.d. of elements of diploid
         double avg = avg(diploid);
         double std = std(diploid, avg);
         // create an array of z-Scores
         z = getZ(xp,avg,std);   
      }
      return z;
   }
   
   private static double[] getZ(ArrayList<String[]> xp, double avg, double std){
      double[]z = new double[xp.size()];
      for(int i=0;i<xp.size();i++){
         if(xp.get(i)[1].compareTo("NA")!=0 && xp.get(i)[1].compareTo("NaN")!=0 
            && xp.get(i)[1].compareTo("null")!=0){
               double s = Double.parseDouble(xp.get(i)[1]);
               s = s - avg;
               s = s/std;  // todo: could div by 0
               z[i] = s;
         } else {
            z[i] = 9999;
         }
      }
      return z;
   }
   
   
   /**
   * Read the copy number file and generate copy number status table
   * returns: HashMap<String,ArrayList<String[]>> that
   * maps geneName -> ArrayList< [ sample, value ] >  // but sample and value are same thing, albeit offset!
   */
   private static HashMap<String,ArrayList<String[]>> readCopyNumberFile(String file) throws IOException{
   
      HashMap<String,ArrayList<String[]>> map = new HashMap<String,ArrayList<String[]>>();
      
      BufferedReader in = new BufferedReader(new FileReader(file));
      
      // assumes single header line
      String header = in.readLine();
      String[]values = header.split("\t");
      
      // todo: this code duplicates above up through 'genes = 0'; combine
      int firstSamplePosition = 0;
      search:
      for(int i=0;i<values.length;i++){
         // todo: won't work with junky jan 2011 tumor case bar codes.
         if(values[i].indexOf("TCGA") != -1){
            firstSamplePosition = i;
            break search;
         }
      }
      samples = values.length; // - firstSamplePosition;
      String[]samples = new String[ComputeZScoreUnit.samples];
      for(int i=firstSamplePosition;i<values.length;i++){
         samples[i] = truncatedSampleName(values[i]);
      }
      System.out.println(file+")\t"+(ComputeZScoreUnit.samples -firstSamplePosition)+" samples");
      
      int genes = 0;
      String line;
      while((line=in.readLine())!=null){
         values = line.split("\t");
         String id = values[0];
         // todo: instead of assuming 1st column is geneID, use last column 
         if(!map.containsKey(id)){
            
            ArrayList<String[]> tmp = new ArrayList<String[]>(); 
            for(int i = firstSamplePosition;i<values.length;i++){
               String[] p = new String[2];
               p[0] = samples[i];
               p[1] = values[i];
               tmp.add(p);
            }
            map.put(id,tmp);  
         genes++;
         }
      }
      System.out.println(file+")\t"+genes+" GENES");
      if( map.size() == 0 ){
         System.err.println( "ComputeZScoreUnit: No gene IDs in copy number file.");
      }
      return map;
   }
   
   /**
   * Return the truncated version of a TCGA sample name
   * same as /([^-]*\-[^-]*\-[^-]*)/; return $1;
   */
   private static String truncatedSampleName(String name){
      String truncatedName = "";
      int dash = 0;
      for(int i=0;i<name.length();i++){
         if(name.charAt(i)=='-') {
            dash++;
         }
         if(dash == 3) {
            return truncatedName;
         } else {
            truncatedName=truncatedName+name.charAt(i);
         }
      }
      return truncatedName;
   }
   
   /**
   * Check if a sample name corresponds to normal samples
   * I.e., tests for normal tissue samples in TCGA case ID barcodes
    # The main parts of the barcode are TCGA-xx-xxxx-xxx-xxx-xxxx-xx
    # (1)-(2)-(3)-(4)(5)-(6)(7)-(8)-(9)
    #  ...
    #  (4) Sample Type = e.g. solid tumor (01) or normal blood (10)
    #  ...
    # 
    # The different sample types for (4) are:
    #  ...
    # 11  normal tissue (not always matched to a cancer patient, used for mRNA, microRNA, methylation) 
   */
   public static boolean isNormal(String name){
      String suffix = "";
      int dash = 0;
      search:
      for(int i=0;i<name.length();i++){
         if(name.charAt(i)=='-') {
            dash++;
         }
         if(dash == 3){
            suffix = name.substring(i,name.length());
            break search;
         }
      }
      if(suffix.indexOf("-11") == 0 ) {
         return true;
      }
      return false;
   }
   
   
   private static double avg(double[]v){
      double avg = 0;
      for(int i=0;i<v.length;i++) {
         avg=avg+v[i];
      }
      avg=avg/(double)v.length;
      return avg;
   }
   
   private static double std(double[]v,double avg){
      double std = 0;
      for(int i=0;i<v.length;i++) {
         std=std+Math.pow((v[i]-avg),2);
      }
      std=std/(double)(v.length-1);
      std=Math.sqrt(std);
      return std;
   }
   
   private static double[] resize(double[]v, int s){
      double[]tmp = new double[s];
      for(int i=0;i<s;i++) {
         tmp[i]=v[i];
      }
      return tmp;
   }

}