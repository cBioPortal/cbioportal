package org.mskcc.cbio.portal.test.model;

import static org.junit.Assert.*;

import java.util.EnumSet;

import junit.framework.Assert;

import org.junit.Test;
import org.mskcc.cbio.portal.model.GeneticEventImpl;
import org.mskcc.cbio.portal.model.GeneticEventImpl.CNA;
import org.mskcc.cbio.portal.model.GeneticEventImpl.MRNA;
import org.mskcc.cbio.portal.model.GeneticEventImpl.mutations;

public class TestGeneticEventImpl {

   @Test
   public void testGeneticEventImplIntIntBoolean() {

      GeneticEventImpl aGeneticEventImpl = new GeneticEventImpl( 2, 1, 1, true );
         Assert.assertEquals( CNA.AMPLIFIED, aGeneticEventImpl.getCnaValue() );
         Assert.assertEquals( true, aGeneticEventImpl.isCnaAmplified() );
         Assert.assertEquals( false, aGeneticEventImpl.isCnaHeterozygousDeleted() );
         Assert.assertEquals( false, aGeneticEventImpl.isCnaHomozygouslyDeleted() );
         
         Assert.assertEquals( MRNA.UPREGULATED, aGeneticEventImpl.getMrnaValue() );
         Assert.assertEquals( true, aGeneticEventImpl.isMRNAUpRegulated() );
         Assert.assertEquals( false, aGeneticEventImpl.isMRNADownRegulated() );
   
         Assert.assertEquals( mutations.MUTATED, aGeneticEventImpl.getMutationValue() );
         Assert.assertEquals( true, aGeneticEventImpl.isMutated() );

      aGeneticEventImpl = new GeneticEventImpl( 0, 0, 0, false );
         Assert.assertEquals( CNA.DIPLOID, aGeneticEventImpl.getCnaValue() );
         Assert.assertEquals( false, aGeneticEventImpl.isCnaAmplified() );
         Assert.assertEquals( false, aGeneticEventImpl.isCnaHeterozygousDeleted() );
         Assert.assertEquals( false, aGeneticEventImpl.isCnaHomozygouslyDeleted() );
         
         Assert.assertEquals( MRNA.NORMAL, aGeneticEventImpl.getMrnaValue() );
         Assert.assertEquals( false, aGeneticEventImpl.isMRNAUpRegulated() );
         Assert.assertEquals( false, aGeneticEventImpl.isMRNADownRegulated() );
   
         Assert.assertEquals( mutations.UNMUTATED, aGeneticEventImpl.getMutationValue() );
         Assert.assertEquals( false, aGeneticEventImpl.isMutated() );

         try {
            aGeneticEventImpl = new GeneticEventImpl( 3, 2, 2, true );
            fail("Should throw IllegalArgumentException");
         } catch (IllegalArgumentException e) {
            Assert.assertEquals("Illegal cnaValue: 3", e.getMessage() );
         }

         try {
            aGeneticEventImpl = new GeneticEventImpl( 1, 2, 2, true );
            fail("Should throw IllegalArgumentException");
         } catch (IllegalArgumentException e) {
            Assert.assertEquals("Illegal mrnaValue: 2", e.getMessage() );
         }

   }
   
}
