package org.mskcc.portal.test.oncoPrintSpecLanguage;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.junit.Assert;
import org.junit.Test;
import org.mskcc.portal.oncoPrintSpecLanguage.CallOncoPrintSpecParser;
import org.mskcc.portal.oncoPrintSpecLanguage.ComparisonOp;
import org.mskcc.portal.oncoPrintSpecLanguage.ConcreteDataTypeSpec;
import org.mskcc.portal.oncoPrintSpecLanguage.ContinuousDataTypeSpec;
import org.mskcc.portal.oncoPrintSpecLanguage.DiscreteDataTypeSetSpec;
import org.mskcc.portal.oncoPrintSpecLanguage.DiscreteDataTypeSpec;
import org.mskcc.portal.oncoPrintSpecLanguage.GeneSet;
import org.mskcc.portal.oncoPrintSpecLanguage.GeneWithSpec;
import org.mskcc.portal.oncoPrintSpecLanguage.GeneticDataTypes;
import org.mskcc.portal.oncoPrintSpecLanguage.GeneticTypeLevel;
import org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintGeneDisplaySpec;
import org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintLangException;
import org.mskcc.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.portal.oncoPrintSpecLanguage.completeOncoPrintSpecASTLexer;
import org.mskcc.portal.oncoPrintSpecLanguage.completeOncoPrintSpecASTParser;
import org.mskcc.portal.oncoPrintSpecLanguage.completeOncoPrintSpecASTwalker;

/**
 * Tests the parser version that generates and walks an AST.
 *
 * @author Arthur Goldberg
 */
public class TestOncoPrintSpecificationInParser extends TestCase{

   @Test
   public void testBadGeneNameInASTParser() {
      OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
      theOncoPrintGeneDisplaySpec.setDefault( 1.0 );
      ParserOutput theParserOutput = CallOncoPrintSpecParser.callOncoPrintSpecParser
              ( "2-PDE", theOncoPrintGeneDisplaySpec );
   }

    @Test
    public void testContinuousDataTypeInequalityInParser() {

        tryAcontinuousDataTypeInequality(GeneticDataTypes.Expression,
                ComparisonOp.LessEqual, 1.3f, "Expression <= 1.3");
        tryAcontinuousDataTypeInequality(GeneticDataTypes.Methylation,
                ComparisonOp.Greater, 1f, "Methylation > 1");
        tryAcontinuousDataTypeInequality(GeneticDataTypes.Expression,
                ComparisonOp.LessEqual, 0f, "Expression <= 0");
        tryAcontinuousDataTypeInequality(GeneticDataTypes.Expression,
                ComparisonOp.GreaterEqual, -1.5f, "Expression >=-1.5");

        // Abbreviations
        tryAcontinuousDataTypeInequality(GeneticDataTypes.Expression,
                ComparisonOp.LessEqual, 1.3f, "Exp<= 1.3");
        tryAcontinuousDataTypeInequality(GeneticDataTypes.Methylation,
                ComparisonOp.Greater, 1f, "Me>1");
        tryAcontinuousDataTypeInequality(GeneticDataTypes.Expression,
                ComparisonOp.LessEqual, 0f, "E<= 0");
        tryAcontinuousDataTypeInequality(GeneticDataTypes.Expression,
                ComparisonOp.GreaterEqual, -1.5f, "Ex>=-1.5");

        // nicknames
        tryAcontinuousDataTypeInequality( GeneticDataTypes.Expression,
              ComparisonOp.LessEqual, 1.3f , "MRNA<= 1.3" );
        tryAcontinuousDataTypeInequality( GeneticDataTypes.Expression,
              ComparisonOp.LessEqual, 1.3f , "microRNA<= 1.3" );
        tryAcontinuousDataTypeInequality( GeneticDataTypes.Expression,
                ComparisonOp.LessEqual, 1.3f , "MR<= 1.3" );

        // error handling
        // semantic error, generate by the tree grammar
        tryErroneousProduction( "continuousDataTypeInequality", "barf < 1.3", "Error at char 1 of line 1: " + 
               "'barf' is not a valid genetic data type.");
    }

