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

package org.mskcc.cbio.portal.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;

import org.mskcc.cbio.portal.model.GeneticEventImpl.CNA;
import org.mskcc.cbio.portal.model.GeneticEventImpl.MRNA;
import org.mskcc.cbio.portal.model.GeneticEventImpl.RPPA;
import org.mskcc.cbio.portal.model.GeneticEventImpl.mutations;

/**
 * Provides configurable comparison and equals of GeneticEventImpls. 
 * @author arthur goldberg
 */
public class GeneticEventComparator implements Comparator<Object>{

   private ArrayList<EnumSet<CNA>> cnaSortOrder = new ArrayList<EnumSet<CNA>>();
   private ArrayList<EnumSet<MRNA>> mRnaSortOrder = new ArrayList<EnumSet<MRNA>>();
   private ArrayList<EnumSet<RPPA>> rppaSortOrder = new ArrayList<EnumSet<RPPA>>();
   private ArrayList<EnumSet<mutations>> mutationsSortOrder = new ArrayList<EnumSet<mutations>>();
   
   private HashMap<CNA,Integer> cnaSortOrderHash = new HashMap<CNA,Integer>();  // load this from cnaSortOrder
   private HashMap<MRNA, Integer> mrnaSortOrderHash = new HashMap<MRNA,Integer>();  // load this from mRnaSortOrder
   private HashMap<RPPA, Integer> rppaSortOrderHash = new HashMap<RPPA,Integer>();  // load this from mRnaSortOrder
   private HashMap<mutations,Integer> mutationsSortOrderHash =
        new HashMap<mutations,Integer>();  // load this from mutationsSortOrder
   
   /*
    * default sort order
    */
   public GeneticEventComparator( ){
      defaultSortOrder();
   }
   
   public GeneticEventComparator( ArrayList<EnumSet<CNA>> CNAsortOrder,
         ArrayList<EnumSet<MRNA>> MRNAsortOrder, 
         ArrayList<EnumSet<RPPA>> RPPAsortOrder, 
         ArrayList<EnumSet<mutations>> mutationsSortOrder ){
      this.cnaSortOrder = CNAsortOrder;
      this.mRnaSortOrder = MRNAsortOrder;
      this.rppaSortOrder = RPPAsortOrder;
      this.mutationsSortOrder = mutationsSortOrder; 
      initSortOrder();
   }
   
   private void defaultSortOrder(){
      //System.out.println( "defaultSortOrder: " );
      int val = 1;
      // all CNAs
      for (CNA aCNA : CNA.values()){
         //System.out.println( aCNA + ": " + val );
         cnaSortOrderHash.put(aCNA, new Integer( val++ ) );
      }

      // all MRNAs
      val = 1;
      for (MRNA aMRNA : MRNA.values()){
         mrnaSortOrderHash.put(aMRNA, new Integer( val++ ) );
      }

      // all RPPAs
      val = 1;
      for (RPPA aRPPA : RPPA.values()){
         rppaSortOrderHash.put(aRPPA, new Integer( val++ ) );
      }
      
      // all mutations
      val = 1;
      for (mutations aMutations : mutations.values()){
         mutationsSortOrderHash.put(aMutations, new Integer( val++ ) );
      }
   }
   
   // default sort orders are useful for calling the constructor
   public static ArrayList<EnumSet<CNA>> defaultCNASortOrder() {
      ArrayList<EnumSet<CNA>> CNAsortOrder = new ArrayList<EnumSet<CNA>>();
      // all CNAs
      for (CNA aCNA : CNA.values()) {
         CNAsortOrder.add(EnumSet.of(aCNA));
      }
      return CNAsortOrder;
   }

   public static ArrayList<EnumSet<MRNA>> defaultMRNASortOrder() {
      ArrayList<EnumSet<MRNA>> MRNAsortOrder = new ArrayList<EnumSet<MRNA>>();
      // all MRNAs
      for (MRNA aMRNA : MRNA.values()) {
         MRNAsortOrder.add(EnumSet.of(aMRNA));
      }
      return MRNAsortOrder;
   }

   public static ArrayList<EnumSet<RPPA>> defaultRPPASortOrder() {
      ArrayList<EnumSet<RPPA>> RPPAsortOrder = new ArrayList<EnumSet<RPPA>>();
      // all MRNAs
      for (RPPA aRPPA : RPPA.values()) {
         RPPAsortOrder.add(EnumSet.of(aRPPA));
      }
      return RPPAsortOrder;
   }

   public static ArrayList<EnumSet<mutations>> defaultMutationsSortOrder() {
      ArrayList<EnumSet<mutations>> mutationsSortOrder = new ArrayList<EnumSet<mutations>>();
      // all mutations
      for (mutations aMutations : mutations.values()) {
         mutationsSortOrder.add(EnumSet.of(aMutations));
      }
      return mutationsSortOrder;
   }   
   
