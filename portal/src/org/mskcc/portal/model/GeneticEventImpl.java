package org.mskcc.portal.model;

import org.mskcc.portal.oncoPrintSpecLanguage.GeneticTypeLevel;
import org.mskcc.portal.util.ValueParser;

/**
 * Implementation of Genetic Event Interface.
 *
 * @author Ethan Cerami and Arthur Goldberg.
 */
public class GeneticEventImpl implements GeneticEvent {

   public enum CNA { amplified, Gained, diploid, HemizygouslyDeleted, homoDeleted, None } // some values have lower case names to match filenames of icons that represent them
   
   public enum MRNA { upRegulated, Normal, downRegulated, notShown } // as above, some values have lower case names to match filenames of icons that represent them

   public enum mutations { Mutated, UnMutated, None }

   private CNA cnaValue;
   private MRNA mrnaValue;
   private mutations isMutated;
   private String gene;
   private String caseId;
   
   private GeneticEventComparator geneticEventComparator = null;

   /**
    * Constructor.
    * 
    */
   public GeneticEventImpl(ValueParser valueParser, String gene, String caseId) {
      this.gene = gene;
      this.caseId = caseId;
      
      /*
       * was:

      // TODO: this might be wrong! what should be the default?
      cnaValue = CNA.Diploid;
      if (valueParser.isCnaAmplified()) {
         cnaValue = CNA.Amplified;
      } else if (valueParser.isCnaHemizygouslyDeleted()) {
         cnaValue = CNA.HemizygouslyDeleted;
      } else if (valueParser.isCnaHomozygouslyDeleted()) {
         cnaValue = CNA.HomozygouslyDeleted;
      }
      */
      cnaValue = CNA.None;
      if( valueParser == null ){
         System.err.println( "in GeneticEventImpl w null valueParser gene: " + gene + " case: "+ caseId);
         return;
      }
      
      GeneticTypeLevel theGeneticTypeLevel = valueParser.getCNAlevel();
      if( null != theGeneticTypeLevel ){
         switch( valueParser.getCNAlevel() ){
            case Amplified:
               cnaValue = CNA.amplified;
               break;
            case Diploid:
               cnaValue = CNA.diploid;
               break;
            case Gained:
               cnaValue = CNA.Gained;
               break;
            case HemizygouslyDeleted:
               cnaValue = CNA.HemizygouslyDeleted;
               break;
            case HomozygouslyDeleted:
               cnaValue = CNA.homoDeleted;
               break;
         }
      }

      mrnaValue = MRNA.notShown;
      if (valueParser.isMRNAWayUp()) {
         mrnaValue = MRNA.upRegulated;
      } else if (valueParser.isMRNAWayDown()) {
         mrnaValue = MRNA.downRegulated;
      }
      
      isMutated = mutations.UnMutated;

      if (valueParser.isMutated()) {
         isMutated = mutations.Mutated;
      }
   }
   
   // TODO: really should be static, but then there's no place to store the geneticEventComparator; think of a work-around 
   public void setGeneticEventComparator( GeneticEventComparator geneticEventComparator ){
      this.geneticEventComparator = geneticEventComparator;
   }

    /**
     * Gets the CNA Value.
     *
     * @return cna Value.
     */
    public CNA getCnaValue() {
        return cnaValue;
    }

    /**
     * Gets the MRNA Value.
     * 
     * @return mRNA Value.
     */
    public MRNA getMrnaValue() {
        return mrnaValue;
    }

    /**
     * Gets the mutation Value.
     *
     * @return mutation Value.
     */
    public mutations getMutationValue() {
        return isMutated;
    }

    /**
     * Constructor, to be used primarily by JUnit Tests.
     *
     * @cnaValue Copy Number Value, discretized -2, -1, 0, 1, 2.
     * @mrnaValue mRNA Value, discretized: -1, 0, 1
     */
    public GeneticEventImpl(int cnaValue, int mrnaValue, boolean isMutated) {

      switch (cnaValue) {
         case 2 :
            this.cnaValue = CNA.amplified;
            break;
         case 1 :
            this.cnaValue = CNA.Gained;
            break;
         case 0 :
            this.cnaValue = CNA.diploid;
            break;
         case -1 :
            this.cnaValue = CNA.HemizygouslyDeleted;
            break;
         case -2 :
            this.cnaValue = CNA.homoDeleted;
            break;
         default :
            throw new IllegalArgumentException("Illegal cnaValue: " + cnaValue);
      }

      switch (mrnaValue) {
         case 1 :
            this.mrnaValue = MRNA.upRegulated;
            break;
         case 0 :
            this.mrnaValue = MRNA.Normal;
            break;
         case -1 :
            this.mrnaValue = MRNA.downRegulated;
            break;
         default :
            throw new IllegalArgumentException("Illegal mrnaValue: "
                  + mrnaValue);
      }

      if (isMutated) {
         this.isMutated = mutations.Mutated;
      } else {
         this.isMutated = mutations.UnMutated;
      }
   }

    public GeneticEventImpl(CNA cnaValue, MRNA mrnaValue, mutations isMutated) {

       this.cnaValue = cnaValue;
       this.mrnaValue = mrnaValue;
       this.isMutated = isMutated;
   }

    /**
     * Is the Gene Amplified at the Copy Number Level?
     *
     * @return true or false.
     */
    public boolean isCnaAmplified() {
        return(cnaValue == CNA.amplified);
    }

    /**
     * Is the Gene Homozygously Deleted at the Copy Number Level?
     *
     * @return true or false.
     */
    public boolean isCnaHomozygouslyDeleted() {
       return(cnaValue == CNA.homoDeleted);
    }

    /**
     * Is the Gene Heterozgously Deleted at the Copy Number Level?
     *
     * @return true or false.
     */
    public boolean isCnaHeterozygousDeleted() {
        return(cnaValue == CNA.HemizygouslyDeleted);
    }

    /**
     * Is the Gene mRNA upregulated?
     *
     * @return true or false.
     */
    public boolean isMRNAUpRegulated() {
       return(mrnaValue == MRNA.upRegulated);
    }

    /**
     * Is the Gene mRNA down-regulated?
     *
     * @return true or false.
     */
    public boolean isMRNADownRegulated() {
       return(mrnaValue == MRNA.downRegulated);
    }

    /**
     * Is gene mutated.
     *
     * @return true or false.
     */
    public boolean isMutated() {
       return( isMutated == mutations.Mutated);
    }

    /**
     * Gets the Gene.
     * @return Gene Symbol.
     */
    public String getGene() {
        return gene;
    }

    /**
     * Gets the Case ID.
     * @return case ID.
     */
    public String caseCaseId() {
        return caseId;
    }
    
    public boolean equals(Object obj) {
       if( geneticEventComparator != null ){
          return geneticEventComparator.equals(this, obj);
       }
       
       // TODO: should be different exception
       throw new IllegalArgumentException("Cannot execute GeneticEventImpl.equals, geneticEventComparator not set, call setGeneticEventComparator.");
   }

    @Override
    public String toString() {
        return (gene + ":" + cnaValue + ":" + mrnaValue + ":" + isMutated);
    }
}