    private void tryAcontinuousDataTypeInequality(
          GeneticDataTypes aGeneticDataTypes, ComparisonOp aComparisonOp,
          float t, String testLangFragment) {
      try {

          ContinuousDataTypeSpec aContinuousDataTypeSpec = new ContinuousDataTypeSpec(
                  aGeneticDataTypes, aComparisonOp, t);
          
          Assert.assertEquals(aContinuousDataTypeSpec, 
                (ContinuousDataTypeSpec) parseMethod( "continuousDataTypeInequality", testLangFragment) );

      } catch (RecognitionException e) {
          System.out.println("testcontinuousDataTypeInequality: RecognitionException: " + e.getMessage());
          e.printStackTrace();
      } catch (Throwable e) {
         e.printStackTrace();
      }
  }

    @Test
    public void testDiscreteDataTypeInParser() {

        tryADiscreteDataType(GeneticDataTypes.CopyNumberAlteration,
                ComparisonOp.LessEqual, GeneticTypeLevel.Gained, "CopyNumberAlteration <= Gain");

        // Abbreviations
        tryADiscreteDataType(GeneticDataTypes.CopyNumberAlteration,
                ComparisonOp.Less, GeneticTypeLevel.Diploid, "cna < Di");
        tryADiscreteDataType(GeneticDataTypes.CopyNumberAlteration,
                ComparisonOp.GreaterEqual, GeneticTypeLevel.HomozygouslyDeleted, "C >= Hom");
        
        // nicknames
        tryADiscreteDataType(GeneticDataTypes.CopyNumberAlteration,
                ComparisonOp.Greater, GeneticTypeLevel.HemizygouslyDeleted, "CNA > Hetloss");
        tryADiscreteDataType(GeneticDataTypes.CopyNumberAlteration,
                ComparisonOp.Less, GeneticTypeLevel.HomozygouslyDeleted, "CNA < Homdel");
        tryADiscreteDataType(GeneticDataTypes.CopyNumberAlteration,
                ComparisonOp.Less, GeneticTypeLevel.HomozygouslyDeleted, "CNA < hom");
        
        // aDiscreteDataTypeSetSpecs
        tryADiscreteDataType(GeneticDataTypes.CopyNumberAlteration,
                GeneticTypeLevel.HemizygouslyDeleted, "C -1");
        tryADiscreteDataType(GeneticDataTypes.CopyNumberAlteration,
                GeneticTypeLevel.Amplified, "C 2");
        
        // test errors
        tryErroneousProduction( "discreteDataType", "Foo <= blah", "Error at char 1 of line 1: 'Foo <= blah' " +
           "is not a valid discrete genetic data type and discrete genetic data level." );
        tryErroneousProduction( "discreteDataType", "\nExp <= blah", "Error at char 1 of line 2: 'Exp <= blah' " +
           "is not a valid discrete genetic data type and discrete genetic data level." );
        tryErroneousProduction( "discreteDataType", "CNA 5", "Error at char 1 of line 1: 'CNA 5' is " +
                "not a valid genetic data type and GISTIC code." );
    }

