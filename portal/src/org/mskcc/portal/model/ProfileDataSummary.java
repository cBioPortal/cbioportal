package org.mskcc.portal.model;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;

import org.mskcc.portal.oncoPrintSpecLanguage.GeneticTypeLevel;
import org.mskcc.portal.oncoPrintSpecLanguage.GeneWithSpec;
import org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintSpecification;
import org.mskcc.portal.util.ValueParser;

/**
 * Utility Class for Summarizing Profile Data.
 */
public class ProfileDataSummary {
    private HashMap<String, Double> geneAlteredMap = new HashMap<String, Double>();
    private HashMap<String, Double> geneMutatedMap = new HashMap<String, Double>();
    private HashMap<String, EnumMap<GeneticTypeLevel,Double>> geneCNALevelMap
                = new HashMap<String, EnumMap<GeneticTypeLevel,Double>>();
    private HashMap<String, double[]> geneMRNAUpDownMap = new HashMap<String, double[]>();
    private HashMap<String, Integer> numCasesAlteredMap = new HashMap<String, Integer>();
    private ArrayList<GeneWithScore> geneAlteredList = new ArrayList<GeneWithScore>();
    private HashMap<String, Boolean> caseAlteredMap = null; //lazy init
    private HashMap<String,HashMap<String,ValueParser>> mapGeneCaseParser = 
            new HashMap<String,HashMap<String,ValueParser>>();
    private double percentOfCasesWithAlteredPathway;
    private ProfileData profileData;
    private double zScoreThreshold;
    private OncoPrintSpecification theOncoPrintSpecification;
    private int numCasesAffected;
    
    /**
     * Calculate summary statistics for the cancer profiles in data, as filtered 
     * through theOncoPrintSpecification and the 
     * zScoreThreshold.
     * 
     * @param data
     * @param theOncoPrintSpecification
     * @param zScoreThreshold
     */
    public ProfileDataSummary(ProfileData data, 
            OncoPrintSpecification theOncoPrintSpecification,  
            double zScoreThreshold) {
        this.profileData = data;
        this.theOncoPrintSpecification = theOncoPrintSpecification;
        this.zScoreThreshold = zScoreThreshold;
        ArrayList<String> geneList = data.getGeneList();
        ArrayList<String> caseList = data.getCaseIdList();
        geneAlteredList = determineFrequencyOfGeneAlteration(geneList, caseList);
    }

    /**
     * Gets the embedded Profile Data Object.
     *
     * @return ProfileData Object.
     */
    public ProfileData getProfileData() {
        return this.profileData;
    }

    /**
     * Gets the list of gene ordered by frequency of alteration.
     *
     * @return ArrayList of GeneWithScore Objects.
     */
    public ArrayList<GeneWithScore> getGeneFrequencyList() {
        return geneAlteredList;
    }

    /**
     * Gets percent of cases where gene X is altered.
     *
     * @param gene gene symbol.
     * @return percentage value.
     */
    public double getPercentCasesWhereGeneIsAltered(String gene) {
        return geneAlteredMap.get(gene);
    }

    /**
     * Gene Number of cases where gene X is altered.
     *
     * @param gene gene symbol.
     * @return percentage value.
     */
    public int getNumCasesWhereGeneIsAltered(String gene) {
        return numCasesAlteredMap.get(gene);
    }

    /**
     * Does this case contain an alteration in at least one gene in the set.
     *
     * @param caseId case ID.
     * @return true or false.
     */
    public boolean isCaseAltered(String caseId) {
        determineFrequencyOfCaseAlteration();
        return caseAlteredMap.get(caseId);
    }

    /**
     * Gets percent of cases that contain an alteration in at least one gene in the set.
     *
     * @return percent value.
     */
    public double getPercentCasesAffected() {
        determineFrequencyOfCaseAlteration();
        return this.percentOfCasesWithAlteredPathway;
    }

    /**
     * Gets number of cases that contain an alteration in at least one gene in the set.
     * 
     * @return number of cases.
     */
    public int getNumCasesAffected() {
        determineFrequencyOfCaseAlteration();
        return this.numCasesAffected;
    }
    
    private ValueParser getValueParser(String gene, String caseId) {
        HashMap<String,ValueParser> mapCaseParser = mapGeneCaseParser.get(gene);
        if (mapCaseParser==null) {
            mapCaseParser = new HashMap<String,ValueParser>();
            mapGeneCaseParser.put(gene, mapCaseParser);
        }
        
        ValueParser parser = mapCaseParser.get(caseId);
        if (parser==null) {
            String value = profileData.getValue(gene, caseId);
            parser = ValueParser.generateValueParser(gene, value, 
                    this.zScoreThreshold, this.theOncoPrintSpecification);
            mapCaseParser.put(caseId, parser);
        }
        return parser;
        
    }

