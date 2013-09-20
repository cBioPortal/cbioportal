/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;

/**
 * 
 * Given expression and CNV data for a set of samples (patients), generate normalized expression values. 
 * Currently the input must be TCGA data, with samples identified by TCGA barcode identifiers.
 * 
 * Each gene is normalized separately. First, the expression distribution for unaltered copies of the 
 * gene is estimated by calculating the mean and variance of the expression values for samples in which 
 * the gene is diploid (as reported by the CNV data). We call this the unaltered distribution.
 * 
 * If the gene has no diploid samples, then its normalized expression is reported as NA. 
 * Otherwise, for every sample, the gene's normalized expression is reported as
 * 
 *  (r - mu)/sigma
 * 
 * where r is the raw expression value, and mu and sigma are the mean and standard deviation 
 * of the unaltered distribution, respectively.
 * 
 * The syntax is simple:
 * 
 * java NormalizeExpressionLevels <copy_number_file> <expression_file> <output_file> [<min_number_of_diploids>]
 * 
 * The output is written onto a file named "output_file"
 * 
 * Any number of columns may precede the data. However, the following must be satisfied: 
 * 
 * - the first column provides gene identifiers
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
 * 
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
 * 
 */
public class NormalizeExpressionLevels{

   static HashMap<Long, ArrayList<String[]>> geneCopyNumberStatus;
   static int SAMPLES;
   static String zScoresFile;
   static final int DEFAULT_MIN_NUM_DIPLOIDS = 10;
   static int MIN_NUM_DIPLOIDS = DEFAULT_MIN_NUM_DIPLOIDS;
   static final int MIN_NUM_ALLOWED_DIPLOIDS = 3;

