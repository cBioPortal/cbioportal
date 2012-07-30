package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.mskcc.cbio.portal.util.Direction;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ComparisonOp;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ConcreteDataTypeSpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ContinuousDataTypeSpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.DataTypeSpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.DiscreteDataTypeSetSpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.DiscreteDataTypeSpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.GeneSet;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.GeneWithSpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.GeneticDataTypes;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.GeneticTypeLevel;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.OncoPrintGeneDisplaySpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.OncoPrintSpecification;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParsedFullDataTypeSpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ResultDataTypeSpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.UniqueEnumPrefix;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.Utilities;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.DataTypeSpecEnumerations.DataTypeCategory;

/**
 * test my library of OncoPrintSpec classes, without the parser
 * @author Arthur Goldberg
 */
public class TestOncoPrintSpecificationLibrary extends TestCase{

   @Test
   public void testComparisonOp() {

       try {
           ComparisonOp aComparisonOp = ComparisonOp.convertCode(null);
           Assert.fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
           Assert.assertEquals("Invalid parsedToken: null", e.getMessage() );
        }
   }
   
   @Test
   public void testAppendSemicolons(){
      
      String[][] inputOutputPairs = {
            { "aaaa", "aaaa", },
            { ": xx ;", ": xx ;", },
            { ": xx ;: xx ;", ": xx ;: xx ;", },
            { ": xx ", ": xx ;", },
            { ": xx ;: xx ", ": xx ;: xx ;", },
            { ": xx : xx ;", ": xx : xx ;", },
            { ": xx \n ", ": xx ;\n ", },
            { ": xx \n: yy \n", ": xx ;\n: yy ;\n", },
            { ": xx ;foo : aa :y z\n: yy ", ": xx ;foo : aa :y z;\n: yy ;", },
      };
      
      for( String [] ioPair : inputOutputPairs){
         Assert.assertEquals( ioPair[1],  Utilities.appendSemis( ioPair[0] ) );
      }
   }
   
    @Test
    public void testGeneticTypeLevel() {

        Assert.assertEquals(GeneticTypeLevel.Amplified, GeneticTypeLevel.convertCNAcode(2));
        Assert.assertEquals(GeneticTypeLevel.HomozygouslyDeleted,
                GeneticTypeLevel.findDataTypeLevel("HomozygouslyDeleted") );
        Assert.assertEquals(GeneticTypeLevel.HomozygouslyDeleted,
                GeneticTypeLevel.findDataTypeLevel("Homdel") );
        Assert.assertEquals(GeneticTypeLevel.HomozygouslyDeleted,
                GeneticTypeLevel.findDataTypeLevel("Hom") );
        Assert.assertEquals(GeneticTypeLevel.HemizygouslyDeleted,
                GeneticTypeLevel.findDataTypeLevel("HemizygouslyDeleted") );
        Assert.assertEquals(GeneticTypeLevel.HemizygouslyDeleted,
                GeneticTypeLevel.findDataTypeLevel("Hemi") );
        Assert.assertEquals(GeneticTypeLevel.HemizygouslyDeleted,
                GeneticTypeLevel.findDataTypeLevel("Hetloss") );
        Assert.assertEquals(GeneticTypeLevel.HemizygouslyDeleted,
                GeneticTypeLevel.findDataTypeLevel("HET") );
        Assert.assertEquals(GeneticTypeLevel.Diploid,
                GeneticTypeLevel.findDataTypeLevel("Diploid") );
        Assert.assertEquals(GeneticTypeLevel.Diploid,
                GeneticTypeLevel.findDataTypeLevel("di") );

    }

    @Test
    public void testDataTypeSpec() {
      try {
         DataTypeSpec.genericFindDataType( "foo", DataTypeCategory.Continuous );
         Assert.fail("Should throw IllegalArgumentException");
     } catch (IllegalArgumentException e) {
         Assert.assertEquals( "Invalid DataType: foo", e.getMessage() );
     }
    }

    @Test
    public void testConcreteDataTypeSpec() {
        try {
            Assert.assertEquals( GeneticDataTypes.Expression,
                    ConcreteDataTypeSpec.findDataType("mrn") );
            Assert.assertEquals( GeneticDataTypes.Expression,
                    ConcreteDataTypeSpec.findDataType("EXP") );
            Assert.assertEquals( GeneticDataTypes.CopyNumberAlteration,
                    ConcreteDataTypeSpec.findDataType("cna") );
            Assert.assertEquals( GeneticDataTypes.CopyNumberAlteration,
                    ConcreteDataTypeSpec.findDataType("copy") );
            Assert.assertEquals( GeneticDataTypes.Methylation,
                    ConcreteDataTypeSpec.findDataType("Methylation") );
            Assert.assertEquals( GeneticDataTypes.Methylation,
                    ConcreteDataTypeSpec.findDataType("Methy") );
            Assert.assertEquals( GeneticDataTypes.Methylation,
                    ConcreteDataTypeSpec.findDataType("Met") );
            Assert.assertEquals( GeneticDataTypes.Mutation,
                    ConcreteDataTypeSpec.findDataType("Mutation") );
            Assert.assertEquals( GeneticDataTypes.Mutation,
                    ConcreteDataTypeSpec.findDataType("Mu") );

        } catch (IllegalArgumentException e) {

            e.printStackTrace();
        }
        
        ConcreteDataTypeSpec aConcreteDataTypeSpec = new ConcreteDataTypeSpec
                (GeneticDataTypes.Methylation);
        Assert.assertTrue( aConcreteDataTypeSpec.satisfy(GeneticDataTypes.Methylation) );
        Assert.assertFalse( aConcreteDataTypeSpec.satisfy(GeneticDataTypes.Expression) );
        Assert.assertEquals( "Methylation", aConcreteDataTypeSpec.toString() );
        Assert.assertTrue( aConcreteDataTypeSpec.equals( aConcreteDataTypeSpec ));
        Assert.assertTrue( aConcreteDataTypeSpec.equals
                ( new ConcreteDataTypeSpec( GeneticDataTypes.Methylation) ));
        Assert.assertTrue( aConcreteDataTypeSpec.equals( new ConcreteDataTypeSpec( "Methyla") ));
        Assert.assertFalse( aConcreteDataTypeSpec.equals( new Object() ));
        Assert.assertEquals( null, ConcreteDataTypeSpec.concreteDataTypeSpecGenerator(null) );
        Assert.assertEquals( new ConcreteDataTypeSpec(GeneticDataTypes.CopyNumberAlteration), 
              ConcreteDataTypeSpec.concreteDataTypeSpecGenerator("CNA") );
        Assert.assertEquals( null, ConcreteDataTypeSpec.concreteDataTypeSpecGenerator("foo") );
        
        //System.out.println( aConcreteDataTypeSpec.hashCode() );
    }
    
