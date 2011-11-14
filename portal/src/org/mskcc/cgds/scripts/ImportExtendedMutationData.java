package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.HashMap;

/**
 * Import an extended mutation file.
 * Columns may be in any order.
 * <p>
 * @author Ethan Cerami
 * <br>
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 * <br>
 */
public class ImportExtendedMutationData{

    private ProgressMonitor pMonitor;
    private File mutationFile;
    private int geneticProfileId;
    private HashMap <String, Integer> headerMap;
    private static final String NOT_AVAILABLE = "NA";
    private MutationFilter myMutationFilter;
    
    /**
     * construct an ImportExtendedMutationData with no white lists.
     * Filter mutations according to the no argument MutationFilter().
     */
    public ImportExtendedMutationData(File mutationFile, int geneticProfileId, 
             ProgressMonitor pMonitor) {
       this.mutationFile = mutationFile;
       this.geneticProfileId = geneticProfileId;
       this.pMonitor = pMonitor;

       // create default MutationFilter
       myMutationFilter = new MutationFilter( );
   }
    
    /**
     * Construct an ImportExtendedMutationData with germline and somatic whitelists.
     * Filter mutations according to the 2 argument MutationFilter().
     */
    public ImportExtendedMutationData( File mutationFile, 
             int geneticProfileId, 
             ProgressMonitor pMonitor,
             String germline_white_list_file) throws IllegalArgumentException {
       this.mutationFile = mutationFile;
       this.geneticProfileId = geneticProfileId;
       this.pMonitor = pMonitor;

       // create MutationFilter
       myMutationFilter = new MutationFilter(germline_white_list_file);
   }

