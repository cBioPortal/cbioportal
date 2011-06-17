package org.mskcc.portal.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;

import org.mskcc.portal.model.GeneticEventImpl.CNA;
import org.mskcc.portal.model.GeneticEventImpl.MRNA;
import org.mskcc.portal.model.GeneticEventImpl.mutations;

/**
 * Provides configurable comparison and equals of GeneticEventImpls. 
 * @author arthur goldberg
 */
public class GeneticEventComparator implements Comparator<Object>{

   ArrayList<EnumSet<CNA>> CNAsortOrder = new ArrayList<EnumSet<CNA>>(); 
   ArrayList<EnumSet<MRNA>> MRNAsortOrder  = new ArrayList<EnumSet<MRNA>>();
   ArrayList<EnumSet<mutations>> mutationsSortOrder = new ArrayList<EnumSet<mutations>>();
   
   HashMap<CNA,Integer> CNAsortOrderHash = new HashMap<CNA,Integer>();  // load this from CNAsortOrder
   HashMap<MRNA, Integer> MRNAsortOrderHash = new HashMap<MRNA,Integer>();  // load this from MRNAsortOrder
   HashMap<mutations,Integer> mutationsSortOrderHash = new HashMap<mutations,Integer>();  // load this from mutationsSortOrder
   
   /*
    * default sort order
    */
   public GeneticEventComparator( ){
      defaultSortOrder();
   }
   
   /*
    * custom sort order
    * 
    * equal and compare both use a set of HashMaps that map from cancer genomic data type to a rank integer that
    * identifies the order of importance of values within each type.
    * for now, the importance of data types is CNA, expression, mutation, but this could be made general too.
    * 
    * for each data type, caller provides a list in the constructor with the sort order, from least to greatest, with equivalent values in an EnumSet
    * for example, a caller could indicate that 
    * CNA has the sort order is Amplified, then HomozygouslyDeleted, then any of (PartlyAmplified, Diploid, HemizygouslyDeleted)
    * and that MRNA and mutations have the default order:  

      ArrayList<EnumSet<CNA>> CNAsortOrder = new ArrayList<EnumSet<CNA>>();
      ArrayList<EnumSet<MRNA>> MRNAsortOrder = new ArrayList<EnumSet<MRNA>>();
      ArrayList<EnumSet<mutations>> mutationsSortOrder = new ArrayList<EnumSet<mutations>>();

      CNAsortOrder.add(EnumSet.of(CNA.Amplified));
      CNAsortOrder.add(EnumSet.of(CNA.HomozygouslyDeleted));
      CNAsortOrder.add(EnumSet.of(CNA.PartlyAmplified, CNA.Diploid, CNA.HemizygouslyDeleted));
      
      // all MRNAs
      for (MRNA aMRNA : MRNA.values()){
         MRNAsortOrder.add(EnumSet.of(aMRNA));
      }
      
      // all mutations
      for (mutations aMutations : mutations.values()){
         mutationsSortOrder.add(EnumSet.of( aMutations ));
      }

      GeneticEventComparator aGeneticEventComparator = new GeneticEventComparator( 
            CNAsortOrder, MRNAsortOrder, mutationsSortOrder);

    */
   public GeneticEventComparator( ArrayList<EnumSet<CNA>> CNAsortOrder, 
         ArrayList<EnumSet<MRNA>> MRNAsortOrder, ArrayList<EnumSet<mutations>> mutationsSortOrder ){
      this.CNAsortOrder = CNAsortOrder; 
      this.MRNAsortOrder = MRNAsortOrder; 
      this.mutationsSortOrder = mutationsSortOrder; 
      initSortOrder();
   }
   
