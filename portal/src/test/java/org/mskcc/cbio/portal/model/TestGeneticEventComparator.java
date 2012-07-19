package org.mskcc.cbio.portal.test.model;


import java.util.ArrayList;
import java.util.EnumSet;
import junit.framework.Assert;
import org.junit.Test;
import org.mskcc.portal.model.GeneticEventComparator;
import org.mskcc.portal.model.GeneticEventImpl;
import org.mskcc.portal.model.GeneticEventImpl.CNA;
import org.mskcc.portal.model.GeneticEventImpl.MRNA;
import org.mskcc.portal.model.GeneticEventImpl.RPPA;
import org.mskcc.portal.model.GeneticEventImpl.mutations;

public class TestGeneticEventComparator {

   @Test
   public void testGeneticEventComparator() {
      GeneticEventComparator aGeneticEventComparator = new GeneticEventComparator();

      GeneticEventImpl ge1 = new GeneticEventImpl( 1, 1, 1,true );
      testReflexiveEquals( aGeneticEventComparator, ge1, new Integer(0), false );

      GeneticEventImpl ge2 = new GeneticEventImpl( 1, 1, 1, true );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, true );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 0 );

      ge1 = new GeneticEventImpl( 0, 1, 1, true );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 1 );
      
      ge1 = new GeneticEventImpl( 2, 1, 1, true );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, -1 );
      
      ge1 = new GeneticEventImpl( 1, 0, 0, true );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 1 );
      
      ge1 = new GeneticEventImpl( 1, 1, 0, false );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 1 );
      
   }

   @Test
   public void testGeneticEventComparatorArrayListOfEnumSetOfCNAArrayListOfEnumSetOfMRNAArrayListOfEnumSetOfmutations() {

      ArrayList<EnumSet<CNA>> CNAsortOrder = new ArrayList<EnumSet<CNA>>();
      CNAsortOrder.add(EnumSet.of(CNA.AMPLIFIED));
      CNAsortOrder.add(EnumSet.of(CNA.HOMODELETED));
      CNAsortOrder.add(EnumSet.of(CNA.GAINED, CNA.DIPLOID, CNA.HEMIZYGOUSLYDELETED));
      CNAsortOrder.add(EnumSet.of(CNA.NONE));

      GeneticEventComparator aGeneticEventComparator = new GeneticEventComparator(
            CNAsortOrder,
            GeneticEventComparator.defaultMRNASortOrder(),
            GeneticEventComparator.defaultRPPASortOrder(),
            GeneticEventComparator.defaultMutationsSortOrder());

      GeneticEventImpl ge1 = new GeneticEventImpl( CNA.AMPLIFIED, MRNA.NORMAL, RPPA.NORMAL, mutations.MUTATED );

      Assert.assertEquals( false, aGeneticEventComparator.equals(ge1, new Integer(0)) );

      GeneticEventImpl ge2 = new GeneticEventImpl( CNA.AMPLIFIED, MRNA.NORMAL, RPPA.NORMAL, mutations.MUTATED );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, true );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 0 );

      ge2 = new GeneticEventImpl( CNA.HOMODELETED, MRNA.NORMAL, RPPA.NORMAL, mutations.MUTATED );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, -1 );
      
      ge1 = new GeneticEventImpl( CNA.GAINED, MRNA.NORMAL, RPPA.NORMAL, mutations.MUTATED );
      ge2 = new GeneticEventImpl( CNA.DIPLOID, MRNA.NORMAL, RPPA.NORMAL, mutations.MUTATED );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, true );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 0 );

      ge2 = new GeneticEventImpl( CNA.HOMODELETED, MRNA.NORMAL, RPPA.NORMAL, mutations.MUTATED );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 1 );

      ge2 = new GeneticEventImpl( CNA.HOMODELETED, MRNA.NORMAL, RPPA.NORMAL, mutations.UNMUTATED );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 1 );
      
      ge2 = new GeneticEventImpl( CNA.DIPLOID, MRNA.UPREGULATED, RPPA.UPREGULATED, mutations.UNMUTATED );
      testReflexiveEquals( aGeneticEventComparator, ge1, ge2, false );
      testReflexiveCompare( aGeneticEventComparator, ge1, ge2, 1 );
      
      ge2 = new GeneticEventImpl( CNA.HEMIZYGOUSLYDELETED, MRNA.DOWNREGULATED, RPPA.DOWNREGULATED, mutations.UNMUTATED );
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