    @Test
    public void testContinuousDataTypeSpec() {
        ContinuousDataTypeSpec aContinuousDataTypeSpec = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode("<"), 1.0f );
        Assert.assertTrue( aContinuousDataTypeSpec.satisfy(0.5) );
        Assert.assertFalse( aContinuousDataTypeSpec.satisfy(1.0) );
        Assert.assertTrue( aContinuousDataTypeSpec.equals( aContinuousDataTypeSpec ) );
        Assert.assertFalse( aContinuousDataTypeSpec.equals( new Float( 0.3f ) ) );
        Assert.assertTrue( aContinuousDataTypeSpec.equals(
                 new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                          ComparisonOp.convertCode("<"), 1.0f ) ) );
        Assert.assertFalse( aContinuousDataTypeSpec.equals(
                 new ContinuousDataTypeSpec( GeneticDataTypes.Methylation,
                          ComparisonOp.convertCode("<"), 1.0f ) ) );
        Assert.assertFalse( aContinuousDataTypeSpec.equals(
                 new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                          ComparisonOp.convertCode("<="), 1.0f ) ) );
        Assert.assertFalse( aContinuousDataTypeSpec.equals(
                 new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                          ComparisonOp.convertCode("<"), 5.0f ) ) );

        // copy constructor and new Instance
        Assert.assertEquals( null, ContinuousDataTypeSpec.newInstance(null) );
        Assert.assertEquals( aContinuousDataTypeSpec, ContinuousDataTypeSpec.newInstance
                (aContinuousDataTypeSpec));
        Assert.assertNotSame( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                 ComparisonOp.convertCode("<"), 5.0f ), 
                 ContinuousDataTypeSpec.newInstance( aContinuousDataTypeSpec ));
        
        Assert.assertEquals( "Expression<1.0", aContinuousDataTypeSpec.toString() );

        Assert.assertEquals( GeneticDataTypes.Expression, 
                ContinuousDataTypeSpec.findDataType( "Exp" ));
        Assert.assertEquals( GeneticDataTypes.Expression, 
              ContinuousDataTypeSpec.findDataType( "mrna" ));
        Assert.assertEquals( GeneticDataTypes.Expression, 
              ContinuousDataTypeSpec.findDataType( "microRNA" ));
        Assert.assertEquals( GeneticDataTypes.Methylation, 
                ContinuousDataTypeSpec.findDataType( "met" ));
        
        ContinuousDataTypeSpec aContinuousDataTypeSpec2 = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode(">="), 1.0f );
        
        try {
            aContinuousDataTypeSpec.combine(aContinuousDataTypeSpec2);
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals( "the ContinuousDataTypeSpecs must have ComparisonOps " +
                    "with the same direction", e.getMessage() );
        }
        try {
            aContinuousDataTypeSpec2.combine(aContinuousDataTypeSpec);
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals( "the ContinuousDataTypeSpecs must have ComparisonOps " +
                    "with the same direction", e.getMessage() );
        }
        
        aContinuousDataTypeSpec = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode(">"), 2.0f );        
        
        aContinuousDataTypeSpec.combine(aContinuousDataTypeSpec2);
        Assert.assertEquals( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                ComparisonOp.convertCode(">="), 1.0f ), aContinuousDataTypeSpec );

        // test same threshold
        aContinuousDataTypeSpec = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode(">"), 2.0f );        
        aContinuousDataTypeSpec2 = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode(">="), 2.0f );
        aContinuousDataTypeSpec.combine(aContinuousDataTypeSpec2);
        Assert.assertEquals( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                ComparisonOp.convertCode(">="), 2.0f ), aContinuousDataTypeSpec );

        aContinuousDataTypeSpec = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode(">"), 2.0f );        
        aContinuousDataTypeSpec2 = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode(">"), 2.0f );
        aContinuousDataTypeSpec.combine(aContinuousDataTypeSpec2);
        Assert.assertEquals( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                ComparisonOp.convertCode(">"), 2.0f ), aContinuousDataTypeSpec );

        aContinuousDataTypeSpec = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode(">="), 2.0f );        
        aContinuousDataTypeSpec2 = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode(">"), 2.0f );
        aContinuousDataTypeSpec.combine(aContinuousDataTypeSpec2);
        Assert.assertEquals( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                ComparisonOp.convertCode(">="), 2.0f ), aContinuousDataTypeSpec );
        
        aContinuousDataTypeSpec = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode("<="), -1.0f );        
        aContinuousDataTypeSpec2 = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode("<"), -3.0f );
        aContinuousDataTypeSpec.combine(aContinuousDataTypeSpec2);
        Assert.assertEquals( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                ComparisonOp.convertCode("<="), -1.0f ), aContinuousDataTypeSpec );
        
        aContinuousDataTypeSpec = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode("<="), -1.0f );        
        aContinuousDataTypeSpec2 = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode("<"), 2.0f );
        aContinuousDataTypeSpec.combine(aContinuousDataTypeSpec2);
        Assert.assertEquals( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                ComparisonOp.convertCode("<"), 2.0f ), aContinuousDataTypeSpec );
        
        aContinuousDataTypeSpec = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode("<="), 0.0f );        
        aContinuousDataTypeSpec2 = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode("<"), 0.0f );
        aContinuousDataTypeSpec.combine(aContinuousDataTypeSpec2);
        Assert.assertEquals( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                ComparisonOp.convertCode("<="), 0.0f ), aContinuousDataTypeSpec );
        
        aContinuousDataTypeSpec = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode("<"), 0.0f );        
        aContinuousDataTypeSpec2 = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode("<="), 0.0f );
        aContinuousDataTypeSpec.combine(aContinuousDataTypeSpec2);
        Assert.assertEquals( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                ComparisonOp.convertCode("<="), 0.0f ), aContinuousDataTypeSpec );
        
        aContinuousDataTypeSpec = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode("<"), 0.0f );        
        aContinuousDataTypeSpec2 = 
            new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.convertCode("<"), 0.0f );
        aContinuousDataTypeSpec.combine(aContinuousDataTypeSpec2);
        Assert.assertEquals( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                ComparisonOp.convertCode("<"), 0.0f ), aContinuousDataTypeSpec );
        
        Assert.assertEquals( null, ContinuousDataTypeSpec.continuousDataTypeSpecGenerator(
                "foo", "bla", "xxx" ) );
        Assert.assertEquals( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
              ComparisonOp.convertCode("<"), 0.3f ), 
              ContinuousDataTypeSpec.continuousDataTypeSpecGenerator( "Exp", "<", "0.3" ) );
        Assert.assertEquals( null, ContinuousDataTypeSpec.continuousDataTypeSpecGenerator
                ("foo", "<", "0" ) );
        Assert.assertEquals( null, ContinuousDataTypeSpec.continuousDataTypeSpecGenerator
                ("Exp", "<<", "0" ) );
        Assert.assertEquals( null, ContinuousDataTypeSpec.continuousDataTypeSpecGenerator
                ("Exp", "<", "x" ) );

    }

    @Test
    public void testDiscreteDataTypeSetSpec() {
        findDiscreteDataTypeSetSpec( GeneticDataTypes.CopyNumberAlteration, "Copy" );
        findDiscreteDataTypeSetSpec( GeneticDataTypes.CopyNumberAlteration, "CNA" );
        findDiscreteDataTypeSetSpec( GeneticDataTypes.CopyNumberAlteration, "cna" );
        findDiscreteDataTypeSetSpec( GeneticDataTypes.Mutation, "mu" );
                
        DiscreteDataTypeSetSpec aDiscreteDataTypeSetSpec = 
            new DiscreteDataTypeSetSpec( GeneticDataTypes.CopyNumberAlteration,
                    GeneticTypeLevel.Gained );
        Assert.assertEquals( "Gained ", 
                aDiscreteDataTypeSetSpec.toString() );
        Assert.assertTrue( aDiscreteDataTypeSetSpec.satisfy( GeneticTypeLevel.Gained ) );
        Assert.assertTrue( aDiscreteDataTypeSetSpec.equals( aDiscreteDataTypeSetSpec ));
        Assert.assertTrue( aDiscreteDataTypeSetSpec.equals( 
                new DiscreteDataTypeSetSpec( GeneticDataTypes.CopyNumberAlteration,
                GeneticTypeLevel.Gained ) ));
        Assert.assertTrue( aDiscreteDataTypeSetSpec.equals( new DiscreteDataTypeSetSpec
                ( "CNA", 1 ) ));
        
        aDiscreteDataTypeSetSpec = 
            new DiscreteDataTypeSetSpec( GeneticDataTypes.CopyNumberAlteration,
                    GeneticTypeLevel.Gained, GeneticTypeLevel.Amplified );
        Assert.assertEquals( "Gained Amplified ", 
                aDiscreteDataTypeSetSpec.toString() );
        Assert.assertTrue( aDiscreteDataTypeSetSpec.satisfy( GeneticTypeLevel.Gained ) );
        Assert.assertTrue( aDiscreteDataTypeSetSpec.satisfy( GeneticTypeLevel.Amplified ) );
        Assert.assertFalse( aDiscreteDataTypeSetSpec.satisfy( GeneticTypeLevel.Diploid ) );
        Assert.assertTrue( aDiscreteDataTypeSetSpec.equals( aDiscreteDataTypeSetSpec ));
        Assert.assertTrue( aDiscreteDataTypeSetSpec.equals( 
                new DiscreteDataTypeSetSpec( GeneticDataTypes.CopyNumberAlteration,
                        GeneticTypeLevel.Gained, GeneticTypeLevel.Amplified ) ));
        
        Assert.assertEquals( aDiscreteDataTypeSetSpec, 
                 DiscreteDataTypeSetSpec.newInstance(aDiscreteDataTypeSetSpec) );
        Assert.assertEquals( null, null,
                 DiscreteDataTypeSetSpec.newInstance( null ) );
        
        aDiscreteDataTypeSetSpec.addLevel( GeneticTypeLevel.Amplified );
        Assert.assertTrue( aDiscreteDataTypeSetSpec.satisfy( GeneticTypeLevel.Amplified ) );
        aDiscreteDataTypeSetSpec.addLevel( GeneticTypeLevel.Diploid );
        Assert.assertTrue( aDiscreteDataTypeSetSpec.satisfy( GeneticTypeLevel.Diploid ) );
        
        aDiscreteDataTypeSetSpec.combine( new DiscreteDataTypeSetSpec
                (GeneticDataTypes.CopyNumberAlteration,
                        GeneticTypeLevel.HemizygouslyDeleted,
                        GeneticTypeLevel.HemizygouslyDeleted, GeneticTypeLevel.Diploid  ));
        Assert.assertTrue( aDiscreteDataTypeSetSpec.satisfy( GeneticTypeLevel.Amplified ) );
        Assert.assertTrue( aDiscreteDataTypeSetSpec.satisfy( GeneticTypeLevel.Gained ) );
        Assert.assertTrue( aDiscreteDataTypeSetSpec.satisfy( GeneticTypeLevel.Diploid ) );
        Assert.assertTrue( aDiscreteDataTypeSetSpec.satisfy
                ( GeneticTypeLevel.HemizygouslyDeleted ) );
        Assert.assertFalse( aDiscreteDataTypeSetSpec.satisfy
                ( GeneticTypeLevel.HomozygouslyDeleted ) );

        try {
           new DiscreteDataTypeSetSpec( GeneticDataTypes.CopyNumberAlteration,
                   GeneticTypeLevel.Mutated );
           Assert.fail("Should throw IllegalArgumentException");
       } catch (IllegalArgumentException e) {
           Assert.assertEquals( "Mutated is not a level for CopyNumberAlteration", e.getMessage() );
       }

       try {
          new DiscreteDataTypeSetSpec( "CNA", 10 );
          Assert.fail("Should throw IllegalArgumentException");
      } catch (IllegalArgumentException e) {
          Assert.assertEquals( "10 is invalid code for levels in CopyNumberAlteration",
                  e.getMessage() );
      }
      Assert.assertEquals( null, DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGenerator( null ) );
      Assert.assertEquals( null, DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGenerator( "foo" ));
      Assert.assertEquals( new DiscreteDataTypeSetSpec( GeneticDataTypes.CopyNumberAlteration,
              GeneticTypeLevel.Gained ),
            DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGenerator( "Gain") );
      Assert.assertEquals( new DiscreteDataTypeSetSpec( GeneticDataTypes.Mutation,
              GeneticTypeLevel.Mutated ),
            DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGenerator( "Mut") );
      
      Assert.assertEquals( null, DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGenerator
              ( null, null ) );
      Assert.assertEquals( null, DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGenerator
              ( null, "2" ) );
      Assert.assertEquals( null, DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGenerator
              ( "JUNK", "2" ) );
      Assert.assertEquals( null, DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGenerator
              ( "JUNK", "CRAP" ) );
      Assert.assertEquals( null, DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGenerator
              ( "CNA", null ) );
      Assert.assertEquals( null, DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGenerator
              ( "CNA", "XX" ) );
      Assert.assertEquals( new DiscreteDataTypeSetSpec( GeneticDataTypes.CopyNumberAlteration,
              GeneticTypeLevel.Amplified ),
            DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGenerator( "CNA", "2" ) );
    }
    
    private void findDiscreteDataTypeSetSpec( GeneticDataTypes expectedGeneticDataType,
            String name) {
        Assert.assertEquals( expectedGeneticDataType, DiscreteDataTypeSetSpec.findDataType(name));
    }
    
    @Test
    public void testDiscreteDataTypeSpec() {
        DiscreteDataTypeSpec aDiscreteDataTypeSpec = 
            new DiscreteDataTypeSpec( GeneticDataTypes.CopyNumberAlteration,
                    ComparisonOp.convertCode("<"), GeneticTypeLevel.Gained );
        Assert.assertTrue( aDiscreteDataTypeSpec.satisfy( GeneticTypeLevel.Diploid ) );
        Assert.assertFalse( aDiscreteDataTypeSpec.satisfy(GeneticTypeLevel.Gained) );
        Assert.assertTrue( aDiscreteDataTypeSpec.equals( aDiscreteDataTypeSpec ) );
        Assert.assertFalse( aDiscreteDataTypeSpec.equals( new Float( 0.3f ) ) );
        Assert.assertTrue( aDiscreteDataTypeSpec.equals( 
                new DiscreteDataTypeSpec( GeneticDataTypes.CopyNumberAlteration,
                ComparisonOp.convertCode("<"), GeneticTypeLevel.Gained ) ) );
        //System.out.println( aDiscreteDataTypeSpec.hashCode() );
        Assert.assertEquals( "CopyNumberAlteration<Gained", aDiscreteDataTypeSpec.toString() );
        
        DiscreteDataTypeSetSpec aDiscreteDataTypeSetSpec =
                aDiscreteDataTypeSpec.convertToDiscreteDataTypeSetSpec();
        for( GeneticTypeLevel aGeneticTypeLevel: GeneticTypeLevel.values()){
            if( aGeneticTypeLevel.getTheGeneticDataType().equals
                    (GeneticDataTypes.CopyNumberAlteration) ){
                Assert.assertEquals( aGeneticTypeLevel.ordinal()
                        < GeneticTypeLevel.Gained.ordinal(),
                        aDiscreteDataTypeSetSpec.satisfy(aGeneticTypeLevel));
            }
        }
        Assert.assertTrue( aDiscreteDataTypeSetSpec.satisfy(GeneticTypeLevel.Diploid));
        Assert.assertFalse( aDiscreteDataTypeSetSpec.satisfy(GeneticTypeLevel.Gained));

        aDiscreteDataTypeSpec = new DiscreteDataTypeSpec( GeneticDataTypes.Mutation,
                ComparisonOp.convertCode("<"), GeneticTypeLevel.Mutated );

        Assert.assertTrue( aDiscreteDataTypeSpec.satisfy(GeneticTypeLevel.Normal));
        Assert.assertFalse( aDiscreteDataTypeSpec.satisfy(GeneticTypeLevel.Amplified));
        
        aDiscreteDataTypeSpec = new DiscreteDataTypeSpec( GeneticDataTypes.CopyNumberAlteration,
                ComparisonOp.convertCode(">="), GeneticTypeLevel.Diploid );

        Assert.assertTrue( aDiscreteDataTypeSpec.satisfy(GeneticTypeLevel.Amplified));
        Assert.assertFalse( aDiscreteDataTypeSpec.satisfy(GeneticTypeLevel.Normal));

        satisfiedDiscreteDataTypeSpec( GeneticDataTypes.CopyNumberAlteration,
                ComparisonOp.convertCode("<"), GeneticTypeLevel.Gained, GeneticTypeLevel.Diploid );
        satisfiedDiscreteDataTypeSpec( GeneticDataTypes.CopyNumberAlteration,
                ComparisonOp.convertCode("<="), GeneticTypeLevel.Gained, GeneticTypeLevel.Diploid );
        satisfiedDiscreteDataTypeSpec( GeneticDataTypes.CopyNumberAlteration,
                ComparisonOp.convertCode("<="), GeneticTypeLevel.Gained, GeneticTypeLevel.Gained );
        satisfiedDiscreteDataTypeSpec( GeneticDataTypes.CopyNumberAlteration,
                ComparisonOp.convertCode(">="), GeneticTypeLevel.Gained, GeneticTypeLevel.Gained );
        satisfiedDiscreteDataTypeSpec( GeneticDataTypes.CopyNumberAlteration,
                ComparisonOp.convertCode(">"), GeneticTypeLevel.Gained, GeneticTypeLevel.Amplified);
        
        findDiscreteDataType( GeneticDataTypes.CopyNumberAlteration, "Copy" );
        findDiscreteDataType( GeneticDataTypes.CopyNumberAlteration, "CNA" );
        findDiscreteDataType( GeneticDataTypes.CopyNumberAlteration, "cna" );
        findDiscreteDataType( GeneticDataTypes.Mutation, "mu" );
        
        Assert.assertEquals( null, DiscreteDataTypeSpec.discreteDataTypeSpecGenerator
                (null, null, null) );
        Assert.assertEquals( null, DiscreteDataTypeSpec.discreteDataTypeSpecGenerator
                (null, null, "foo") );
        Assert.assertEquals( null, DiscreteDataTypeSpec.discreteDataTypeSpecGenerator
                (null, "xx", null) );
        Assert.assertEquals( null, DiscreteDataTypeSpec.discreteDataTypeSpecGenerator
                ("asd", null, null) );
        Assert.assertEquals( new DiscreteDataTypeSpec(  GeneticDataTypes.CopyNumberAlteration,
              ComparisonOp.convertCode("<"), GeneticTypeLevel.Gained ), 
              DiscreteDataTypeSpec.discreteDataTypeSpecGenerator( "CNA", "<", "Gai") );
        Assert.assertEquals( new DiscreteDataTypeSpec(  GeneticDataTypes.Mutation,
              ComparisonOp.convertCode("<"), GeneticTypeLevel.Mutated ), 
              DiscreteDataTypeSpec.discreteDataTypeSpecGenerator( "Mutation", "<", "Mutated") );
    }
    
    @Test
    public void testGeneSet() {
       
       GeneSet aGeneSet = new GeneSet();
       OncoPrintGeneDisplaySpec theResultFullDataTypeSpec = new OncoPrintGeneDisplaySpec();
       GeneWithSpec aGeneWithSpec = GeneWithSpec.geneWithSpecGenerator("foo",
               theResultFullDataTypeSpec, null );
       aGeneSet.addGeneWithSpec(aGeneWithSpec);
       Assert.assertEquals( GeneWithSpec.geneWithSpecGenerator("foo",
               theResultFullDataTypeSpec, null ),
                aGeneSet.getGene() );
       aGeneSet.addGeneWithSpec(aGeneWithSpec);
       Assert.assertEquals( null, aGeneSet.getGene() );
       aGeneSet.addGeneWithSpec( GeneWithSpec.geneWithSpecGenerator("foo",
               theResultFullDataTypeSpec, null ) );
       
       Assert.assertEquals( aGeneWithSpec, aGeneSet.getGeneWithSpec("foo") );
       Assert.assertEquals( aGeneWithSpec, aGeneSet.getGeneWithSpec("FOO") );
       Assert.assertEquals( null, aGeneSet.getGeneWithSpec("none") );
       
       GeneSet anotherGeneSet = new GeneSet( "test" );
       
       Assert.assertTrue( aGeneSet.equals(aGeneSet) );
       Assert.assertFalse( aGeneSet.equals(null) );
       Assert.assertFalse( aGeneSet.equals( new Float( 1 )) );
       Assert.assertFalse( aGeneSet.equals( anotherGeneSet ) );
       
       anotherGeneSet = new GeneSet( "foo" );
       anotherGeneSet.addGeneWithSpec(aGeneWithSpec);
       anotherGeneSet.addGeneWithSpec(aGeneWithSpec);
       anotherGeneSet.addGeneWithSpec(aGeneWithSpec);
// TODO: FIX:       Assert.assertTrue( aGeneSet.equals( anotherGeneSet ) );
    }

    private void satisfiedDiscreteDataTypeSpec( GeneticDataTypes theGeneticDataType,
            ComparisonOp comparisonOp, Object threshold, Object satisfied ) {
        DiscreteDataTypeSpec aDiscreteDataTypeSpec = 
            new DiscreteDataTypeSpec( theGeneticDataType, comparisonOp, threshold );
        Assert.assertTrue( aDiscreteDataTypeSpec.satisfy( (GeneticTypeLevel) satisfied ) );

        // take 'opposite' comparisonOp, and try Assert.assertFalse
        DiscreteDataTypeSpec oppositeDiscreteDataTypeSpec = 
            new DiscreteDataTypeSpec( theGeneticDataType, comparisonOp.oppositeComparisonOp(),
                    threshold );
        Assert.assertFalse( oppositeDiscreteDataTypeSpec.satisfy( (GeneticTypeLevel) satisfied ) );
    }
    
    private void findDiscreteDataType( GeneticDataTypes expectedGeneticDataType, String name) {
        Assert.assertEquals( expectedGeneticDataType, DiscreteDataTypeSpec.findDataType(name));
    }
    
    @Test
    public void testOncoPrintGeneDisplaySpec() {        
       OncoPrintGeneDisplaySpec theResultFullDataTypeSpec = new OncoPrintGeneDisplaySpec();
       for( GeneticDataTypes aGeneticDataType : GeneticDataTypes.values()){
          Assert.assertFalse( theResultFullDataTypeSpec.satisfy( aGeneticDataType ));
       }
       for( GeneticDataTypes aGeneticDataType : GeneticDataTypes.values()){
          Assert.assertFalse( theResultFullDataTypeSpec.satisfy( aGeneticDataType ));

          switch( aGeneticDataType.getTheDataTypeCategory()){
            case Continuous:
               Assert.assertFalse( theResultFullDataTypeSpec.satisfy( aGeneticDataType, 0.0f ));
               break;
            case Discrete:
               for( GeneticTypeLevel aGeneticTypeLevel: GeneticTypeLevel.values()){
                  if( aGeneticTypeLevel.getTheGeneticDataType().getTheDataTypeCategory()
                          == DataTypeCategory.Discrete ){
                     Assert.assertFalse( theResultFullDataTypeSpec.satisfy( aGeneticDataType,
                             aGeneticTypeLevel ));
                  }
               }
               break;
          }
       }
       
       // test the default
       OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
       theOncoPrintGeneDisplaySpec.setDefault( 1.0, 1.0 );
       
       // Expression alteration definition is <= and >=, as per docs
       Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
               ( GeneticDataTypes.Expression, -1.0f ) );
       Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
               ( GeneticDataTypes.Expression, 1.0f ) );
       Assert.assertFalse( theOncoPrintGeneDisplaySpec.satisfy
               ( GeneticDataTypes.Expression, 0.0f ) );
       Assert.assertFalse( theOncoPrintGeneDisplaySpec.satisfy
               ( GeneticDataTypes.Expression, -0.9f ) );
       Assert.assertFalse( theOncoPrintGeneDisplaySpec.satisfy
               ( GeneticDataTypes.Expression, 0.9f ) );
       Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
               ( GeneticDataTypes.CopyNumberAlteration ) );
       Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
               ( GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.HomozygouslyDeleted ) );
       Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
               ( GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.Amplified ) );
       Assert.assertFalse( theOncoPrintGeneDisplaySpec.satisfy
               ( GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.Gained) );
       Assert.assertFalse( theOncoPrintGeneDisplaySpec.satisfy
               ( GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.HemizygouslyDeleted ) );
       Assert.assertFalse( theOncoPrintGeneDisplaySpec.satisfy
               ( GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.Diploid ) );
       
       Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
               ( GeneticDataTypes.Mutation, GeneticTypeLevel.Mutated) );
       Assert.assertFalse( theOncoPrintGeneDisplaySpec.satisfy
               ( GeneticDataTypes.Mutation, GeneticTypeLevel.Normal ) );
       
    }
    
    /**
     * combine some data type specs:
     * The net result:
     * HomozygouslyDeleted Gained Amplified   Expression<2.0 Expression>=3.0   Methylation ;
     * @return
     */
    public static OncoPrintGeneDisplaySpec createTestOncoPrintGeneDisplaySpec(){
       ParsedFullDataTypeSpec aParsedFullDataTypeSpec = new ParsedFullDataTypeSpec( );

       aParsedFullDataTypeSpec.addSpec( new ConcreteDataTypeSpec(  GeneticDataTypes.Methylation ));
       aParsedFullDataTypeSpec.addSpec( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
               ComparisonOp.convertCode("<"), 1.0f ) );
       aParsedFullDataTypeSpec.addSpec( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
               ComparisonOp.convertCode("<"), 2.0f ) );
       aParsedFullDataTypeSpec.addSpec( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
               ComparisonOp.convertCode(">="), 3.0f ) );
       aParsedFullDataTypeSpec.addSpec( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
               ComparisonOp.convertCode("<"), 2.0f ) );
       aParsedFullDataTypeSpec.addSpec( new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
               ComparisonOp.convertCode(">="), 3.4f ) );

       aParsedFullDataTypeSpec.addSpec( new DiscreteDataTypeSetSpec
               ( GeneticDataTypes.CopyNumberAlteration,
               GeneticTypeLevel.Gained) );
       aParsedFullDataTypeSpec.addSpec( new DiscreteDataTypeSetSpec
               ( GeneticDataTypes.CopyNumberAlteration,
               GeneticTypeLevel.Amplified) );
       aParsedFullDataTypeSpec.addSpec( new DiscreteDataTypeSetSpec
               ( GeneticDataTypes.CopyNumberAlteration,
               GeneticTypeLevel.Gained) );
       aParsedFullDataTypeSpec.addSpec( new DiscreteDataTypeSpec
               ( GeneticDataTypes.CopyNumberAlteration,
               ComparisonOp.convertCode("<"), GeneticTypeLevel.HemizygouslyDeleted ) );
       return aParsedFullDataTypeSpec.cleanUpInput();
    }

    @Test
    public void testParsedFullDataTypeSpec() {        
       // make ResultFullDataTypeSpec that accepts nothing
       OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec =
               (new ParsedFullDataTypeSpec( )).cleanUpInput();
       for( GeneticDataTypes aGeneticDataType : GeneticDataTypes.values()){
          Assert.assertFalse( theOncoPrintGeneDisplaySpec.satisfy( aGeneticDataType ));
       }
       
        theOncoPrintGeneDisplaySpec = createTestOncoPrintGeneDisplaySpec();
        //System.out.println( theResultFullDataTypeSpec );
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy(GeneticDataTypes.Methylation, 2f ));
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
                (GeneticDataTypes.Methylation, -33f ));

        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy(GeneticDataTypes.Expression, -3f ));
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
                (GeneticDataTypes.Expression, 1.99f ));
        Assert.assertFalse( theOncoPrintGeneDisplaySpec.satisfy(GeneticDataTypes.Expression, 2f ));
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy(GeneticDataTypes.Expression, 3f ));
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy(GeneticDataTypes.Expression, 33f ));
        
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
                (GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.Amplified ));
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
                (GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.Gained ));
        Assert.assertFalse( theOncoPrintGeneDisplaySpec.satisfy
                (GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.Diploid ));
        Assert.assertFalse( theOncoPrintGeneDisplaySpec.satisfy
                (GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.HemizygouslyDeleted ));
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
                (GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.HomozygouslyDeleted ));
        
        // all methylations
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
                ( GeneticDataTypes.Methylation, -3.0f, Direction.higher ) );
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
                ( GeneticDataTypes.Methylation, -3.0f, Direction.lower ) );
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
                ( GeneticDataTypes.Methylation, 3.0f, Direction.higher ) );
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
                ( GeneticDataTypes.Methylation, 3.0f, Direction.lower ) );

        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
                ( GeneticDataTypes.Expression, 3f, Direction.higher ) );
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
                ( GeneticDataTypes.Expression, 3.5f, Direction.higher ) );
        Assert.assertFalse( theOncoPrintGeneDisplaySpec.satisfy
                ( GeneticDataTypes.Expression, 2.5f, Direction.higher ) );
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
                ( GeneticDataTypes.Expression, -1.0f, Direction.lower ) );
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
                ( GeneticDataTypes.Expression, 1.0f, Direction.lower ) );
        Assert.assertTrue( theOncoPrintGeneDisplaySpec.satisfy
                ( GeneticDataTypes.Expression, 1.99f, Direction.lower) );
        Assert.assertFalse( theOncoPrintGeneDisplaySpec.satisfy
                ( GeneticDataTypes.Expression, 2.0f, Direction.lower ) );
    }

    @Test
    public void testResultDataTypeSpec() {
       ResultDataTypeSpec aResultDataTypeSpec = new ResultDataTypeSpec
               ( GeneticDataTypes.Expression );
       Assert.assertTrue( aResultDataTypeSpec.equals
               ( ResultDataTypeSpec.newInstance(aResultDataTypeSpec) )  );
       aResultDataTypeSpec.setCombinedLesserContinuousDataTypeSpec(
                new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                         ComparisonOp.convertCode("<"), 1.0f ) );
       Assert.assertTrue( aResultDataTypeSpec.equals
               ( ResultDataTypeSpec.newInstance(aResultDataTypeSpec) )  );
       aResultDataTypeSpec.setCombinedGreaterContinuousDataTypeSpec(
                new ContinuousDataTypeSpec( GeneticDataTypes.Expression,
                         ComparisonOp.convertCode(">="), 3.0f ) );
       Assert.assertTrue( aResultDataTypeSpec.equals
               ( ResultDataTypeSpec.newInstance(aResultDataTypeSpec) )  );
       aResultDataTypeSpec.setAcceptAll(true);
       Assert.assertTrue( aResultDataTypeSpec.equals
               ( ResultDataTypeSpec.newInstance(aResultDataTypeSpec) )  );
       
       Assert.assertFalse( aResultDataTypeSpec.equals
               ( new ResultDataTypeSpec( GeneticDataTypes.Expression ) )  );
       Assert.assertEquals( null, ResultDataTypeSpec.newInstance( null )  );

    }
    
    @Test
    public void testGeneWithSpec() {
       
       Assert.assertEquals( null, GeneWithSpec.geneWithSpecGenerator(null, null, null ));
       Assert.assertEquals( null, GeneWithSpec.geneWithSpecGenerator("name", null, null ));
       OncoPrintGeneDisplaySpec theResultFullDataTypeSpec = new OncoPrintGeneDisplaySpec();
       Assert.assertEquals( null, GeneWithSpec.geneWithSpecGenerator
               (null, theResultFullDataTypeSpec, null ));
       theResultFullDataTypeSpec.setDefault( 2.0, 1.0 );
       GeneWithSpec aGeneWithSpec = new GeneWithSpec( "name", theResultFullDataTypeSpec );
       Assert.assertEquals( aGeneWithSpec.toString(),
               GeneWithSpec.geneWithSpecGenerator("name",
               theResultFullDataTypeSpec, null ).toString());
       Assert.assertEquals( aGeneWithSpec.toString(),
               GeneWithSpec.geneWithSpecGenerator("name", null,
                   theResultFullDataTypeSpec ).toString());
       // TODO: impl equals and hashCode for GeneWithSpec AND all its components, so this works
       Assert.assertTrue( aGeneWithSpec.equals( GeneWithSpec.geneWithSpecGenerator("name",
               theResultFullDataTypeSpec, null )));
       Assert.assertTrue( aGeneWithSpec.equals( GeneWithSpec.geneWithSpecGenerator("name",
               null, theResultFullDataTypeSpec )));
    }
    
    private void checkAmazin(GeneWithSpec aGeneWithSpec){
       
        Assert.assertTrue(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy(
                GeneticDataTypes.CopyNumberAlteration));
        Assert.assertTrue(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy
                (GeneticDataTypes.Mutation));
        Assert.assertTrue(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy
                (GeneticDataTypes.Expression, -2.01f));
        Assert.assertFalse(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy
                (GeneticDataTypes.Expression, -2.0f));
    }

    @Test
    public void testUniqueEnumPrefix() {

        tryAnUniqueEnumPrefix("Meth", GeneticDataTypes.class,
                GeneticDataTypes.Methylation);
        tryAnUniqueEnumPrefix("E", GeneticDataTypes.class,
                GeneticDataTypes.Expression);
        tryAnUniqueEnumPrefix("Mu", GeneticDataTypes.class,
                GeneticDataTypes.Mutation);
        tryAnUniqueEnumPrefix("C", GeneticDataTypes.class,
                GeneticDataTypes.CopyNumberAlteration);
    }

    private void tryAnUniqueEnumPrefix(String testInput, Class enumClass,
            Object enumConstant) {

        try {
            Assert.assertEquals(enumConstant,
                    UniqueEnumPrefix.findUniqueEnumMatchingPrefix(enumClass, testInput));
        } catch (IllegalArgumentException e) {
            System.out
                    .println("testUniqueEnumPrefix: IllegalArgumentException: "
                            + e.getMessage());
        }
    }

    @Test
    public void testOncoPrintSpecification() {
       
       OncoPrintSpecification aOncoPrintSpecification = new OncoPrintSpecification();
       
       String[] genes = { "G1", "GENE2", "LAST" };
       OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
       theOncoPrintGeneDisplaySpec.setDefault( 1.5f, 1.0f );
       GeneSet aGeneSet = new GeneSet( );
       for( String g : genes){
          aGeneSet.addGeneWithSpec( new GeneWithSpec( g, theOncoPrintGeneDisplaySpec ) );
       }
       aOncoPrintSpecification.add(aGeneSet);
       Assert.assertEquals( null, aOncoPrintSpecification.getGeneWithSpec( "geneName" ) );
       Assert.assertEquals( new ArrayList<String>( Arrays.asList( genes ) ),
               aOncoPrintSpecification.listOfGenes() );

       // TODO: change to .equals, when implemented
       Assert.assertEquals( new GeneWithSpec( "GENE2", theOncoPrintGeneDisplaySpec ).toString(),
               aOncoPrintSpecification.getGeneWithSpec( "gene2" ).toString() );

       aOncoPrintSpecification.add(aGeneSet);
       ArrayList<String> t = new ArrayList<String>( Arrays.asList( genes ) );
       t.addAll( new ArrayList<String>( Arrays.asList( genes ) ) );
       Assert.assertEquals( t, aOncoPrintSpecification.listOfGenes() );

       Assert.assertEquals( null, aOncoPrintSpecification.getGeneWithSpec( "GENENAME" ) );
       // TODO: change to .equals, when implemented
       Assert.assertEquals( new GeneWithSpec( "GENE2", theOncoPrintGeneDisplaySpec ).toString(),
               aOncoPrintSpecification.getGeneWithSpec( "gene2" ).toString() );
       
    }
    
    @Test
    public void testGetUnionOfPossibleLevels() {
       
       OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
       OncoPrintSpecification anOncoPrintSpecification = new OncoPrintSpecification(  );
       // TODO: change to .equals, when implemented
       Assert.assertEquals( theOncoPrintGeneDisplaySpec.toString(),
               anOncoPrintSpecification.getUnionOfPossibleLevels().toString() );
       
       String[] genes = { "G1", "LAST" };
       theOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
       theOncoPrintGeneDisplaySpec.setDefault( 2.5, 1.0 );
       anOncoPrintSpecification = new OncoPrintSpecification( genes, theOncoPrintGeneDisplaySpec );
       // TODO: change to .equals, when implemented
       Assert.assertEquals( theOncoPrintGeneDisplaySpec.toString(),
               anOncoPrintSpecification.getUnionOfPossibleLevels().toString() );

       //  HomozygouslyDeleted Gained Amplified   Expression<2.0 Expression>=3.0   Methylation ;
       anOncoPrintSpecification = new OncoPrintSpecification( genes,
               createTestOncoPrintGeneDisplaySpec() );
       Assert.assertEquals( createTestOncoPrintGeneDisplaySpec().toString(),
               anOncoPrintSpecification.getUnionOfPossibleLevels().toString() );
       
       ResultDataTypeSpec theResultDataTypeSpec = new ResultDataTypeSpec
               ( GeneticDataTypes.Expression );
       theResultDataTypeSpec.setCombinedGreaterContinuousDataTypeSpec( 
                ContinuousDataTypeSpec.continuousDataTypeSpecGenerator
                        ( "Expression", "<=", "2.5" ) );
       theOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
       theOncoPrintGeneDisplaySpec.setResultDataTypeSpec( GeneticDataTypes.Expression,
               theResultDataTypeSpec );
       GeneSet aGeneSet = new GeneSet( );
       aGeneSet.addGeneWithSpec( new GeneWithSpec( "foo", theOncoPrintGeneDisplaySpec ) );
       anOncoPrintSpecification.add(aGeneSet);
       
       // a hack; should create the right OncoPrintGeneDisplaySpec for the expected
       Assert.assertEquals( "HomozygouslyDeleted Gained Amplified   Expression<=2.5 " +
               "Expression>=3.0   Methylation ;",
                anOncoPrintSpecification.getUnionOfPossibleLevels().toString() );       

       theResultDataTypeSpec = new ResultDataTypeSpec( GeneticDataTypes.Mutation );
       theResultDataTypeSpec.setTheDiscreteDataTypeSetSpec( new DiscreteDataTypeSetSpec
               ( "Mutated") );
       theOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
       theOncoPrintGeneDisplaySpec.setResultDataTypeSpec( GeneticDataTypes.Mutation,
               theResultDataTypeSpec );
       aGeneSet = new GeneSet( );
       aGeneSet.addGeneWithSpec( new GeneWithSpec( "bar", theOncoPrintGeneDisplaySpec ) );       
       anOncoPrintSpecification.add(aGeneSet);
       // a hack; should create the right OncoPrintGeneDisplaySpec for the expected
       Assert.assertEquals( "HomozygouslyDeleted Gained Amplified   Expression<=2.5 " +
               "Expression>=3.0   Mutated   Methylation ;",
                anOncoPrintSpecification.getUnionOfPossibleLevels().toString() );

    }
}