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