    private void tryADiscreteDataType(
            GeneticDataTypes aGeneticDataTypes, ComparisonOp aComparisonOp,
            GeneticTypeLevel aGeneticTypeLevel, String testLangFragment) {

        try {

            DiscreteDataTypeSpec aDiscreteDataTypeSpec = new DiscreteDataTypeSpec(
                    aGeneticDataTypes, aComparisonOp, aGeneticTypeLevel);
            Assert.assertTrue(aDiscreteDataTypeSpec
                    .equals((DiscreteDataTypeSpec) parseMethod( "discreteDataType", testLangFragment) ));
            
        } catch (RecognitionException e) {
            System.out.println("testDiscreteDataType: RecognitionException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable e) {
         e.printStackTrace();
      }
    }
    
    // overloaded; this for DiscreteDataTypeSetSpecs
    private void tryADiscreteDataType(
            GeneticDataTypes aGeneticDataTypes, GeneticTypeLevel aGeneticTypeLevel, String testLangFragment) {
        try {

            DiscreteDataTypeSetSpec aDiscreteDataTypeSetSpec = new DiscreteDataTypeSetSpec(
                    aGeneticDataTypes, aGeneticTypeLevel);
            Assert.assertTrue( aDiscreteDataTypeSetSpec
                    .equals((DiscreteDataTypeSetSpec) parseMethod( "discreteDataType", testLangFragment) ));
            
        } catch (RecognitionException e) {
            System.out.println("testDiscreteDataType: RecognitionException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable e) {
         e.printStackTrace();
      }
    }

    @Test
    public void testDataTypeSpecInParser() {
        
        try {
            // overloaded several times: these test alternative: discreteDataType
            tryADataTypeSpec(GeneticDataTypes.CopyNumberAlteration,
                    ComparisonOp.LessEqual, GeneticTypeLevel.Gained, "CopyNumberAlteration <= Gain");

            // Abbreviations
            tryADataTypeSpec(GeneticDataTypes.CopyNumberAlteration,
                    ComparisonOp.Less, GeneticTypeLevel.Diploid, "cna < Di");
            tryADataTypeSpec(GeneticDataTypes.CopyNumberAlteration,
                    ComparisonOp.GreaterEqual, GeneticTypeLevel.HomozygouslyDeleted, "C >= Hom");
            
            // nicknames
            tryADataTypeSpec(GeneticDataTypes.CopyNumberAlteration,
                    ComparisonOp.Greater, GeneticTypeLevel.HemizygouslyDeleted, "CNA > Hetloss");
            tryADataTypeSpec(GeneticDataTypes.CopyNumberAlteration,
                    ComparisonOp.Less, GeneticTypeLevel.HomozygouslyDeleted, "CNA < Homdel");
            tryADataTypeSpec(GeneticDataTypes.CopyNumberAlteration,
                    ComparisonOp.Less, GeneticTypeLevel.HomozygouslyDeleted, "CNA < hom");
            
            // aDiscreteDataTypeSetSpecs
            tryADataTypeSpec(GeneticDataTypes.CopyNumberAlteration,
                    GeneticTypeLevel.HemizygouslyDeleted, "C -1");
            tryADataTypeSpec(GeneticDataTypes.CopyNumberAlteration,
                    GeneticTypeLevel.Amplified, "C 2");

            tryADataTypeSpec(GeneticDataTypes.Expression,
                    ComparisonOp.LessEqual, 1.3f, "Expression <= 1.3");
            tryADataTypeSpec(GeneticDataTypes.Methylation,
                    ComparisonOp.Greater, 1f, "Methylation > 1");
            tryADataTypeSpec(GeneticDataTypes.Expression,
                    ComparisonOp.LessEqual, 0f, "Expression <= 0");
            tryADataTypeSpec(GeneticDataTypes.Expression,
                    ComparisonOp.GreaterEqual, -1.5f, "Expression >=-1.5");

            // Abbreviations
            tryADataTypeSpec(GeneticDataTypes.Expression,
                    ComparisonOp.LessEqual, 1.3f, "Exp<= 1.3");
            tryADataTypeSpec(GeneticDataTypes.Methylation,
                    ComparisonOp.Greater, 1f, "Me>1");
            tryADataTypeSpec(GeneticDataTypes.Expression,
                    ComparisonOp.LessEqual, 0f, "E<= 0");
            tryADataTypeSpec(GeneticDataTypes.Expression,
                    ComparisonOp.GreaterEqual, -1.5f, "Ex>=-1.5");

            // nicknames
            tryADataTypeSpec( GeneticDataTypes.Expression,
                  ComparisonOp.LessEqual, 1.3f , "MRNA<= 1.3" );
            tryADataTypeSpec( GeneticDataTypes.Expression,
                  ComparisonOp.LessEqual, 1.3f , "micro<= 1.3" );
            tryADataTypeSpec( GeneticDataTypes.Expression,
                    ComparisonOp.LessEqual, 1.3f , "MR<= 1.3" );

        } catch (RecognitionException e) {
            e.printStackTrace();
        }
    }
    
    // overloaded several times: this tests alternative:    dataTypeName // may be discrete or continuous
    private void tryADataTypeSpec( GeneticDataTypes aGeneticDataTypes, String testLangFragment)
            throws RecognitionException{
            ConcreteDataTypeSpec aConcreteDataTypeSpec = new ConcreteDataTypeSpec( aGeneticDataTypes );
            try {
               Assert.assertTrue( aConcreteDataTypeSpec.equals((ConcreteDataTypeSpec) parseMethod
                       ( "dataTypeSpec", testLangFragment) ));
            } catch (Throwable e) {
               e.printStackTrace();
            }
    }
    
    // overloaded several times: this tests alternative: dataTypeLevel  // must be discrete
    private void tryADataTypeSpec( GeneticDataTypes aGeneticDataType, GeneticTypeLevel aGeneticTypeLevel,
            String testLangFragment) throws RecognitionException{
            DiscreteDataTypeSetSpec aDiscreteDataTypeSetSpec = new DiscreteDataTypeSetSpec( aGeneticDataType,
                    aGeneticTypeLevel );
            try {
               Assert.assertTrue( aDiscreteDataTypeSetSpec.equals((DiscreteDataTypeSetSpec) parseMethod
                       ( "dataTypeSpec", testLangFragment) ));
            } catch (Throwable e) {
               e.printStackTrace();
            }
    }

    // overloaded several times: this tests alternative: discreteDataType      
    private void tryADataTypeSpec( GeneticDataTypes aGeneticDataType, ComparisonOp aComparisonOp,
            GeneticTypeLevel aGeneticTypeLevel,
            String testLangFragment) throws RecognitionException{
            DiscreteDataTypeSpec aDiscreteDataTypeSpec = new DiscreteDataTypeSpec(
                    aGeneticDataType, aComparisonOp, aGeneticTypeLevel);
            try {
               Assert.assertTrue(aDiscreteDataTypeSpec
                       .equals((DiscreteDataTypeSpec) parseMethod( "dataTypeSpec", testLangFragment) ));
            } catch (Throwable e) {
               e.printStackTrace();
            }
    }

    // overloaded several times: this tests alternative: continuousDataTypeInequality
    private void tryADataTypeSpec( GeneticDataTypes aGeneticDataType, ComparisonOp aComparisonOp, float threshold,
            String testLangFragment) throws RecognitionException{
        ContinuousDataTypeSpec aContinuousDataTypeSpec = new ContinuousDataTypeSpec(
                aGeneticDataType, aComparisonOp, threshold);
        try {
         Assert.assertTrue(aContinuousDataTypeSpec
                   .equals((ContinuousDataTypeSpec) parseMethod( "dataTypeSpec", testLangFragment) ));
      } catch (Throwable e) {
         e.printStackTrace();
      }
    }

    @Test
    public void testFullDataTypeSpecInParser() {        
        try {
           OncoPrintGeneDisplaySpec theResultFullDataTypeSpec = (OncoPrintGeneDisplaySpec)parseMethod
                   ( "fullDataTypeSpec", ": Mutation Mut Meth  HetLoss Exp<-1   " +
                    "HetLoss Exp>=1.5  Exp <= -1.5   C -2 Expression<-2; " ); 
            Assert.assertTrue( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.Mutation, null ));

            Assert.assertTrue( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.Methylation, 2f ));
            Assert.assertTrue( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.Methylation, -33f ));

            Assert.assertTrue( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.Expression, -3f ));
            Assert.assertFalse( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.Expression, -1f ));
            Assert.assertTrue( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.Expression, 1.5f ));
            Assert.assertTrue( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.Expression, 3f ));
            
            Assert.assertTrue( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.CopyNumberAlteration,
                    GeneticTypeLevel.HemizygouslyDeleted ));
            Assert.assertTrue( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.CopyNumberAlteration,
                    GeneticTypeLevel.HomozygouslyDeleted ));
            
            theResultFullDataTypeSpec = (OncoPrintGeneDisplaySpec)parseMethod( "fullDataTypeSpec", ": C ; " );
            Assert.assertTrue( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.CopyNumberAlteration,
                    GeneticTypeLevel.HemizygouslyDeleted ));
            Assert.assertTrue( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.CopyNumberAlteration,
                    GeneticTypeLevel.HomozygouslyDeleted ));
            Assert.assertFalse( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.Expression, -1f ));
            
            theResultFullDataTypeSpec = (OncoPrintGeneDisplaySpec)parseMethod( "fullDataTypeSpec", ": C Exp; " );
            Assert.assertTrue( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.CopyNumberAlteration,
                    GeneticTypeLevel.HemizygouslyDeleted ));
            Assert.assertTrue( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.CopyNumberAlteration,
                    GeneticTypeLevel.HomozygouslyDeleted ));
            Assert.assertTrue( theResultFullDataTypeSpec.satisfy(GeneticDataTypes.Expression, -1f ));
            
        } catch (RecognitionException e) {
            e.printStackTrace();
        } catch (Throwable e) {
         e.printStackTrace();
      }
        
        // try error
        // this exercises the error produced by dataTypeSpec, which I cannot trigger with that production
        tryErroneousProduction( "fullDataTypeSpec", ":FOOL;", "Error at char 2 of line 1: " +
                "'FOOL' is not a valid genetic data type or data level." );
    }
    
    @Test
    public void testGeneListInParser() {
        try {
            GeneSet theGeneSet = (GeneSet) parseMethod( "userGeneList", 
                    " DATATYPES : CNA Mutation C -2 Expression<-2; amazin " + 
                    "g5 : CNA Hetloss Homdel Expression <-1;" +
                    " DATATYPES : Meth C<Dip; foo " );
            ArrayList<GeneWithSpec> theGenes = theGeneSet.getGenes();
            checkAgeneWithSpec( theGenes.get(0) );

            GeneWithSpec aGeneWithSpec = theGenes.get(2); 
            Assert.assertTrue(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy(
                    GeneticDataTypes.Methylation));
            Assert.assertTrue(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy
                    (GeneticDataTypes.CopyNumberAlteration,
                    GeneticTypeLevel.HomozygouslyDeleted ));
            Assert.assertTrue(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy
                    (GeneticDataTypes.CopyNumberAlteration,
                    GeneticTypeLevel.HemizygouslyDeleted ));

        } catch (RecognitionException e) {
            e.printStackTrace();
        } catch (Throwable e) {
         e.printStackTrace();
      }
    }
    
    @Test
    public void testIndividualGeneInParser() {
        try {
            GeneWithSpec aGeneWithSpec = (GeneWithSpec) parseMethod( "individualGene", "amazin : " +
                    "CNA Mutation C -2 Expression<-2; ");
            checkAgeneWithSpec(aGeneWithSpec);

            aGeneWithSpec = (GeneWithSpec) parseMethod( "individualGene", 
                    " P53: Mutated Methy C -2 G Expression<-2 Expression<-3 Expression>=3.2; ");
            Assert.assertTrue(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy(GeneticDataTypes.Methylation));
            Assert.assertTrue(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy(GeneticDataTypes.Mutation,
                    GeneticTypeLevel.Mutated ));
            Assert.assertTrue(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy
                    (GeneticDataTypes.CopyNumberAlteration,
                    GeneticTypeLevel.HomozygouslyDeleted ));
            Assert.assertTrue(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy
                    (GeneticDataTypes.CopyNumberAlteration,
                    GeneticTypeLevel.Gained ));
            Assert.assertTrue(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy
                    (GeneticDataTypes.Expression, -2.01f));
            Assert.assertFalse(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy
                    (GeneticDataTypes.Expression, -2.0f));
            
        } catch (RecognitionException e) {
            e.printStackTrace();
        } catch (Throwable e) {
         e.printStackTrace();
        }
        
        // test errors
        tryErroneousProduction( "individualGene", "notGene", "Error at char 1 of line 1: " +
                "'notGene' is not a valid gene or microRNA name.");
    }

    private void checkAgeneWithSpec(GeneWithSpec aGeneWithSpec){

       Assert.assertTrue(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy(
               GeneticDataTypes.CopyNumberAlteration));
       Assert.assertTrue(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy(GeneticDataTypes.Mutation));
       Assert.assertTrue(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy(GeneticDataTypes.Expression, -2.01f));
       Assert.assertFalse(aGeneWithSpec.getTheOncoPrintGeneDisplaySpec().satisfy(GeneticDataTypes.Expression, -2.0f));
       
   }
   
    @Test
    public void testGroupedGeneListInParser() {
        try {
            String n = "P53 pathway";
            String nInQuotes = '"' + n + '"';
            String inputNames = "CCND1 CDKN2B CDKN2A";
            String[] names = inputNames.split(" ");
            
            GeneSet theGeneSet = (GeneSet) parseMethod( "userGeneList", nInQuotes + " {  " + inputNames + " } ");
            Assert.assertEquals( n, theGeneSet.getName() );
            ArrayList<GeneWithSpec> theGenes = theGeneSet.getGenes();
            int i=0;
            for( GeneWithSpec aGeneWithSpec : theGenes ){
                Assert.assertEquals( names[i++], aGeneWithSpec.getName() );
            }

            theGeneSet = (GeneSet) parseMethod( "userGeneList", " {  " + inputNames + " } ");
            theGenes = theGeneSet.getGenes();
            i=0;
            for( GeneWithSpec aGeneWithSpec : theGenes ){
                Assert.assertEquals( names[i++], aGeneWithSpec.getName() );
            }

            theGeneSet = (GeneSet) parseMethod( "userGeneList",  inputNames );
            theGenes = theGeneSet.getGenes();
            i=0;
            for( GeneWithSpec aGeneWithSpec : theGenes ){
                Assert.assertEquals( names[i++], aGeneWithSpec.getName() );
            }
        } catch (RecognitionException e) {
            e.printStackTrace();
        } catch (Throwable e) {
         e.printStackTrace();
      }
    }
    
    /*
     * TODO: IMPORTANT; MAKE TEST OPERATIONAL
     */
    @Test
    public void testOncoPrintSpecificationInParser() {
           
        try {
         ArrayList<GeneSet> expectedGeneSets = new ArrayList<GeneSet>();
           OncoPrintGeneDisplaySpec r = new OncoPrintGeneDisplaySpec();
           r.setDefault( 1.0f );
           GeneWithSpec g = new GeneWithSpec( "gene2" );
           g.setTheResultFullDataTypeSpec(r);
           GeneSet h = new GeneSet( );
           h.addGeneWithSpec(g);
           GeneWithSpec aGeneWithSpec = (GeneWithSpec) parseMethod( "individualGene",
                   "gene3 : Hetloss Homdel Expression <-1;");
           h.addGeneWithSpec(aGeneWithSpec);
           expectedGeneSets.add( h );

           GeneSet theGeneSet = (GeneSet) parseMethod( "userGeneList",
                   " { DATATYPES: AMP homo EXP<-1 exp>1 MUTated ; JAG1 JAG2 }" );
           expectedGeneSets.add( theGeneSet );

           OncoPrintGeneDisplaySpec theResultFullDataTypeSpec = (OncoPrintGeneDisplaySpec)parseMethod
                   ( "fullDataTypeSpec", ": G Amp Mutated Expression <=-2; ");
           h = new GeneSet( );
           g = new GeneWithSpec( "FOO" );
           g.setTheResultFullDataTypeSpec(theResultFullDataTypeSpec);
           h.addGeneWithSpec(g);
           expectedGeneSets.add( h );
           
           theGeneSet = (GeneSet) parseMethod( "userGeneList",
                "\"P53 pathway\" { DATATYPES: G Amp Mutated Expression <=-2; CCND1 " +
           		"DATATYPES: Di G Am Mut E<=-2; CDKN2B  CDKN2A  }" +
           		"AR: C-1");
           expectedGeneSets.add( theGeneSet );
           
           OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
           theOncoPrintGeneDisplaySpec.setDefault( 1.0 );
           ParserOutput theParserOutput = CallOncoPrintSpecParser.callOncoPrintSpecParser
                   ("gene2 gene3 : Hetloss Homdel Expression <-1; " +
                    "{ JAG1 JAG2 } " +
                    "DATATYPES: G Amp Mutated Expression <=-2; FOO" +
                    "\"P53 pathway\" { CCND1 DATATYPES: Di G Am Mut E<=-2; CDKN2B CDKN2A  }",
                    theOncoPrintGeneDisplaySpec );
           
           Assert.assertEquals( 0, theParserOutput.getSemanticsErrors().size() );
           Iterator<GeneSet> theIterator = expectedGeneSets.iterator();
           for( GeneSet gs: theParserOutput.getTheOncoPrintSpecification().getGeneSets() ){
              
              // TODO HIGH: FIX THIS; DOESN'T WORK
              // Assert.assertEquals( theIterator.next().toString(), gs.toString() );
           }
        } catch (RecognitionException e) {
           e.printStackTrace();
      } catch (Throwable e) {
         e.printStackTrace();
      }
    }

    /**
     * try calling the root rule (production) in the ASTwalker, 
     * oncoPrintSpecification
     */ 
   @Test
   public void testErrorHandlingByOncoPrintSpecificationInASTwalker() {

      // should return a list of errors
      String[][] errorFilledInputAndErrors = {
            // rule input error; the extra stuff like "DATATYPES: CNA ; " \
            // in the input is needed for the parser to recover; don't change it
            { "continuousDataTypeInequality", "gene: barf < 1.3 ;",
                  "Error at char 7 of line 1: 'barf' is not a valid genetic data type." },
            {
                  "discreteDataType",
                  "g2:Foo <= blah; DATATYPES: CNA ;  { X }",
                  "Error at char 4 of line 1: 'Foo <= blah' is not a valid discrete genetic data " +
                          "type and discrete genetic data level." },
            {
                  "discreteDataType",
                  "g3:Exp <= blah;  DATATYPES: CNA ;  ",
                  "Error at char 4 of line 1: 'Exp <= blah' is not a valid discrete genetic data " +
                          "type and discrete genetic data level." },
            { "discreteDataType", "g4: CNA 5;  DATATYPES: CNA ; { X }",
                  "Error at char 5 of line 1: 'CNA 5' is not a valid genetic data type and GISTIC code." },
            { "fullDataTypeSpec", "DATATYPES:FOOL; DATATYPES: CNA ;  ",
                  "Error at char 11 of line 1: 'FOOL' is not a valid genetic data type or data level." },
      };

      String[] errors  = new String[1];
      // try each error
      for (String[] ruleInputError : errorFilledInputAndErrors) {
         errors[0] = ruleInputError[2];
         tryOncoPrintSpecification(ruleInputError[1], errors);
      }

      // try all errors
      StringBuffer langFrag = new StringBuffer();
      errors = new String[ errorFilledInputAndErrors.length ];
      int i=0;
      for (String[] ruleInputError : errorFilledInputAndErrors) {
         langFrag.append(ruleInputError[1]+"\n" );
         // increment line #s
         Integer n = new Integer( i+1 );
         errors[i++ ] = ruleInputError[2].replaceFirst("line 1", "line " + n.toString() ) ;
      }
      tryOncoPrintSpecification(langFrag.toString(), errors);
   }

   private void tryOncoPrintSpecification(String testLangFragment, String[] errorMsgs) {
      OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
      theOncoPrintGeneDisplaySpec.setDefault( 1.0 );
      ParserOutput theParserOutput = CallOncoPrintSpecParser.callOncoPrintSpecParser( testLangFragment,
              theOncoPrintGeneDisplaySpec );
      ArrayList<OncoPrintLangException> listOfErrors = theParserOutput.getSemanticsErrors();
      Iterator<OncoPrintLangException> i = listOfErrors.iterator();
      for (String s : errorMsgs) {
         Assert.assertEquals(s, i.next().getMessage() );
      }
   }

   /**
     * check that IllegalArgumentException with appropriate error message is thrown
     */
    private void tryErroneousProduction( String production, String testLangFragment, String errorMsg ) {
       try {
          parseMethod( production, testLangFragment);
       } catch (Throwable e) {
          if( !(e instanceof OncoPrintLangException)){
             System.out.println( e.getClass().getName() );
             e.printStackTrace();
          }
          Assert.assertTrue(e instanceof OncoPrintLangException);
          Assert.assertEquals( errorMsg, e.getMessage() );
       }
   }
    
    /**
     * call method 'method' in the lexer and parser;
     * loads of reflection
     */
   public static Object parseMethod(String method, String prog) throws Throwable {

      // Create a stream of tokens pulled from the lexer
      CommonTokenStream tokens;
      // Create a parser attached to the token stream
      completeOncoPrintSpecASTParser parser;
      // create (empty) arg list
      Class<?>[] emptyArgList;
      // get method
      Method parserMethod;
      Object parserRV;
      try {
         ByteArrayInputStream bs = new ByteArrayInputStream(prog.getBytes());
         ANTLRInputStream input = new ANTLRInputStream(bs);
         // Create a lexer attached to that input stream
         completeOncoPrintSpecASTLexer lexer = new completeOncoPrintSpecASTLexer(input);
         tokens = new CommonTokenStream(lexer);
         parser = new completeOncoPrintSpecASTParser(tokens);

         // Invoke the program rule in get return value
         // get parser class
         Class<?> parserClass = parser.getClass();
         emptyArgList = new Class[] {};
         parserMethod = parserClass.getDeclaredMethod(method, emptyArgList);

         // was completeOncoPrintSpecASTParser.fullDataTypeSpec_return r =
         // parser.fullDataTypeSpec();
         parserRV = parserMethod.invoke(parser);
      } catch (InvocationTargetException e) {
         throw e.getCause();
      }

      try {
         // now call returnedObject's getTree()
         // get return value class
         Class<?> rvClass = parserRV.getClass();

         // get method
         Method rvClassGetTree = rvClass.getDeclaredMethod("getTree", emptyArgList);
         // call method
         // was: CommonTree t = (CommonTree)r.getTree();
         CommonTree theCommonTree = (CommonTree) rvClassGetTree.invoke(parserRV);

         // Walk resulting tree; create treenode stream first
         CommonTreeNodeStream nodes = new CommonTreeNodeStream(theCommonTree);
         // AST nodes have payloads that point into token stream
         nodes.setTokenStream(tokens);
         // Create a tree Walker attached to the nodes stream
         completeOncoPrintSpecASTwalker walker = new completeOncoPrintSpecASTwalker(nodes);

         // Invoke the start symbol, rule program
         // invoke method 'method' on walker
         return walker.getClass().getDeclaredMethod(method, emptyArgList)
               .invoke(walker, (Object[]) null);
         // was: return walker.fullDataTypeSpec();
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (NoSuchMethodException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         throw e.getCause();
      }
      return null;
   }   
}