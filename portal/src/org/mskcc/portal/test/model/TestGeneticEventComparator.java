package org.mskcc.portal.test.model;


import java.util.ArrayList;
import java.util.EnumSet;

import junit.framework.Assert;

import org.junit.Test;
import org.mskcc.portal.model.GeneticEventComparator;
import org.mskcc.portal.model.GeneticEventImpl;
import org.mskcc.portal.model.GeneticEventImpl.CNA;
import org.mskcc.portal.model.GeneticEventImpl.MRNA;
import org.mskcc.portal.model.GeneticEventImpl.mutations;

public class TestGeneticEventComparator {

   @Test
   public void testGeneticEventComparator() {
      GeneticEventComparator aGeneticEventComparator = new GeneticEventComparator();

      GeneticEventImpl ge1 = new GeneticEventImpl( 1, 1, true );
      testReflexiveEquals( aGeneticEventComparator, ge1, new Integer(0), false );

      GeneticEventImpl ge2 = new GeneticEventImpl( 1, 1, true );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, true );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 0 );

      ge1 = new GeneticEventImpl( 0, 1, true );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 1 );
      
      ge1 = new GeneticEventImpl( 2, 1, true );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, -1 );
      
      ge1 = new GeneticEventImpl( 1, 0, true );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 1 );
      
      ge1 = new GeneticEventImpl( 1, 1, false );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 1 );
      
   }

   @Test
   public void testGeneticEventComparatorArrayListOfEnumSetOfCNAArrayListOfEnumSetOfMRNAArrayListOfEnumSetOfmutations() {

      ArrayList<EnumSet<CNA>> CNAsortOrder = new ArrayList<EnumSet<CNA>>();
      CNAsortOrder.add(EnumSet.of(CNA.amplified));
      CNAsortOrder.add(EnumSet.of(CNA.homoDeleted));
      CNAsortOrder.add(EnumSet.of(CNA.Gained, CNA.diploid, CNA.HemizygouslyDeleted));
      CNAsortOrder.add(EnumSet.of(CNA.None));

      GeneticEventComparator aGeneticEventComparator = new GeneticEventComparator(
            CNAsortOrder,
            GeneticEventComparator.defaultMRNASortOrder(),
            GeneticEventComparator.defaultRPPASortOrder(),
            GeneticEventComparator.defaultMutationsSortOrder());

      GeneticEventImpl ge1 = new GeneticEventImpl( CNA.amplified, MRNA.Normal, mutations.Mutated );

      Assert.assertEquals( false, aGeneticEventComparator.equals(ge1, new Integer(0)) );

      GeneticEventImpl ge2 = new GeneticEventImpl( CNA.amplified, MRNA.Normal, mutations.Mutated );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, true );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 0 );

      ge2 = new GeneticEventImpl( CNA.homoDeleted, MRNA.Normal, mutations.Mutated );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, -1 );
      
      ge1 = new GeneticEventImpl( CNA.Gained, MRNA.Normal, mutations.Mutated );
      ge2 = new GeneticEventImpl( CNA.diploid, MRNA.Normal, mutations.Mutated );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, true );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 0 );

      ge2 = new GeneticEventImpl( CNA.homoDeleted, MRNA.Normal, mutations.Mutated );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 1 );

      ge2 = new GeneticEventImpl( CNA.homoDeleted, MRNA.Normal, mutations.UnMutated );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 1 );
      
      ge2 = new GeneticEventImpl( CNA.diploid, MRNA.upRegulated, mutations.UnMutated );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 1 );
      
      ge2 = new GeneticEventImpl( CNA.HemizygouslyDeleted, MRNA.downRegulated, mutations.UnMutated );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, -1 );
      
   }
   
   private void testReflexiveCompare( GeneticEventComparator aGeneticEventComparator, GeneticEventImpl ge1, GeneticEventImpl ge2, int expected ){
      Assert.assertEquals( expected, aGeneticEventComparator.compare(ge1, ge2) );
      Assert.assertEquals( -expected, aGeneticEventComparator.compare(ge2, ge1) );
   }
   
   private void testReflexiveEquals( GeneticEventComparator aGeneticEventComparator, Object ge1, Object ge2, boolean expected ){
      Assert.assertEquals( expected, aGeneticEventComparator.equals(ge1, ge2) );
      Assert.assertEquals( expected, aGeneticEventComparator.equals(ge2, ge1) );
   }
}
