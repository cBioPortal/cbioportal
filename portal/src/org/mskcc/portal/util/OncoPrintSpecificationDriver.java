package org.mskcc.portal.util;

import java.util.ArrayList;
import java.util.HashSet;

import org.mskcc.portal.oncoPrintSpecLanguage.CallOncoPrintSpecParser;
import org.mskcc.portal.oncoPrintSpecLanguage.GeneticDataTypes;
import org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintGeneDisplaySpec;
import org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintLangException;
import org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintSpecification;
import org.mskcc.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.cgds.model.GeneticAlterationType;
import org.mskcc.cgds.model.GeneticProfile;

public class OncoPrintSpecificationDriver {

   /**
    * A wrapper for the OncoPrintSpec parser, which includes the list of genetic
    * data types input in the user's checkboxes. Performs two complementary
    * functions:
    * <ul>
    * <LI>LimitLanguageDefaultToProfile: Limit the specification returned by the
    * language to the genetic data types that the user selects in the
    * checkboxes.
    * <LI>LanguageCannotOverrunProfile: If the user explicitly selects a data
    * type in the language, but does not choose it in the checkboxes, add an
    * error.
    * </ul>
    * <p>
    * 
    * @param geneListStr
    *           an OncoPrintSpec specification
    * @param geneticProfileIdSet
    *           profile strings entered by the user
    * @param profileList
    *           all profiles for the selected cancer type
    * @return output from parsing geneListStr 
    */
   static public ParserOutput callOncoPrintSpecParserDriver(String geneListStr, HashSet<String> geneticProfileIdSet,
            ArrayList<GeneticProfile> profileList, double zScoreThreshold, double rppaScoreThreshold) {

      // I. LimitLanguageDefaultToProfile: Create an OncoPrintGeneDisplaySpec
      // from the geneticProfileIdSet, and use that as the default for the parse

      // make an OncoPrintGeneDisplaySpec, from geneticProfileIdSet, profileList and zScoreThreshold 
      OncoPrintGeneDisplaySpec checkboxInputOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
      // TODO: replace when GeneticAlterationType is made a true enumeration
      for (GeneticProfile theGeneticProfile : profileList) {

         if (theGeneticProfile.getGeneticAlterationType() == GeneticAlterationType.COPY_NUMBER_ALTERATION
                  && geneticProfileIdSet.contains(theGeneticProfile.getStableId())) {
            checkboxInputOncoPrintGeneDisplaySpec.setDefault( GeneticDataTypes.CopyNumberAlteration );
         }

         if (theGeneticProfile.getGeneticAlterationType() == GeneticAlterationType.MRNA_EXPRESSION
                  && geneticProfileIdSet.contains(theGeneticProfile.getStableId())) {
            checkboxInputOncoPrintGeneDisplaySpec.setDefaultExpression(zScoreThreshold, GeneticDataTypes.Expression);
         }

         if (theGeneticProfile.getGeneticAlterationType() == GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL
                  && geneticProfileIdSet.contains(theGeneticProfile.getStableId())) {
            checkboxInputOncoPrintGeneDisplaySpec.setDefaultExpression(rppaScoreThreshold, GeneticDataTypes.RPPA);
         }
         
         if ( (theGeneticProfile.getGeneticAlterationType() == GeneticAlterationType.MUTATION_EXTENDED )
                  && geneticProfileIdSet.contains(theGeneticProfile.getStableId())) {
            checkboxInputOncoPrintGeneDisplaySpec.setDefault( GeneticDataTypes.Mutation );
         }
      }

      ParserOutput theOncoPrintSpecParserOutput = CallOncoPrintSpecParser.callOncoPrintSpecParser( 
               geneListStr, checkboxInputOncoPrintGeneDisplaySpec );      

      // II. LanguageCannotOverrunProfile: If the OncoSpec asked for data types
      // not in the geneticProfileIdSet, add semantics error(s)
      OncoPrintSpecification anOncoPrintSpecification = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification();
      ArrayList<OncoPrintLangException> theSemanticsErrors = theOncoPrintSpecParserOutput.getSemanticsErrors();

      OncoPrintGeneDisplaySpec unionOncoPrintGeneDisplaySpec = anOncoPrintSpecification.getUnionOfPossibleLevels();
      for( GeneticDataTypes aGeneticDataType : GeneticDataTypes.values() ){
         if( unionOncoPrintGeneDisplaySpec.typeDifference( checkboxInputOncoPrintGeneDisplaySpec, aGeneticDataType ) ){
            theSemanticsErrors.add( new OncoPrintLangException( "Error: " +  aGeneticDataType + 
                     " specified in the list of genes, but not selected in the Genetic Profile Checkboxes." ) );
         }
      }
      return theOncoPrintSpecParserOutput;

   }

   /**
    * A wrapper for the OncoPrintSpec parser, which includes the list of genetic
    * data types input in the user's checkboxes. Performs two complementary
    * functions:
    * <ul>
    * <LI>LimitLanguageDefaultToProfile: Limit the specification returned by the
    * language to the genetic data types that the user selects in the
    * checkboxes.
    * <LI>LanguageCannotOverrunProfile: If the user explicitly selects a data
    * type in the language, but does not choose it in the checkboxes, add an
    * error.
    * </ul>
    * <p>
    *
    * @param geneListStr an OncoPrintSpec specification
    * @return output from parsing geneListStr
    */
   static public ParserOutput callOncoPrintSpecParserDriver(String geneListStr) {
      OncoPrintGeneDisplaySpec checkboxInputOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
      ParserOutput theOncoPrintSpecParserOutput = CallOncoPrintSpecParser.callOncoPrintSpecParser(
               geneListStr, checkboxInputOncoPrintGeneDisplaySpec );

      // II. LanguageCannotOverrunProfile: If the OncoSpec asked for data types
      // not in the geneticProfileIdSet, add semantics error(s)
      OncoPrintSpecification anOncoPrintSpecification =
              theOncoPrintSpecParserOutput.getTheOncoPrintSpecification();
      ArrayList<OncoPrintLangException> theSemanticsErrors =
              theOncoPrintSpecParserOutput.getSemanticsErrors();

      OncoPrintGeneDisplaySpec unionOncoPrintGeneDisplaySpec =
              anOncoPrintSpecification.getUnionOfPossibleLevels();
      for( GeneticDataTypes aGeneticDataType : GeneticDataTypes.values() ){
         if(unionOncoPrintGeneDisplaySpec.typeDifference
                 (checkboxInputOncoPrintGeneDisplaySpec, aGeneticDataType ) ){
            theSemanticsErrors.add( new OncoPrintLangException
                    ("Error: " +  aGeneticDataType +
                     " specified in the list of genes, but not selected in " +
                            "the Genetic Profile Checkboxes." ) );
         }
      }
      return theOncoPrintSpecParserOutput;
   }
}