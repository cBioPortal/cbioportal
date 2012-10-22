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
import java.util.EnumSet;
import junit.framework.Assert;
import org.junit.Test;
import org.mskcc.cbio.portal.model.GeneticEventComparator;
import org.mskcc.cbio.portal.model.GeneticEventImpl;
import org.mskcc.cbio.portal.model.GeneticEventImpl.CNA;
import org.mskcc.cbio.portal.model.GeneticEventImpl.MRNA;
import org.mskcc.cbio.portal.model.GeneticEventImpl.RPPA;
import org.mskcc.cbio.portal.model.GeneticEventImpl.mutations;

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
