/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.model;

import org.mskcc.cbio.portal.oncoPrintSpecLanguage.GeneticTypeLevel;
import org.mskcc.cbio.portal.util.ValueParser;

/**
 * Implementation of Genetic Event Interface.
 *
 * @author Ethan Cerami and Arthur Goldberg.
 */
public class GeneticEventImpl implements GeneticEvent {

   public enum CNA { AMPLIFIED, GAINED, DIPLOID, HEMIZYGOUSLYDELETED, HOMODELETED, NONE } 
   
   public enum MRNA { UPREGULATED, NORMAL, DOWNREGULATED, NOTSHOWN }

   public enum RPPA { UPREGULATED, NORMAL, DOWNREGULATED, NOTSHOWN }

   public enum mutations { MUTATED, UNMUTATED, NONE }

   private CNA cnaValue;
   private MRNA mrnaValue;
   private RPPA rppaValue;
   private mutations isMutated;
   private String mutationType; // mutation type
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
      cnaValue = CNA.NONE;
      if( valueParser == null ){
         System.err.println( "in GeneticEventImpl w null valueParser gene: " + gene + " case: "+ caseId);
         return;
      }
      
      GeneticTypeLevel theGeneticTypeLevel = valueParser.getCNAlevel();
      if( null != theGeneticTypeLevel ){
         switch( valueParser.getCNAlevel() ){
            case Amplified:
               cnaValue = CNA.AMPLIFIED;
               break;
            case Diploid:
               cnaValue = CNA.DIPLOID;
               break;
            case Gained:
               cnaValue = CNA.GAINED;
               break;
            case HemizygouslyDeleted:
               cnaValue = CNA.HEMIZYGOUSLYDELETED;
               break;
            case HomozygouslyDeleted:
               cnaValue = CNA.HOMODELETED;
               break;
         }
      }

      mrnaValue = MRNA.NOTSHOWN;
      if (valueParser.isMRNAWayUp()) {
         mrnaValue = MRNA.UPREGULATED;
      } else if (valueParser.isMRNAWayDown()) {
         mrnaValue = MRNA.DOWNREGULATED;
      }

      rppaValue = RPPA.NOTSHOWN;
      if (valueParser.isRPPAWayUp()) {
         rppaValue = RPPA.UPREGULATED;
      } else if (valueParser.isRPPAWayDown()) {
         rppaValue = RPPA.DOWNREGULATED;
      }
      
      isMutated = mutations.UNMUTATED;

	  mutationType = "Mutation cannot be determined";
      if (valueParser.isMutated()) {
         isMutated = mutations.MUTATED;
		 // get type
		 mutationType = valueParser.getMutationType();
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
     * Gets the RPPA Value.
     * 
     * @return RPPA Value.
     */
    public RPPA getRPPAValue() {
        return rppaValue;
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
     * Gets the mutation type (amino acid change).
     *
     * @return mutation Type.
     */
    public String getMutationType() {
		return mutationType;
    }

    /**
     * Constructor, to be used primarily by JUnit Tests.
     *
     * @cnaValue Copy Number Value, discretized -2, -1, 0, 1, 2.
     * @mrnaValue mRNA Value, discretized: -1, 0, 1
     */
    public GeneticEventImpl(int cnaValue, int mrnaValue, int rppaValue, boolean isMutated) {

      switch (cnaValue) {
         case 2 :
            this.cnaValue = CNA.AMPLIFIED;
            break;
         case 1 :
            this.cnaValue = CNA.GAINED;
            break;
         case 0 :
            this.cnaValue = CNA.DIPLOID;
            break;
         case -1 :
            this.cnaValue = CNA.HEMIZYGOUSLYDELETED;
            break;
         case -2 :
            this.cnaValue = CNA.HOMODELETED;
            break;
         default :
            throw new IllegalArgumentException("Illegal cnaValue: " + cnaValue);
      }

      switch (mrnaValue) {
         case 1 :
            this.mrnaValue = MRNA.UPREGULATED;
            break;
         case 0 :
            this.mrnaValue = MRNA.NORMAL;
            break;
         case -1 :
            this.mrnaValue = MRNA.DOWNREGULATED;
            break;
         default :
            throw new IllegalArgumentException("Illegal mrnaValue: "
                  + mrnaValue);
      }

      switch (rppaValue) {
         case 1 :
            this.rppaValue = RPPA.UPREGULATED;
            break;
         case 0 :
            this.rppaValue = RPPA.NORMAL;
            break;
         case -1 :
            this.rppaValue = RPPA.DOWNREGULATED;
            break;
         default :
            throw new IllegalArgumentException("Illegal mrnaValue: "
                  + rppaValue);
      }

      if (isMutated) {
         this.isMutated = mutations.MUTATED;
      } else {
         this.isMutated = mutations.UNMUTATED;
      }
   }

    public GeneticEventImpl(CNA cnaValue, MRNA mrnaValue, RPPA rppaValue, mutations isMutated) {

       this.cnaValue = cnaValue;
       this.mrnaValue = mrnaValue;
       this.rppaValue = rppaValue;
       this.isMutated = isMutated;
   }

    /**
     * Is the Gene Amplified at the Copy Number Level?
     *
     * @return true or false.
     */
    public boolean isCnaAmplified() {
        return(cnaValue == CNA.AMPLIFIED);
    }

    /**
     * Is the Gene Homozygously Deleted at the Copy Number Level?
     *
     * @return true or false.
     */
    public boolean isCnaHomozygouslyDeleted() {
       return(cnaValue == CNA.HOMODELETED);
    }

    /**
     * Is the Gene Heterozgously Deleted at the Copy Number Level?
     *
     * @return true or false.
     */
    public boolean isCnaHeterozygousDeleted() {
        return(cnaValue == CNA.HEMIZYGOUSLYDELETED);
    }

    /**
     * Is the Gene mRNA upregulated?
     *
     * @return true or false.
     */
    public boolean isMRNAUpRegulated() {
       return(mrnaValue == MRNA.UPREGULATED);
    }

    /**
     * Is the Gene mRNA down-regulated?
     *
     * @return true or false.
     */
    public boolean isMRNADownRegulated() {
       return(mrnaValue == MRNA.DOWNREGULATED);
    }

    /**
     * Is the Gene RPPA upregulated?
     *
     * @return true or false.
     */
    public boolean isRPPAUpRegulated() {
       return(rppaValue == RPPA.UPREGULATED);
    }

    /**
     * Is the Gene RPPA down-regulated?
     *
     * @return true or false.
     */
    public boolean isRPPADownRegulated() {
       return(rppaValue == RPPA.DOWNREGULATED);
    }

    /**
     * Is gene mutated.
     *
     * @return true or false.
     */
    public boolean isMutated() {
       return( isMutated == mutations.MUTATED);
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