   private void defaultSortOrder(){
      //System.out.println( "defaultSortOrder: " );
      int val = 1;
      // all CNAs
      for (CNA aCNA : CNA.values()){
         //System.out.println( aCNA + ": " + val );
         CNAsortOrderHash.put(aCNA, new Integer( val++ ) );
      }

      // all MRNAs
      val = 1;
      for (MRNA aMRNA : MRNA.values()){
         MRNAsortOrderHash.put(aMRNA, new Integer( val++ ) );
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

   public static ArrayList<EnumSet<mutations>> defaultMutationsSortOrder() {
      ArrayList<EnumSet<mutations>> mutationsSortOrder = new ArrayList<EnumSet<mutations>>();
      // all mutations
      for (mutations aMutations : mutations.values()) {
         mutationsSortOrder.add(EnumSet.of(aMutations));
      }
      return mutationsSortOrder;
   }   
   
   private void initSortOrder(){
      // TODO: MAKE MORE COMPACT, with reflection or generics
      
      // verify that all elements of each enumeration are provided; otherwise compare() or equals() will die
      // CNA
      int val = 1;
      for( EnumSet<CNA> aCNAset: CNAsortOrder){
         for( CNA aCNA : aCNAset){
            CNAsortOrderHash.put(aCNA, new Integer(val) );
            //System.out.println( aCNA + ": " + val );
         }
         val++;
      }

      // verify all CNAs
      for (CNA aCNA : CNA.values()){
         if( !CNAsortOrderHash.containsKey(aCNA) ){
            throw new IllegalArgumentException("CNA sets missing: " + aCNA );
         }
      }

      // MRNA
      val = 1;
      for( EnumSet<MRNA> aMRNAset: MRNAsortOrder){
         for( MRNA aMRNA : aMRNAset){
            MRNAsortOrderHash.put(aMRNA, new Integer(val) );
            //System.out.println( aMRNA + ": " + val );
         }
         val++;
      }
      // verify all MRNAs
      for (MRNA aMRNA : MRNA.values()){
         if( !MRNAsortOrderHash.containsKey(aMRNA) ){
            throw new IllegalArgumentException("MRNA sets missing: " + aMRNA );
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
      if( CNAsortOrderHash.get( ge1.getCnaValue() ).intValue() < CNAsortOrderHash.get( ge2.getCnaValue() ).intValue() ){
         return -1;
      }
      if( CNAsortOrderHash.get( ge1.getCnaValue() ).intValue() > CNAsortOrderHash.get( ge2.getCnaValue() ).intValue() ){
         return 1;
      }
      
      // MRNA
      if( MRNAsortOrderHash.get( ge1.getMrnaValue() ).intValue() < MRNAsortOrderHash.get( ge2.getMrnaValue() ).intValue() ){
         return -1;
      }
      if( MRNAsortOrderHash.get( ge1.getMrnaValue() ).intValue() > MRNAsortOrderHash.get( ge2.getMrnaValue() ).intValue() ){
         return 1;
      }
      
      // mutations
      if( mutationsSortOrderHash.get( ge1.getMutationValue() ).intValue() < mutationsSortOrderHash.get( ge2.getMutationValue() ).intValue() ){
         return -1;
      }
      if( mutationsSortOrderHash.get( ge1.getMutationValue() ).intValue() > mutationsSortOrderHash.get( ge2.getMutationValue() ).intValue() ){
         return 1;
      }
      
      return 0;
   }
   
   public boolean equals(Object obj1, Object obj2 ) {
      if (obj1 instanceof GeneticEvent && obj2 instanceof GeneticEvent ) {
         GeneticEvent ge1 = (GeneticEvent) obj2;
         GeneticEvent ge2 = (GeneticEvent) obj1;
         
         // CNA
         if( CNAsortOrderHash.get( ge1.getCnaValue() ).intValue() != CNAsortOrderHash.get( ge2.getCnaValue() ).intValue() ){
            return false;
         }
         
         // MRNA
         if( MRNAsortOrderHash.get( ge1.getMrnaValue() ).intValue() != MRNAsortOrderHash.get( ge2.getMrnaValue() ).intValue() ){
            return false;
         }
         
         // mutations
         if( mutationsSortOrderHash.get( ge1.getMutationValue() ).intValue() != mutationsSortOrderHash.get( ge2.getMutationValue() ).intValue() ){
            return false;
         }
         return true;
      }
      return false;
   }
   
}