    /**
     * Determines if the gene X is altered in case Y.
     *
     * @param gene   gene symbol.
     * @param caseId case Id.
     * @return true or false.
     */
    public boolean isGeneAltered(String gene, String caseId) {
        ValueParser parser = getValueParser(gene, caseId);
        if( null != parser ){
           return parser.isGeneAltered();
        }
        return false;
    }

    /**
     * Determines if the gene X is mutated in case Y.
     *
     * @param gene   gene symbol.
     * @param caseId case Id.
     * @return true or false.
     */
    public boolean isGeneMutated(String gene, String caseId) {
        ValueParser parser = getValueParser(gene, caseId);
        if( null != parser ){
           return parser.isMutated();
        }
        return false;
    }
    
    /**
     * Get the CNA level of gene X in case Y.
     *
     * @param gene   gene symbol.
     * @param caseId case Id.
     * @return CNV level.
     */
    public GeneticTypeLevel getCNALevel(String gene, String caseId) {
        ValueParser parser = getValueParser(gene, caseId);
        if( null != parser ){
           if (parser.isCnaAmplified()) {
               return GeneticTypeLevel.Amplified;
           } else if (parser.isCnaGained()) {
               return GeneticTypeLevel.Gained;
           } else if (parser.isCnaHemizygouslyDeleted()) {
               return GeneticTypeLevel.HemizygouslyDeleted;
           } else if (parser.isCnaHomozygouslyDeleted()) {
               return GeneticTypeLevel.HomozygouslyDeleted;
           }
        }
        return null;
    }
    
    /**
     * Determines if the gene X is mutated in case Y.
     *
     * @param gene   gene symbol.
     * @param caseId case Id.
     * @return true or false.
     */
    public boolean isMRNAWayUp(String gene, String caseId) {
        ValueParser parser = getValueParser(gene, caseId);
        if( null != parser ){
           return parser.isMRNAWayUp();
        }
        return false;
    }
    
    /**
     * Determines if the gene X is mutated in case Y.
     *
     * @param gene   gene symbol.
     * @param caseId case Id.
     * @return true or false.
     */
    public boolean isMRNAWayDown(String gene, String caseId) {
        ValueParser parser = getValueParser(gene, caseId);
        if( null != parser ){
           return parser.isMRNAWayDown();
        }
        return false;
    }
    
    /**
     * Gene percentage of cases where gene X is at a certain CNA level.
     *
     * @param gene gene symbol.
     * @return percentage value.
     */    
    public double getPercentCasesWhereGeneIsAtCNALevel(String gene, GeneticTypeLevel cnaLevel) {
        EnumMap<GeneticTypeLevel,Double> map = geneCNALevelMap.get(gene);
        if (map == null) {
            map = new EnumMap<GeneticTypeLevel,Double>(GeneticTypeLevel.class);
            for (GeneticTypeLevel level : GeneticTypeLevel.values()) {
                map.put(level, 0.0);
            }
            
            ArrayList<String> caseList = profileData.getCaseIdList();
            double delta = 1.0 / caseList.size();
            for (String caseId : caseList) {
                 GeneticTypeLevel level = getCNALevel(gene, caseId);
                 if (level!=null) {
                    map.put(level, map.get(level)+delta);
                 }
            }
        }
        
        return map.get(cnaLevel);
    }
    
    /**
     * Gene percentage of cases where gene X is up regulated.
     *
     * @param gene gene symbol.
     * @return percentage value.
     */
    public double getPercentCasesWhereMRNAIsUpRegulated(String gene) {
        return getPercentCasesWhereMRNAIsUpOrDownRegulated(gene, true);
    }
    