   private void initSortOrder(){
      // verify that all elements of each enumeration are provided; otherwise compare() or equals() will die
      // CNA
      int val = 1;
      for( EnumSet<CNA> aCNAset: cnaSortOrder){
         for( CNA aCNA : aCNAset){
            cnaSortOrderHash.put(aCNA, new Integer(val) );
            //System.out.println( aCNA + ": " + val );
         }
         val++;
      }

      // verify all CNAs
      for (CNA aCNA : CNA.values()){
         if( !cnaSortOrderHash.containsKey(aCNA) ){
            throw new IllegalArgumentException("CNA sets missing: " + aCNA );
         }
      }

      // MRNA
      val = 1;
      for( EnumSet<MRNA> aMRNAset: mRnaSortOrder){
         for( MRNA aMRNA : aMRNAset){
            mrnaSortOrderHash.put(aMRNA, new Integer(val) );
            //System.out.println( aMRNA + ": " + val );
         }
         val++;
      }
      // verify all MRNAs
      for (MRNA aMRNA : MRNA.values()){
         if( !mrnaSortOrderHash.containsKey(aMRNA) ){
            throw new IllegalArgumentException("MRNA sets missing: " + aMRNA );
         }
      }

      // RPPA
      val = 1;
      for( EnumSet<RPPA> aRPPAset: rppaSortOrder){
         for( RPPA aRPPA : aRPPAset){
            rppaSortOrderHash.put(aRPPA, new Integer(val) );
            //System.out.println( aMRNA + ": " + val );
         }
         val++;
      }
      // verify all RPPAs
      for (RPPA aRPPA : RPPA.values()){
         if( !rppaSortOrderHash.containsKey(aRPPA) ){
            throw new IllegalArgumentException("RPPA sets missing: " + aRPPA );
         }
      }
      
      // mutations
      val = 1;
      for( EnumSet<mutations> aMutationsSet: mutationsSortOrder){
         for( mutations aMutations : aMutationsSet){
            mutationsSortOrderHash.put( aMutations, new Integer(val) );
            //System.out.println( aMutations + ": " + val );
         }
         val++;
      }
      // verify all mutations
      for (mutations aMutations : mutations.values()){
         if( !mutationsSortOrderHash.containsKey(aMutations) ){
            throw new IllegalArgumentException("mutations sets missing: " + aMutations );
         }
      }

   }

   public int compare(Object o1, Object o2) {
      if( !( o1 instanceof GeneticEventImpl && o2 instanceof GeneticEventImpl )){
         throw new IllegalArgumentException("objects not GeneticEventImpl" );
      }
      GeneticEventImpl ge1 = (GeneticEventImpl) o1;
      GeneticEventImpl ge2 = (GeneticEventImpl) o2;
      
      // for now, significance of data types is CNA, expression, mutation
      // CNA
      if( cnaSortOrderHash.get( ge1.getCnaValue() ).intValue() < cnaSortOrderHash.get( ge2.getCnaValue() ).intValue() ){
         return -1;
      }
      if( cnaSortOrderHash.get( ge1.getCnaValue() ).intValue() > cnaSortOrderHash.get( ge2.getCnaValue() ).intValue() ){
         return 1;
      }
      
      // MRNA
      if( mrnaSortOrderHash.get( ge1.getMrnaValue() ).intValue() < mrnaSortOrderHash.get
              ( ge2.getMrnaValue() ).intValue() ){
         return -1;
      }
      if( mrnaSortOrderHash.get( ge1.getMrnaValue() ).intValue() > mrnaSortOrderHash.get
              ( ge2.getMrnaValue() ).intValue() ){
         return 1;
      }
      
      // RPPA
      if( rppaSortOrderHash.get( ge1.getRPPAValue() ).intValue() < rppaSortOrderHash.get
              ( ge2.getRPPAValue() ).intValue() ){
         return -1;
      }
      if( rppaSortOrderHash.get( ge1.getRPPAValue() ).intValue() > rppaSortOrderHash.get
              ( ge2.getRPPAValue() ).intValue() ){
         return 1;
      }
      
      // mutations
      if( mutationsSortOrderHash.get( ge1.getMutationValue() ).intValue() < mutationsSortOrderHash.get
              ( ge2.getMutationValue() ).intValue() ){
         return -1;
      }
      if( mutationsSortOrderHash.get( ge1.getMutationValue() ).intValue() > mutationsSortOrderHash.get
              ( ge2.getMutationValue() ).intValue() ){
         return 1;
      }
      
      return 0;
   }
   
   public boolean equals(Object obj1, Object obj2 ) {
      if (obj1 instanceof GeneticEvent && obj2 instanceof GeneticEvent ) {
         GeneticEvent ge1 = (GeneticEvent) obj2;
         GeneticEvent ge2 = (GeneticEvent) obj1;
         
         // CNA
         if( cnaSortOrderHash.get( ge1.getCnaValue() ).intValue() != cnaSortOrderHash.get
                 ( ge2.getCnaValue() ).intValue() ){
            return false;
         }
         
         // MRNA
         if( mrnaSortOrderHash.get( ge1.getMrnaValue() ).intValue() != mrnaSortOrderHash.get
                 ( ge2.getMrnaValue() ).intValue() ){
            return false;
         }
         
         // RPPA
         //System.out.println(ge1.getRPPAValue());
         //System.out.println(rppaSortOrderHash.get( ge1.getRPPAValue() ));
         //System.out.println(rppaSortOrderHash.get( ge1.getRPPAValue() ).intValue());
         if( rppaSortOrderHash.get( ge1.getRPPAValue() ).intValue() != rppaSortOrderHash.get
                 ( ge2.getRPPAValue() ).intValue() ){
            return false;
         }
         
         // mutations
         if( mutationsSortOrderHash.get( ge1.getMutationValue() ).intValue() != mutationsSortOrderHash.get
                 ( ge2.getMutationValue() ).intValue() ){
            return false;
         }
         return true;
      }
      return false;
   }
}