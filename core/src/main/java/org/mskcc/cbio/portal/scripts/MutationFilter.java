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

package org.mskcc.cbio.portal.scripts;

import java.util.HashSet;
import java.util.Set;

import org.mskcc.cbio.portal.model.ExtendedMutation;

/**
 * Filter mutations as they're imported into the CGDS dbms.
 * <p>
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class MutationFilter {
   
   private Set<Long> whiteListGenesForPromoterMutations;

   private int accepts=0;
   private int germlineWhitelistAccepts=0;
   private int somaticWhitelistAccepts=0;
   private int unknownAccepts=0;
   public int decisions=0;
   private int silentOrIntronRejects=0;
   private int mutationStatusNoneRejects=0;
   private int lohOrWildTypeRejects=0;
   private int emptyAnnotationRejects=0;
   private int missenseGermlineRejects=0;
	private int utrRejects=0;
	public int igrRejects=0;
	private int redactedRejects=0;

   /**
    * Construct a MutationFilter with no white lists. 
    * This filter will 
    * <br>
    * REJECT Silent, LOH, Intron and Wildtype mutations, and
    * <br>
    * KEEP all other mutations.
    */
   public MutationFilter() throws IllegalArgumentException{
      whiteListGenesForPromoterMutations = new HashSet<Long>();
      whiteListGenesForPromoterMutations.add(Long.valueOf(7015)); // TERT
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
      
      // Do not accept mutations with Mutation_Status of None
      if (safeStringTest( mutation.getMutationStatus(), "None" )) {
          mutationStatusNoneRejects++;
          return false;
      }
            
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

		// Do not accept Redacted mutations
		if (safeStringTest(mutation.getValidationStatus(), "Redacted"))
		{
		   redactedRejects++;
		   return false;
		}

      // Do not accept 3'UTR or 5' UTR Mutations
      if( safeStringTest( mutation.getMutationType(), "3'UTR" ) ||
		  safeStringTest( mutation.getMutationType(), "3'Flank" ) ||
		  safeStringTest( mutation.getMutationType(), "5'UTR" ) ){
		  utrRejects++;
         return false;
      }
      
      if( safeStringTest( mutation.getMutationType(), "5'Flank" ) ) { 
            if (whiteListGenesForPromoterMutations.contains(mutation.getEntrezGeneId())){
                  mutation.setProteinChange("Promoter");
            } else {
		  utrRejects++;
                  return false;
            }
      }

      // Do not accept IGR Mutations
      if( safeStringTest( mutation.getMutationType(), "IGR" ) ){
		  igrRejects++;
         return false;
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
     * Provide number of REJECT decisions for UTR Mutations.
     * @return number of REJECT decisions for UTR Mutations.
     */
   public int getUTRRejects() {
       return this.utrRejects;
   }

    /**
     * Provide number of REJECT decisions for IGR Mutations.
     * @return number of REJECT decisions for IGR Mutations.
     */
   public int getIGRRejects() {
       return this.igrRejects;
   }

    public int getMutationStatusNoneRejects() {
        return mutationStatusNoneRejects;
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

	public int getRedactedRejects()
	{
		return this.redactedRejects;
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
            "\nMutation Status 'None' Rejects:  " + this.getMutationStatusNoneRejects() +
            "\nSilent or Intron Rejects:  " + this.getSilentOrIntronRejects() +
		  "\nUTR Rejects:  " + this.getUTRRejects() +
		  "\nIGR Rejects:  " + this.getIGRRejects() +
            "\nLOH or Wild Type Rejects:  " + this.getLohOrWildTypeRejects() +
            "\nEmpty Annotation Rejects:  " + this.getEmptyAnnotationRejects() +
            "\nMissense Germline Rejects:  " + this.getMissenseGermlineRejects();
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
      return( sb.toString() );
   }
}