	public static void main (String[]args){
		try {
			driver(args);
		}
		catch (RuntimeException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Helper function which allows routine to be
	 * called by other java code and get a return status code.
	 */
	public static void driver(String[] args) throws RuntimeException {

		// TODO, perhaps: use command line parser
		if( args.length != 3 && args.length != 4){
			fatalError( "incorrect number of arguments. Arguments should be '<copy_number_file> <expression_file> <output_file> [<min_number_of_diploids>]'." );
		}
		String copyNumberFile = args[0];
		String expressionFile = args[1];
		zScoresFile = args[2];
		if( args.length == 4){
			try {
				MIN_NUM_DIPLOIDS = Integer.parseInt(args[3] );
			} catch (NumberFormatException e) {
				fatalError( "incorrect arguments. 'min_number_of_diploids', was entered as " + args[3] + " but must be an integer." );
			}
			if( MIN_NUM_DIPLOIDS < MIN_NUM_ALLOWED_DIPLOIDS ){
				fatalError( "incorrect arguments. 'min_number_of_diploids', was entered as " + args[3] + " but must be at least " + MIN_NUM_ALLOWED_DIPLOIDS + "." );
			}
		}
      
		geneCopyNumberStatus = readCopyNumberFile(copyNumberFile);
		computeZScoreXP(expressionFile); 
	}
   
   private static void computeZScoreXP(String file){
      
      BufferedReader in = null;
      PrintWriter out = null;
      String NOT_AVAILABLE = "NA";
      
      try {
         out = new PrintWriter(new FileWriter( zScoresFile ));
      } catch (IOException e) {
         fatalError( "cannot open <output_file> '" + zScoresFile + "' for writing.");
      }
      try {
         in = new BufferedReader(new FileReader(file));
      } catch (FileNotFoundException e) {
         fatalError( "cannot read <expression_file> in '" + file + "'.");
      }
      String header;
      try {
         header = in.readLine();
         String[]values = header.split("\t");
         
         int firstSamplePosition = getFirstDataColumn( values );
         // catch error if no sample id contains SampleNamePrefix
         if( NO_POSITION == firstSamplePosition ){
            fatalError( "no sample id in <expression_file>, '" + file + "'.");
         }

         SAMPLES = values.length;// - firstSamplePosition;  
         String[]samples = new String[SAMPLES];  // the names of all samples
         HashSet<String> normalSamples = new HashSet<String>();
         
         // make set of case IDs of normal samples, and list of all truncated samples
         for(int i=firstSamplePosition; i<SAMPLES; i++){
            if(isNormal(values[i])){
               normalSamples.add(truncatedSampleName(values[i]));
            }
            samples[i] = truncatedSampleName(values[i]);
         }
         
         ArrayList<String> outputLine = new ArrayList<String>(); 

         // header contains only ids of tumor samples
         outputLine.add("Hugo_Symbol");
         outputLine.add("Entrez_Gene_Id");
         for(int i=firstSamplePosition;i<samples.length;i++)
            if(!normalSamples.contains(samples[i]))
               outputLine.add(samples[i]);
         out.println( join( outputLine, "\t") );

         // SAMPLES is number of tumors
         SAMPLES = SAMPLES-normalSamples.size()-firstSamplePosition;
         System.out.println(file+")\t"+SAMPLES+" SAMPLES ("+normalSamples.size()+" normals)");
         
         // discards second line from expr file: it should be: "Composite Element REF  signal   ... "
         // TODO: check that 2nd line does not contain data
         in.readLine();
         
         String line;
         int genes = 0;
         int genesWithValidScores = 0;
         int genesFound=0;
         int rowsWithSomeDiploidCases = 0;
         
         DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
         
         // process expression file
         while((line = in.readLine())!=null){
            
            values = line.split("\t");
                    
            CanonicalGene gene;
            if (values[1].matches("[0-9]+")) {
                gene = daoGeneOptimized.getGene(Long.parseLong(values[1]));
            } else {
                gene = daoGeneOptimized.getNonAmbiguousGene(values[0]);
            }

            if (gene==null) {
                continue;
            }
            
            // ignore gene's data if its copy number status is unknown
            if(geneCopyNumberStatus.containsKey(gene.getEntrezGeneId())){
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
               
               ArrayList<String[]> cnStatus = geneCopyNumberStatus.get(gene.getEntrezGeneId());
               double[] zscores = getZscore( gene.getEntrezGeneId(), tumorSampleExpressions, cnStatus );
               
               if(zscores != null){
                  rowsWithSomeDiploidCases++;
                  
                  outputLine.clear();
                  outputLine.add(gene.getHugoGeneSymbolAllCaps());
                  outputLine.add(Long.toString(gene.getEntrezGeneId()));

                  for(int k =0;k<zscores.length;k++)

                     // Double.NaN indicates an invalid expression value
                     if(zscores[k] != Double.NaN){
                        // limit precision
                        outputLine.add( String.format( "%.4f", zscores[k] ) );
                     }else{
                        outputLine.add( NOT_AVAILABLE );
                     }
                  out.println( join( outputLine, "\t") );
                  genesWithValidScores++;
               }else{
                  outputLine.clear();
                  outputLine.add(gene.getHugoGeneSymbolAllCaps());
                  outputLine.add(Long.toString(gene.getEntrezGeneId()));
                  for(int k =0;k<SAMPLES;k++)
                     outputLine.add( NOT_AVAILABLE );
                  out.println( join( outputLine, "\t") );
               }
               genes++;
            }else{
               outputLine.clear();
               outputLine.add(gene.getHugoGeneSymbolAllCaps());
               outputLine.add(Long.toString(gene.getEntrezGeneId()));
               for(int k =0;k<SAMPLES;k++)
                  outputLine.add( NOT_AVAILABLE );
               out.println( join( outputLine, "\t") );
            }
         }
         if( 0 == genesFound ){
            fatalError( "none of the genes in the expression file '" + file + "' were in the copy number file." );
         }
         if( 0 == rowsWithSomeDiploidCases ){
            fatalError( "no genes with at least " + MIN_NUM_DIPLOIDS + " diploid cases found. Check copy number file." );
         }
      } catch (IOException e) {
         fatalError( "cannot read from <expression_file> in '" + file + "'.");
      }
      out.close();
   }
   
   static int NO_POSITION = -1;
   private static int getFirstDataColumn( String[] values){
      // TODO: instead of guessing, we should normalizing
         for(int i=0;i<values.length;i++) {
             if (!values[i].equalsIgnoreCase("HUGO_SYMBOL")
                     && !values[i].equalsIgnoreCase("ENTREZ_GENE_ID")
                     && !values[i].equalsIgnoreCase("CYTOBAND")
                     && !values[i].equalsIgnoreCase("LOCUS")
                     && !values[i].equalsIgnoreCase("ID")) {
                 return i;
             }
         }
            
      
      return NO_POSITION;
      
   }
   
   /**
    * Given expression and copy number data for a set of cases for one gene
    * return array of z-Scores for the expression data.
    * 
    * @param exp ArrayList of String[] = [ sampleID, expression ] 
    * @param cn  ArrayList< [sampleID, copyNumber] >
    * @return array of z-Scores for the expression data; null if there were no diploid values
    */
   private static double[] getZscore(long id, ArrayList<String[]> xp, ArrayList<String[]> cn){
      double[] z = null;
      double[] diploid = new double[SAMPLES];
      HashSet<String> diploidSamples = new HashSet<String>();
      String DiploidSample = "0"; // CN value of 0 indicates diploid
      
      for(int i=0;i<cn.size();i++){
         
         if(cn.get(i)[1].equals( DiploidSample ))  
            diploidSamples.add(cn.get(i)[0]);  // entry [0] is the sampleID; TODO: put in a named record (class)
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
               try {
                  diploid[xPos++] = Double.parseDouble(xp.get(i)[1]);
               } catch (NumberFormatException e) {
                  fatalError( "expression value '" + xp.get(i)[1] + "' for gene " + id + " in sample " +  xp.get(i)[0] + " is not a floating point number." );
               }
            }
         }
      }

      // make sure there are enough diploid values to normalize to the distribution
      // perhaps TODO: also make sure that the distribution of diploids is close enough to normal
      if( MIN_NUM_DIPLOIDS <= xPos ){

         // remove empty elements at end of diploid
         diploid = resize(diploid,xPos);
         // get mean and s.d. of elements of diploid
         double avg = avg(diploid);
         double std = std(diploid, avg);
         
         // create an array of z-Scores
         // do not compute z-Score if std == 0
         // TODO: use some minimum threshold for std
         if( 0.0d < std ){
            z = getZ(id, xp, avg, std);
         }
      }
      return z;
   }
   