    /**
     * Gene percentage of cases where gene X is down regulated.
     *
     * @param gene gene symbol.
     * @return percentage value.
     */
    public double getPercentCasesWhereMRNAIsDownRegulated(String gene) {
        return getPercentCasesWhereMRNAIsUpOrDownRegulated(gene, false);
    }    
    
    
    /**
     * Gene percentage of cases where gene X is up/down regulated.
     *
     * @param gene gene symbol.
     * @param upOrDown true if up regulated; false if down regulated.
     * @return percentage value.
     */
    private double getPercentCasesWhereMRNAIsUpOrDownRegulated(String gene, boolean up) {
        if (geneMRNAUpDownMap.get(gene)==null) {
            int numSamplesWhereGeneIsUpRegulated = 0;
            int numSamplesWhereGeneIsDownRegulated = 0;
            ArrayList<String> caseList = profileData.getCaseIdList();
            for (String caseId : caseList) {
                if (isMRNAWayUp(gene, caseId)) {
                    numSamplesWhereGeneIsUpRegulated++;
                } else if (isMRNAWayDown(gene, caseId)) {
                    numSamplesWhereGeneIsDownRegulated++;
                }
            }
            geneMRNAUpDownMap.put(gene, new double[]{
                1.0*numSamplesWhereGeneIsUpRegulated/caseList.size(),
                1.0*numSamplesWhereGeneIsDownRegulated/caseList.size()
                }
            );
        }
        
        double[] percs = geneMRNAUpDownMap.get(gene);
        return up ? percs[0] : percs[1];
    } 
    
    
    /**
     * Gene percentage of cases where gene X is mutated.
     *
     * @param gene gene symbol.
     * @return percentage value.
     */
    public double getPercentCasesWhereGeneIsMutated(String gene) {
        if (geneMutatedMap.get(gene)==null) {
            int numSamplesWhereGeneIsMutated = 0;
            ArrayList<String> caseList = profileData.getCaseIdList();
            for (String caseId : caseList) {
                if (isGeneMutated(gene, caseId)) {
                    numSamplesWhereGeneIsMutated++;
                }
            }
            geneMutatedMap.put(gene, 1.0*numSamplesWhereGeneIsMutated/caseList.size());
        }
        
        return geneMutatedMap.get(gene);
    }    
    

    /**
     * Determines frequency with which each gene is altered.
     */
    private ArrayList<GeneWithScore> determineFrequencyOfGeneAlteration(ArrayList<String> geneList,
                                                                        ArrayList<String> caseList) {
        ArrayList<GeneWithScore> localGeneList = new ArrayList<GeneWithScore>();
        //  First, determine frequency with which each gene is altered.
        //  Iterate through all genes.
        for (String gene : geneList) {
            int numSamplesWhereGeneIsAltered = 0;

            //  Iterate through all cases.
            for (String caseId : caseList) {

                //  Determine if gene is altered in this case
                boolean isAltered = isGeneAltered(gene, caseId);

                //  If gene is altered in this case, increment counter.
                if (isAltered) {
                    numSamplesWhereGeneIsAltered++;
                }
            }
            double percent = numSamplesWhereGeneIsAltered / (double) caseList.size();
            geneAlteredMap.put(gene, percent);
            numCasesAlteredMap.put(gene, numSamplesWhereGeneIsAltered);
            GeneWithScore geneWithScore = new GeneWithScore();
            geneWithScore.setGene(gene);
            geneWithScore.setScore(percent);

            if (this.theOncoPrintSpecification.containsGene(gene) ) {
               // TODO: this isn't right, as the same gene could appear multiple times in a spec;
               // thus, we need a more complex way to find the given gene
               // for now, return the 1st one 
               GeneWithSpec aGeneWithSpec = this.theOncoPrintSpecification.getGeneWithSpec(gene); 
               geneWithScore.setaGeneWithSpec(aGeneWithSpec);
           }
            localGeneList.add(geneWithScore);
        }

        //  Sort genes, based on frequency of alteration.
        Collections.sort(localGeneList, new GeneWithScoreComparator());
        return localGeneList;
    }

    /**
     * Determines frequency with which each case contains an alteration in at
     * least one member of the gene set.
     */
    private void determineFrequencyOfCaseAlteration() {
        if (caseAlteredMap != null) {
            return;
        }
        
        caseAlteredMap = new HashMap<String, Boolean>();
        
        numCasesAffected = 0;

        ArrayList<String> caseList = profileData.getCaseIdList();
        ArrayList<String> geneList = profileData.getGeneList();
        
        //  Iterate through all cases
        for (String caseId : caseList) {
            boolean caseIsAltered = false;

            //  Iterate through all gene lists
            for (String gene : geneList) {
                boolean isAltered = isGeneAltered(gene, caseId);
                if (isAltered) {
                    caseIsAltered = true;
                }
            }
            if (caseIsAltered) {
                numCasesAffected++;
            }
            caseAlteredMap.put(caseId, caseIsAltered);
        }
        
        percentOfCasesWithAlteredPathway = numCasesAffected
                / (double) caseList.size();
    }
}

/**
 * Ranks Genes based on score.
 */
class GeneWithScoreComparator implements Comparator<Object> {

    /**
     * Ranks genes based on score.
     *
     * @param o0 GeneWithScore0
     * @param o1 GeneWithScore1
     * @return integer value.
     */
    public int compare(Object o0, Object o1) {
        GeneWithScore gene0 = (GeneWithScore) o0;
        GeneWithScore gene1 = (GeneWithScore) o1;
        double score0 = gene0.getScore();
        double score1 = gene1.getScore();
        if (score0 == score1) {
            return gene0.getGene().compareTo(gene1.getGene());
        } else {
            if (score0 < score1) {
                return +1;
            } else {
                return -1;
            }
        }
    }
}
