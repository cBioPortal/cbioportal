package org.mskcc.portal.test.model;

import static org.junit.Assert.*;

import java.util.EnumSet;

import junit.framework.Assert;

import org.junit.Test;
import org.mskcc.portal.model.GeneticEventImpl;
import org.mskcc.portal.model.GeneticEventImpl.CNA;
import org.mskcc.portal.model.GeneticEventImpl.MRNA;
import org.mskcc.portal.model.GeneticEventImpl.mutations;

public class TestGeneticEventImpl {

   @Test
   public void testGeneticEventImplIntIntBoolean() {

      GeneticEventImpl aGeneticEventImpl = new GeneticEventImpl( 2, 1, true );
         Assert.assertEquals( CNA.amplified, aGeneticEventImpl.getCnaValue() );
         Assert.assertEquals( true, aGeneticEventImpl.isCnaAmplified() );
         Assert.assertEquals( false, aGeneticEventImpl.isCnaHeterozygousDeleted() );
         Assert.assertEquals( false, aGeneticEventImpl.isCnaHomozygouslyDeleted() );
         
         Assert.assertEquals( MRNA.upRegulated, aGeneticEventImpl.getMrnaValue() );
         Assert.assertEquals( true, aGeneticEventImpl.isMRNAUpRegulated() );
         Assert.assertEquals( false, aGeneticEventImpl.isMRNADownRegulated() );
   
         Assert.assertEquals( mutations.Mutated, aGeneticEventImpl.getMutationValue() );
         Assert.assertEquals( true, aGeneticEventImpl.isMutated() );

      aGeneticEventImpl = new GeneticEventImpl( 0, 0, false );
         Assert.assertEquals( CNA.diploid, aGeneticEventImpl.getCnaValue() );
         Assert.assertEquals( false, aGeneticEventImpl.isCnaAmplified() );
         Assert.assertEquals( false, aGeneticEventImpl.isCnaHeterozygousDeleted() );
         Assert.assertEquals( false, aGeneticEventImpl.isCnaHomozygouslyDeleted() );
         
         Assert.assertEquals( MRNA.Normal, aGeneticEventImpl.getMrnaValue() );
         Assert.assertEquals( false, aGeneticEventImpl.isMRNAUpRegulated() );
         Assert.assertEquals( false, aGeneticEventImpl.isMRNADownRegulated() );
   
         Assert.assertEquals( mutations.UnMutated, aGeneticEventImpl.getMutationValue() );
         Assert.assertEquals( false, aGeneticEventImpl.isMutated() );

         try {
            aGeneticEventImpl = new GeneticEventImpl( 3, 2, true );
            fail("Should throw IllegalArgumentException");
         } catch (IllegalArgumentException e) {
            Assert.assertEquals("Illegal cnaValue: 3", e.getMessage() );
         }

         try {
            aGeneticEventImpl = new GeneticEventImpl( 1, 2, true );
            fail("Should throw IllegalArgumentException");
         } catch (IllegalArgumentException e) {
            Assert.assertEquals("Illegal mrnaValue: 2", e.getMessage() );
         }

   }
   
}