   public static double[] getZ(long id, ArrayList<String[]> xp, double avg, double std){
      double[]z = new double[xp.size()];
      
      if( 0.0d == std){
         // this should not happen
         fatalError( "cannot normalize relative to distribution with standard deviation of 0.0." );         
      }
      for(int i=0;i<xp.size();i++){
         if(xp.get(i)[1].compareTo("NA")!=0 && xp.get(i)[1].compareTo("NaN")!=0 
            && xp.get(i)[1].compareTo("null")!=0){
               double s;
               try {
                  s = Double.parseDouble(xp.get(i)[1]);
                  s = s - avg;
                  s = s/std;
                  z[i] = s;
               } catch (NumberFormatException e) {
                  fatalError( "expression value '" + xp.get(i)[1] + "' for gene " + id + " in sample " +  xp.get(i)[0] + " is not a floating point number." );
               }
            }
         else
            z[i] = Double.NaN;
      }
      return z;
   }
   
   
   /**
   * Read the copy number file and generate copy number status table
   * returns: HashMap<Long,ArrayList<String[]>> that
   * maps geneName -> ArrayList< [ sampleName, value ] >  
   */
   public static HashMap<Long,ArrayList<String[]>> readCopyNumberFile(String file){
   
      HashMap<Long,ArrayList<String[]>> map = new HashMap<Long,ArrayList<String[]>>();
      BufferedReader in = null;
      try {
         in = new BufferedReader(new FileReader(file));
      } catch (FileNotFoundException e) {
         fatalError( "cannot open copy number file '" + file + "' for reading.");
      }
      
      // assumes single header line
      String header;
      try {
         header = in.readLine();
         String[]values = header.split("\t");
         
         int firstSamplePosition = getFirstDataColumn( values );
         // error if no sample id contains SampleNamePrefix
         if( NO_POSITION == firstSamplePosition ){
            fatalError( "no sample id contains in <CopyNumberFile>, '" + file + "'.");
         }

         SAMPLES = values.length; // - firstSamplePosition; 
         String[]samples = new String[SAMPLES];
         HashSet<String> tempSamplesNames = new HashSet<String>(); 
         for(int i=firstSamplePosition;i<SAMPLES;i++){
            samples[i] = truncatedSampleName(values[i]);
            // error if sample name is duplicated in CNV file
            if( tempSamplesNames.contains(samples[i] ) ){
               fatalError( "multiple columns with same truncated sample id of " + samples[i] + " in <CopyNumberFile>, '" + file + "'.");               
            }
            tempSamplesNames.add( samples[i] );
         }
         System.out.println(file+")\t"+(SAMPLES-firstSamplePosition)+" SAMPLES");
         
         DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
         String line;
         while((line=in.readLine())!=null){
            values = line.split("\t");
            CanonicalGene gene;
            if (values[1].matches("[0-9]+")) {
                gene = daoGeneOptimized.getGene(Long.parseLong(values[1]));
            } else {
                gene = daoGeneOptimized.getNonAmbiguousGene(values[0]);
            }

            if (gene==null) {
                continue;
            }
            
            Long entrez = gene.getEntrezGeneId();
            
            if(!map.containsKey(entrez)){

               ArrayList<String[]> tmp = new ArrayList<String[]>(); 
               for(int i = firstSamplePosition;i<values.length;i++){
                  String[] p = new String[2];
                  p[0] = samples[i];
                  p[1] = values[i];
                  tmp.add(p);
               }
               map.put(entrez,tmp);  
            }else{
               // remove duplicate ids, and report a warning
               // TODO: this is a subtle bug; if a gene appears an even number of times in the input, then it doesn't appear in the output;
               // if it appears an odd number, then the last one appears in the output; fix by creating a list of dupes
               map.remove(entrez);
               warning( "duplicate entry for gene " + entrez + " in <CopyNumberFile>, '" + file + "'.");
            }
         }

         System.out.println(file+")\t"+ map.size() +" GENES");
         if( map.isEmpty() ){
            fatalError( "no gene IDs in copy number file '" + file + "'.");
         }
      } catch (IOException e) {
         fatalError( "cannot read copy number file '" + file + "'.");
      }
      return map;
   }
   
