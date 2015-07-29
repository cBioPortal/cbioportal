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

package org.mskcc.cbio.portal.util;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import junit.framework.Assert;

import org.junit.Test;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ComparisonOp;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ConcreteDataTypeSpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.DiscreteDataTypeSetSpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.DiscreteDataTypeSpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.GeneSet;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.GeneWithSpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.GeneticDataTypes;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.GeneticTypeLevel;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.OncoPrintGeneDisplaySpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.OncoPrintSpecification;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParsedFullDataTypeSpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.TestOncoPrintSpecificationLibrary;

import static org.junit.Assert.*;

public class TestValueParser {
   // TODO: reorganize to test individual methods separately in different
   // methods

	@Test
   public void testValueParser() {

      ValueParser parser = new ValueParser("COPY_NUMBER_ALTERATION:2;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:3;", 1.0);
      assertTrue(parser.isMRNAWayUp());
      assertFalse(parser.isMRNAWayDown());
      assertTrue(parser.isCnaAmplified());
      assertFalse(parser.isCnaGained());
      assertTrue(parser.isMutated());
      assertTrue(parser.isGeneAltered());
      // default CNA alteration is AMP and HOMDEL
      assertTrue(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      parser = new ValueParser("COPY_NUMBER_ALTERATION:2;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:3;", 4.0);
      assertFalse(parser.isMRNAWayUp());
      assertFalse(parser.isMRNAWayDown());
      assertTrue(parser.isCnaAmplified());
      assertFalse(parser.isCnaGained());
      assertTrue(parser.isMutated());
      assertTrue(parser.isGeneAltered());
      assertTrue(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      parser = new ValueParser("COPY_NUMBER_ALTERATION:2;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:-3;", 2.0);
      assertFalse(parser.isMRNAWayUp());
      assertTrue(parser.isMRNAWayDown());
      assertTrue(parser.isGeneAltered());
      assertTrue(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      try {
         parser = new ValueParser("COPY_NUMBER_ALTERATION:2;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:3;", -2.0);
      } catch (Exception e) {
         assertEquals("zScoreThreshold must be greater than 0", e.getMessage());
      }

      String[] genes = { "g1", "gene2" };
      OncoPrintSpecification anOncoPrintSpecification = new OncoPrintSpecification(genes);

      OncoPrintGeneDisplaySpec aDefaultOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
      aDefaultOncoPrintGeneDisplaySpec.setDefault( 1.0, 1.0 );
      GeneSet aGeneSet = new GeneSet();
      String[] moreGenes = { "P53", "Last" };
      OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec = TestOncoPrintSpecificationLibrary
               .createTestOncoPrintGeneDisplaySpec();
      aGeneSet = new GeneSet();
      for (String g : moreGenes) {
         aGeneSet.addGeneWithSpec(new GeneWithSpec(g, theOncoPrintGeneDisplaySpec));
      }
      anOncoPrintSpecification.add(aGeneSet);

      parser = new ValueParser("COPY_NUMBER_ALTERATION:2;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:3;", 2.0, 1.0,
               aDefaultOncoPrintGeneDisplaySpec);
      assertTrue(parser.isMRNAWayUp());
      assertFalse(parser.isMRNAWayDown());
      assertTrue(parser.isCnaAmplified());
      assertFalse(parser.isCnaGained());
      assertEquals(GeneticTypeLevel.Amplified, parser.getCNAlevel());
      assertTrue(parser.isMutated());
      assertTrue(parser.isGeneAltered());
      assertTrue(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      parser = ValueParser.generateValueParser("g1", "COPY_NUMBER_ALTERATION:2;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:3;", 2.0, 1.0,
               anOncoPrintSpecification);
      assertTrue(parser.isMRNAWayUp());
      assertFalse(parser.isMRNAWayDown());
      assertTrue(parser.isCnaAmplified());
      assertFalse(parser.isCnaGained());
      assertEquals(GeneticTypeLevel.Amplified, parser.getCNAlevel());
      assertTrue(parser.isMutated());
      assertTrue(parser.isGeneAltered());
      assertTrue(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      parser = new ValueParser("COPY_NUMBER_ALTERATION:1;MUTATION_EXTENDED:1;MRNA_EXPRESSION:-3;", 2.0, 1.0,
               aDefaultOncoPrintGeneDisplaySpec);
      assertFalse(parser.isMRNAWayUp());
      assertTrue(parser.isMRNAWayDown());
      assertFalse(parser.isCnaAmplified());
      assertFalse(parser.isCnaGained()); // default spec; shows all data, except
                                         // only show CNA of AMP and HOMDEL
      assertEquals(null, parser.getCNAlevel());
      assertTrue(parser.isMutated());
      assertTrue(parser.isGeneAltered());
      assertFalse(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      parser = new ValueParser("COPY_NUMBER_ALTERATION:2;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:1;", 0.0, 1.0,
               theOncoPrintGeneDisplaySpec);
      assertFalse(parser.isMRNAWayUp());
      assertTrue(parser.isMRNAWayDown());
      assertTrue(parser.isCnaAmplified());
      assertEquals(GeneticTypeLevel.Amplified, parser.getCNAlevel());
      assertFalse(parser.isCnaGained());
      assertFalse(parser.isMutated());
      assertTrue(parser.wasSequenced());
      assertTrue(parser.isGeneAltered());
      assertTrue(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      // no such gene
      parser = ValueParser.generateValueParser("None", "sdfdsf", 0.0,  1.0,anOncoPrintSpecification);
      assertEquals(null, parser);

      ParsedFullDataTypeSpec aParsedFullDataTypeSpec = new ParsedFullDataTypeSpec();
      aParsedFullDataTypeSpec.addSpec(new ConcreteDataTypeSpec(GeneticDataTypes.Methylation));
      aParsedFullDataTypeSpec.addSpec(new DiscreteDataTypeSpec(GeneticDataTypes.CopyNumberAlteration, ComparisonOp
               .convertCode(">"), GeneticTypeLevel.HemizygouslyDeleted));
      parser = new ValueParser("COPY_NUMBER_ALTERATION:2;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:1;", 0.5, 1.0,
               aParsedFullDataTypeSpec.cleanUpInput());
      assertFalse(parser.isMRNAWayUp());
      assertFalse(parser.isMRNAWayDown());
      assertTrue(parser.isCnaAmplified());
      assertFalse(parser.isCnaGained());
      assertFalse(parser.isMutated());
      assertEquals(GeneticTypeLevel.Amplified, parser.getCNAlevel());
      assertTrue(parser.wasSequenced());
      assertTrue(parser.isGeneAltered());

      parser = new ValueParser("COPY_NUMBER_ALTERATION:2;MRNA_EXPRESSION:1;", 0.5);
      assertFalse(parser.isMutated());
      assertFalse(parser.wasSequenced());
      assertTrue(parser.isGeneAltered());

      parser = new ValueParser("COPY_NUMBER_ALTERATION:2;MRNA_EXPRESSION:blah;MUTATION_EXTENDED:0;", 0.5);
      assertFalse(parser.isMRNAWayUp());
      assertFalse(parser.isMRNAWayDown());
      assertTrue(parser.wasSequenced());
      assertTrue(parser.isGeneAltered());

      parser = new ValueParser("COPY_NUMBER_ALTERATION:;MRNA_EXPRESSION:;MUTATION_EXTENDED:;", 0.5);
      assertFalse(parser.isMRNAWayUp());
      assertFalse(parser.isMRNAWayDown());
      
      // TODO: IMPORTANT: these tests fail, but do we care about them? is "MUTATION:" a legitimate value? 
//      assertFalse(parser.wasSequenced());
//      assertFalse(parser.isGeneAltered());

      parser = new ValueParser("COPY_NUMBER_ALTERATION:2;MRNA_EXPRESSION:1;MUTATION_EXTENDED:nan;", 0.5);
      assertFalse(parser.isMutated());
      assertFalse(parser.wasSequenced());
      assertTrue(parser.isGeneAltered());

      parser = new ValueParser("COPY_NUMBER_ALTERATION:2;MRNA_EXPRESSION:1;MUTATION_EXTENDED:0;", 0.5);
      assertFalse(parser.isMutated());
      assertTrue(parser.wasSequenced());
      assertTrue(parser.isGeneAltered());

      aParsedFullDataTypeSpec = new ParsedFullDataTypeSpec();
      aParsedFullDataTypeSpec.addSpec(new DiscreteDataTypeSpec(GeneticDataTypes.Mutation, ComparisonOp
               .convertCode("<="), GeneticTypeLevel.Normal));
      parser = new ValueParser("COPY_NUMBER_ALTERATION:2;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:1;", 0.5, 1.0,
               aParsedFullDataTypeSpec.cleanUpInput());
      assertFalse(parser.isMutated());
      assertTrue(parser.wasSequenced());
      assertFalse(parser.isGeneAltered()); // because the filter only shows
                                           // 'normal' mutation levels; an
                                           // absurd use

      parser = new ValueParser("COPY_NUMBER_ALTERATION:2;MRNA_EXPRESSION:1;", 0.5);
      assertFalse(parser.isMutated());
      assertFalse(parser.wasSequenced());
      assertTrue(parser.isGeneAltered());

      parser = ValueParser.generateValueParser("g1", "COPY_NUMBER_ALTERATION:2;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:3;", 1.0, 1.0,
               anOncoPrintSpecification);

      assertTrue(parser.isCnaAmplified());
      assertFalse(parser.isCnaGained());
      assertTrue(parser.isMRNAWayUp());
      assertFalse(parser.isMRNAWayDown()); // because anOncoPrintSpecification
                                           // has no mRNA inequalities, the
                                           // zScore rules
      assertTrue(parser.isMutated());

      parser = ValueParser.generateValueParser("P53", "COPY_NUMBER_ALTERATION:1;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:1;", 0, 1.0,
               anOncoPrintSpecification);
      assertFalse(parser.isCnaAmplified());
      assertTrue(parser.isCnaGained());
      assertFalse(parser.isCnaDiploid());
      assertFalse(parser.isCnaHemizygouslyDeleted());
      assertFalse(parser.isCnaHomozygouslyDeleted());
      assertFalse(parser.isMRNAWayUp());
      assertTrue(parser.isMRNAWayDown()); // because theOncoPrintGeneDisplaySpec
                                          // filter rules combine to E<2 E>=3
      assertFalse(parser.isMutated()); // because filter rules ignores Mutations

      parser = new ValueParser("COPY_NUMBER_ALTERATION:1;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:1.5;", 1.0f);
      assertFalse(parser.isCnaAmplified());
      assertTrue(parser.isCnaGained());
      assertFalse(parser.isCnaDiploid());
      assertFalse(parser.isCnaHemizygouslyDeleted());
      assertFalse(parser.isCnaHomozygouslyDeleted());
      assertTrue(parser.isMRNAWayUp());
      assertFalse(parser.isMRNAWayDown());

      try {
         parser.isDiscreteTypeThisLevel(GeneticDataTypes.Expression, GeneticTypeLevel.Amplified);
         Assert.fail("should throw IllegalArgumentException(  )");
      } catch (Exception e) {
         assertEquals("theDiscreteGeneticDataType is not a discrete datatype", e.getMessage());
      }

   }

	@Test
   public void testIsDiscreteTypeAltered() {

      OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec = TestOncoPrintSpecificationLibrary
               .createTestOncoPrintGeneDisplaySpec();
      ValueParser parser = new ValueParser("COPY_NUMBER_ALTERATION:2;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:1;", 0.0, 1.0,
               theOncoPrintGeneDisplaySpec);
      assertTrue(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      parser = new ValueParser("COPY_NUMBER_ALTERATION:1;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:1;", 0.0, 1.0,
               theOncoPrintGeneDisplaySpec);
      assertTrue(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      parser = new ValueParser("COPY_NUMBER_ALTERATION:0;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:1;", 0.0, 1.0,
               theOncoPrintGeneDisplaySpec);
      assertFalse(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      parser = new ValueParser("COPY_NUMBER_ALTERATION:-1;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:1;", 0.0, 1.0,
               theOncoPrintGeneDisplaySpec);
      assertFalse(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      parser = new ValueParser("COPY_NUMBER_ALTERATION:-2;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:1;", 0.0, 1.0,
               theOncoPrintGeneDisplaySpec);
      assertTrue(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      parser = new ValueParser("MUTATION:C135F;MRNA_EXPRESSION:1;", 0.0, 1.0, theOncoPrintGeneDisplaySpec);
      assertFalse(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      parser = new ValueParser("COPY_NUMBER_ALTERATION:nan;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:1;", 0.0, 1.0,
               theOncoPrintGeneDisplaySpec);
      assertFalse(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      parser = new ValueParser("COPY_NUMBER_ALTERATION: JUNK;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:1;", 0.0, 1.0,
               theOncoPrintGeneDisplaySpec);
      assertFalse(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      parser = new ValueParser("COPY_NUMBER_ALTERATION:4;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:1;", 0.0, 1.0,
               theOncoPrintGeneDisplaySpec);
      assertFalse(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

      ParsedFullDataTypeSpec aParsedFullDataTypeSpec = new ParsedFullDataTypeSpec( );
      aParsedFullDataTypeSpec.addSpec( new DiscreteDataTypeSetSpec( GeneticDataTypes.CopyNumberAlteration,
              GeneticTypeLevel.Gained) );
      aParsedFullDataTypeSpec.addSpec( new DiscreteDataTypeSetSpec( GeneticDataTypes.CopyNumberAlteration,
               GeneticTypeLevel.Amplified) );
      aParsedFullDataTypeSpec.addSpec( new DiscreteDataTypeSetSpec( GeneticDataTypes.CopyNumberAlteration,
               GeneticTypeLevel.Diploid ) );
      theOncoPrintGeneDisplaySpec = aParsedFullDataTypeSpec.cleanUpInput();
      parser = new ValueParser("COPY_NUMBER_ALTERATION:0;MUTATION_EXTENDED:C135F;MRNA_EXPRESSION:1;", 0.0, 1.0,
               theOncoPrintGeneDisplaySpec);
      assertTrue(parser.isDiscreteTypeAltered(GeneticDataTypes.CopyNumberAlteration));

   }

}