    public void importData() throws IOException, DaoException {
        HashSet <String> sequencedCaseSet = new HashSet<String>();

        FileReader reader = new FileReader(mutationFile);
        BufferedReader buf = new BufferedReader(reader);

        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        DaoCase daoCase = new DaoCase();
        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        DaoMutation daoMutation = DaoMutation.getInstance();
        
        //  The MAF File Changes fairly frequently, and we cannot use column index constants.
        String line = buf.readLine();
        line = line.trim();
        String[] headers = line.split("\t");
        headerMap = new HashMap<String, Integer>();
        for( int i=0; i<headers.length; i++) {
            String header = headers[i];
            headerMap.put(header, i);
        }

        boolean fileHasOMAData = false;

        try {
           
           // fail gracefully if a non-essential column is missing
           // e.g. if there is no MA_link.var column, we assume that the value is NA and insert it as such
            fileHasOMAData = true;
            pMonitor.setCurrentMessage("Extracting OMA Scores from Column Number:  "
                + getHeaderIndex( "MA:FImpact" ));
        } catch( IllegalArgumentException e) {
            fileHasOMAData = false;
        }

        line = buf.readLine();
        while( line != null) {
            if( pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            if( !line.startsWith("#") && line.trim().length() > 0) {
                String[] parts = line.split("\t", -1 ); // the -1 keeps trailing empty strings; see JavaDoc for String

                // process case id
                // an example bar code looks like this:  TCGA-13-1479-01A-01W
                String barCode = getField( parts, "Tumor_Sample_Barcode" );
                String barCodeParts[] = barCode.split("-");

                String caseId = null;
                try {
                    caseId = barCodeParts[0] + "-" + barCodeParts[1] + "-" + barCodeParts[2];
                } catch( ArrayIndexOutOfBoundsException e) {
                    caseId = barCode;
                }
                if( !daoCase.caseExistsInGeneticProfile(caseId, geneticProfileId)) {
   					daoCase.addCase(caseId, geneticProfileId);
   				}

                String center = getField( parts, "Center" );
                String sequencer = getField(parts, "Sequencer");
                String chr = getField( parts, "Chromosome");

                long startPosition = 0;
                try {
                    startPosition = Integer.parseInt( getField( parts, "Start_position"));
                } catch( NumberFormatException e) {
                    startPosition = 0;
                }

                long endPosition = 0;
                try {
                    endPosition = Integer.parseInt( getField( parts, "End_position"));
                } catch( NumberFormatException e) {
                    endPosition = 0;
                }
                
                String aminoAcidChange = "MUTATED";
                String mutationType = getField( parts, "Variant_Classification");
                String validationStatus = getField( parts, "Validation_Status");
                String mutationStatus = getField( parts, "Mutation_Status");
                String functionalImpactScore = "";
                String linkXVar = "";  
                String linkMsa = "";
                String linkPdb = "";
                if( fileHasOMAData) {
                   String[] aminoAcidChangeHeaders = { "amino_acid_change", "MA:variant" };
                    aminoAcidChange = getField( parts, aminoAcidChangeHeaders );
                    if( aminoAcidChange != null && aminoAcidChange.equalsIgnoreCase( NOT_AVAILABLE )) {
                        aminoAcidChange = "MUTATED";
                    }

                    functionalImpactScore = getField(parts, "MA:FImpact" );
                    functionalImpactScore = transformOMAScore(functionalImpactScore);
                    linkXVar = getField(parts, "MA:link.var" );  
                    linkXVar = linkXVar.replace("\"", "");  
                    linkMsa = getField(parts, "MA:link.MSA" );
                    linkPdb = getField(parts, "MA:link.PDB" );
                }

                //  Assume we are dealing with Entrez Gene Ids (this is the best / most stable option)
                String geneSymbol = getField(parts, "Hugo_Symbol" );
                String entrezGeneIdStr = getField(parts, "Entrez_Gene_Id");
                CanonicalGene gene = null;
                try {
                    long entrezGeneId = Long.parseLong(entrezGeneIdStr);
                    gene = daoGene.getGene(entrezGeneId);
                } catch(NumberFormatException e) {
                    pMonitor.logWarning("Entrez Gene ID not an integer: " + entrezGeneIdStr );
                }
                
                if(gene == null) {
                    // If Entrez Gene ID Fails, try Symbol.
                    gene = daoGene.getNonAmbiguousGene(geneSymbol);
                }
                    
                if(gene == null) {
                    pMonitor.logWarning("Gene not found:  " + geneSymbol + " ["
                            + entrezGeneIdStr + "]. Ignoring it "
                            + "and all mutation data associated with it!");
                } else {
                    ExtendedMutation mutation = new ExtendedMutation();
                    mutation.setGeneticProfileId(geneticProfileId);
                    mutation.setCaseId(caseId);
                    mutation.setGene(gene);
                    mutation.setSequencingCenter(center);
                    mutation.setSequencer(sequencer);
                    mutation.setAminoAcidChange(aminoAcidChange);
                    mutation.setMutationType(mutationType);
                    mutation.setChr(chr);
                    mutation.setStartPosition(startPosition);
                    mutation.setEndPosition(endPosition);
                    mutation.setValidationStatus(validationStatus);
                    mutation.setMutationStatus(mutationStatus);
                    mutation.setFunctionalImpactScore(functionalImpactScore);
                    mutation.setLinkXVar(linkXVar);  
                    mutation.setLinkPdb(linkPdb);
                    mutation.setLinkMsa(linkMsa);
                    sequencedCaseSet.add(caseId);

                    //  Filter out Mutations
                    if( myMutationFilter.acceptMutation( mutation )) {
                        // add record to db
                        daoMutation.addMutation(mutation);
                    }
                }
            }
            line = buf.readLine();
        }
        if( MySQLbulkLoader.isBulkLoad()) {
           daoGeneticAlteration.flushGeneticAlteration();
           daoMutation.flushMutations();
        }
        pMonitor.setCurrentMessage(myMutationFilter.getStatistics() );
           
    }
    
    private int getHeaderIndex( String headerName ) {
        if( headerMap.containsKey(headerName)) {
            return headerMap.get(headerName);
        } else {
            throw new IllegalArgumentException( "MAF file does not contain column:  " + headerName);
        }
    }
    
    // try one of several column names
    // TODO: one notification if the column isn't available
    private int getHeaderIndex( String[] possibleHeaderNames ) {       
       StringBuffer sb = new StringBuffer();
       
       for( String possibleHeader : possibleHeaderNames ){
          if( headerMap.containsKey( possibleHeader )) {
             return headerMap.get(possibleHeader);
          }
          sb.append( possibleHeader + ", " );
       }
       throw new IllegalArgumentException( "MAF file does not contain any of these columns:  "
               + sb.substring(0, sb.length() - 2 ) );
   }

    private String transformOMAScore( String omaScore) {
        if( omaScore == null || omaScore.length() ==0) {
            return omaScore;
        }
        if( omaScore.equalsIgnoreCase("H") || omaScore.equalsIgnoreCase("high")) {
            return "H";
        } else if( omaScore.equalsIgnoreCase("M") || omaScore.equalsIgnoreCase("medium")) {
            return "M";
        } else if( omaScore.equalsIgnoreCase("L") || omaScore.equalsIgnoreCase("low")) {
            return "L";
        } else if( omaScore.equalsIgnoreCase("N") || omaScore.equalsIgnoreCase("neutral")) {
            return "N";
        } else {
            return omaScore;
        }
    }

    // get the value for field colName
    private String getField( String parts[], String colName ) {
       String colNames[] = { colName };
       return getField( parts, colNames );
    }

    // get the value for field colName
    // return NOT_AVAILABLE if the column isn't available or the string is empty
    private String getField( String parts[], String possibleColNames[] ) {
        int index;
         try {
            index = getHeaderIndex( possibleColNames );
            String value;
            try {
               value = parts[index];
               if( value.trim().equals("")) {
                  return NOT_AVAILABLE;
              } else {
                  return value.trim();
              }
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
               return NOT_AVAILABLE;
            }
         } catch( IllegalArgumentException e) {
            return NOT_AVAILABLE;
         }
    }
    
    @Override
    public String toString(){
       return "geneticProfileId: " + this.geneticProfileId + "\n" +
          "mutationFile: " + this.mutationFile + "\n" +
          this.myMutationFilter.toString();
    }
}