   /**
   * Return the truncated version of a TCGA sample name
   */
   private static String truncatedSampleName(String name){
      String truncatedName = "";
      int dash = 0;
      for(int i=0;i<name.length();i++){
         if(name.charAt(i)=='-')
            dash++;
         if(dash == 3)
            return truncatedName;
         else
            truncatedName=truncatedName+name.charAt(i);
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
         if(name.charAt(i)=='-')
            dash++;
         if(dash == 3){
            suffix = name.substring(i,name.length());
            break search;
         }
      }
      if(suffix.indexOf("-11") == 0 )
         return true;
      return false;
   }
   
   
   private static double avg(double[]v){
      double avg = 0;
      for(int i=0;i<v.length;i++)
         avg=avg+v[i];
      avg=avg/(double)v.length;
      return avg;
   }
   
   private static double std(double[]v,double avg){
      double std = 0;
      for(int i=0;i<v.length;i++)
         std=std+Math.pow((v[i]-avg),2);
      std=std/(double)(v.length-1);
      std=Math.sqrt(std);
      return std;
   }
   
   private static double[] resize(double[]v, int s){
      double[]tmp = new double[s];
      for(int i=0;i<s;i++)
         tmp[i]=v[i];
      return tmp;
   }

   private static void fatalError(String msg){
      throw new RuntimeException("NormalizeExpressionLevels: Fatal error: " + msg );
   }
   
   private static void warning(String msg){
      System.err.println( "NormalizeExpressionLevels: " + msg );
   }
   
   public static String join(Collection<String> s, String delimiter) {
      if (s.isEmpty()) return "";

      Iterator<String> iter = s.iterator();
      StringBuffer buffer = new StringBuffer(iter.next());
      while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
      return buffer.toString();
  }
   
}