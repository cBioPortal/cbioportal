package org.mskcc.portal.util;

import java.util.HashMap;
import org.mskcc.portal.oncoPrintSpecLanguage.ComparisonOp;
import org.mskcc.portal.oncoPrintSpecLanguage.ConcreteDataTypeSpec;
import org.mskcc.portal.oncoPrintSpecLanguage.ContinuousDataTypeSpec;
import org.mskcc.portal.oncoPrintSpecLanguage.DataTypeSpecEnumerations.DataTypeCategory;
import org.mskcc.portal.oncoPrintSpecLanguage.GeneWithSpec;
import org.mskcc.portal.oncoPrintSpecLanguage.GeneticDataTypes;
import org.mskcc.portal.oncoPrintSpecLanguage.GeneticTypeLevel;
import org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintGeneDisplaySpec;
import org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintSpecification;
import org.mskcc.portal.oncoPrintSpecLanguage.ParsedFullDataTypeSpec;
import org.mskcc.portal.oncoPrintSpecLanguage.ResultDataTypeSpec;
import org.mskcc.cgds.model.GeneticAlterationType;

/**
 * Takes a String encoding of genetic data created by ProfileMerger, and some
 * definition of the threshold for up or down regulation of mRNA expression and
 * an optional filter for which datatypes and levels should be viewed, and then
 * exports a set of boolean functions, such as isCnaAmplified() or
 * isCnaHomozygouslyDeleted(), which indicates whether the genetic data meets
 * the boolean characteristics.
 * <p>
 * DEFAULT_THRESHOLD: The default definition of mRNA thresholds is provided by a
 * zScore, symmetric about 0. The optional filter is provided as an
 * OncoPrintGeneDisplaySpec, which would typically be the parsed output of a
 * filter defined in the OncoPrintSpec language. If the filter defines any
 * inequalities on mRNA expression then it overrides the zScore thresholds.
 * <p>
 * Could be modified to implement GeneticEvent.
 * @author Arthur Goldberg
 */
public class ValueParser {
   private String                   originalValue;
   private HashMap<String, String>  datatypeToValueMap;
   private OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec;

   /**
    * Create a ValueParser. mRNA Expression, Copy Number Alterations and
    * Mutations will be viewable. Since only zScoreThreshold is provided, we use
    * it to set mRNA thresholds for over and under expression.
    * 
    * @param str
    * @param zScoreThreshold
    */
   public ValueParser(String str, double zScoreThreshold) {
      // TODO: get rid of this as soon as we can stop the calls
      // isCaseEpigeneticallySilenced()
      // ProfileData.getValueParsed()

      // create a 'default' OncoPrintGeneDisplaySpec, that displays all types
      // and thresholds high and low expression values
      // with zScoreThreshold, i.e., filter mRNA<= -zScoreThreshold
      // mRNA>=zScoreThreshold
      if (zScoreThreshold < 0.0) {
         throw new IllegalArgumentException("zScoreThreshold must be greater than 0");
      }
      ParsedFullDataTypeSpec aParsedFullDataTypeSpec = createParsedFullDataTypeSpecFromZscore(zScoreThreshold);

      // add other data types
      aParsedFullDataTypeSpec.addSpec(new ConcreteDataTypeSpec("CNA"));
      aParsedFullDataTypeSpec.addSpec(new ConcreteDataTypeSpec("Mutation"));

      this.theOncoPrintGeneDisplaySpec = aParsedFullDataTypeSpec.cleanUpInput();
      parseValue(str);
   }

   /**
    * create a ParsedFullDataTypeSpec with symmetric zScore thresholds.
    * 
    * @param zScoreThreshold
    * @return
    */
   private ParsedFullDataTypeSpec createParsedFullDataTypeSpecFromZscore(double zScoreThreshold) {
      ParsedFullDataTypeSpec aParsedFullDataTypeSpec = new ParsedFullDataTypeSpec();
      aParsedFullDataTypeSpec.addSpec(new ContinuousDataTypeSpec(GeneticDataTypes.Expression, ComparisonOp
               .convertCode("<="), (float) -zScoreThreshold));
      aParsedFullDataTypeSpec.addSpec(new ContinuousDataTypeSpec(GeneticDataTypes.Expression, ComparisonOp
               .convertCode(">="), (float) zScoreThreshold));
      return aParsedFullDataTypeSpec;
   }

   /**
    * Create a ValueParser that filters according to a given
    * OncoPrintGeneDisplaySpec.
    * 
    * @param str
    * @param theOncoPrintGeneDisplaySpec
    */
   public ValueParser(String str, OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec) {
      this.theOncoPrintGeneDisplaySpec = theOncoPrintGeneDisplaySpec;
      parseValue(str);
      // System.err.println( this.toString() );
   }

   /**
    * Generate a ValueParser. As a convenience, take a gene name and an
    * OncoPrintSpecification, and find the gene in the OncoPrintSpecification.
    * 
    * TODO: replace this: it's basically wrong because the same gene may
    * occur in multiple times in an OncoPrintSpecification
    * 
    * @param gene
    * @param value
    * @param zScoreThreshold
    * @param theOncoPrintSpecification
    * @return null if the gene cannot be found in theOncoPrintSpecification,
    *         otherwise a new ValueParser, as constructed by ValueParser( String
    *         value, double zScoreThreshold, OncoPrintGeneDisplaySpec
    *         theOncoPrintGeneDisplaySpec ) for theOncoPrintGeneDisplaySpec for
    *         the gene in theOncoPrintSpecification.
    */
   static public ValueParser generateValueParser(String gene, String value, double zScoreThreshold,
            OncoPrintSpecification theOncoPrintSpecification) {

      // check that gene can be found
      GeneWithSpec theGeneWithSpec = theOncoPrintSpecification.getGeneWithSpec(gene);
      if (null == theGeneWithSpec) {
         // System.err.println( "Cannot find " + gene + " in theOncoPrintSpecification.");
         return null;
      }
      return new ValueParser(value, zScoreThreshold, theGeneWithSpec.getTheOncoPrintGeneDisplaySpec());
   }

   // TODO: create a constructor that doesn't take a zScoreThreshold, to be used
   // when the caller knows that the OncoPrintGeneDisplaySpec
   // contains an Expression inequality

   /**
    * Given genetic data in value, instantiate a ValueParser that combines a
    * zScore threshold and an OncoPrintGeneDisplaySpec to determine which data
    * types and levels should be displayed, and what defines over and under mRNA
    * expression. See the DEFAULT_THRESHOLD logic in the class java doc.
    * <p>
    * TODO: create a value parser constructor that doesn't need a
    * zScoreThreshold, for use by code that has an OncoPrintGeneDisplaySpec with
    * a mRNA inequality
    * 
    * @param value
    * @param zScoreThreshold
    * @param theOncoPrintGeneDisplaySpec
    */
   public ValueParser(String value, double zScoreThreshold, OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec) {

      // if theOncoPrintGeneDisplaySpec shows Expression and does not define an
      // inequality on Expression ...
      // then use the zScore to determine Expression thresholds
      ResultDataTypeSpec theResultDataTypeSpec = theOncoPrintGeneDisplaySpec
               .getResultDataTypeSpec(GeneticDataTypes.Expression);
      if (null != theResultDataTypeSpec && null == theResultDataTypeSpec.getCombinedLesserContinuousDataTypeSpec()
               && null == theResultDataTypeSpec.getCombinedGreaterContinuousDataTypeSpec()) {

         ParsedFullDataTypeSpec aParsedFullDataTypeSpec = createParsedFullDataTypeSpecFromZscore(zScoreThreshold);

         // combine this with the given OncoPrintGeneDisplaySpec
         ResultDataTypeSpec expressionResultDataTypeSpec = aParsedFullDataTypeSpec.cleanUpInput()
                  .getResultDataTypeSpec(GeneticDataTypes.Expression);
         theOncoPrintGeneDisplaySpec.setResultDataTypeSpec(GeneticDataTypes.Expression, expressionResultDataTypeSpec);
      }
      this.theOncoPrintGeneDisplaySpec = theOncoPrintGeneDisplaySpec;
      parseValue(value);
   }

   private void parseValue(String str) {
      this.originalValue = str;
      datatypeToValueMap = new HashMap<String, String>();
      String fields[] = str.split( ProfileMerger.VALUE_SEPARATOR );
      for (String field : fields) {
         String parts[] = field.split( ProfileMerger.TYPE_VALUE_SEPARATOR, 2 );  // just split on the 1st colon, so that colon(s) within the value remain intact
         if (parts != null && parts.length == 2) {  // TODO: throw exception if this conditional isn't true
            String name = parts[0];
            String value = parts[1];
            datatypeToValueMap.put(name, value);
         }
      }
   }

   public String getOriginalValue() {
      return originalValue;
   }

   /**
    * does a discrete type have the given level? given the params, report
    * whether this genetic value has theGeneticTypeLevel, and that level is not
    * filtered by the OncoPrintSpec.
    * 
    * @param theDiscreteGeneticDataType
    * @param theGeneticTypeLevel
    * @return true, if the value for the given GeneticDataTypes has the given
    *         level, and the spec filter passes that level
    */
   public boolean isDiscreteTypeThisLevel(GeneticDataTypes theDiscreteGeneticDataType,
            GeneticTypeLevel theGeneticTypeLevel) {
      /*
       * basic idea: if the measurement is the right value, and it passes
       * through the filter then yes, else no.
       */

      // check that theDiscreteGeneticDataType is not Mutation
      if (theDiscreteGeneticDataType.equals(GeneticDataTypes.Mutation)) {
         throw new IllegalArgumentException("isDiscreteTypeThisLevel cannot handle Mutations");
      }

      // check that theDiscreteGeneticDataType is a discrete datatype
      if (!theDiscreteGeneticDataType.getTheDataTypeCategory().equals(DataTypeCategory.Discrete)) {
         throw new IllegalArgumentException("theDiscreteGeneticDataType is not a discrete datatype");
      }

      // TODO: make sure getValue() returns the right kind of int code
      String value = getValue(convertGeneticType(theDiscreteGeneticDataType));
      if (null == value) {
         return false;
      }

      GeneticTypeLevel theMeasuredGeneticTypeLevel;
      try {
         theMeasuredGeneticTypeLevel = GeneticTypeLevel
                  .convertCode(theDiscreteGeneticDataType, Integer.parseInt(value));
      } catch (NumberFormatException e) {
         return false;
      } catch (IllegalArgumentException e) {
         return false;
      }
      if (!theMeasuredGeneticTypeLevel.equals(theGeneticTypeLevel)) {
         return false;
      }
      return this.theOncoPrintGeneDisplaySpec.satisfy(theDiscreteGeneticDataType, theGeneticTypeLevel);
   }

   /**
    * return true if the measurement in theDiscreteGeneticDataType is altered,
    * that is, if it is one of the levels defined by the
    * OncoPrintGeneDisplaySpec. Note that this does not work for mutation,
    * because its values are not numeric codes.
    * 
    * @param theDiscreteGeneticDataType
    * @return
    */
   public boolean isDiscreteTypeAltered(GeneticDataTypes theDiscreteGeneticDataType) {
      // TODO: this is public only so it can be tested; todo: restructure tests
      // so they can access package level methods
      // consider approach in
      // http://junit.sourceforge.net/doc/faq/faq.htm#organize_1, and perhaps
      // http://www.artima.com/suiterunner/private3.html for testing private
      // methods

      /*
       * basic idea: false if there is no data or the measurement is NaN
       * otherwise, true if the measurement satisfies the
       * OncoPrintGeneDisplaySpec
       */

      // check that theDiscreteGeneticDataType is not Mutation
      if (theDiscreteGeneticDataType.equals(GeneticDataTypes.Mutation)) {
         throw new IllegalArgumentException("isDiscreteTypeAltered cannot handle Mutations");
      }

      // check that theDiscreteGeneticDataType is a discrete datatype
      if (!theDiscreteGeneticDataType.getTheDataTypeCategory().equals(DataTypeCategory.Discrete)) {
         throw new IllegalArgumentException("theDiscreteGeneticDataType is not a discrete datatype");
      }

      // TODO: make sure getValue() returns the right kind of int code
      String measurement = getValue(convertGeneticType(theDiscreteGeneticDataType));
      if (null == measurement) {
         return false;
      }

      GeneticTypeLevel theMeasuredGeneticTypeLevel;
      try {
         theMeasuredGeneticTypeLevel = GeneticTypeLevel.convertCode(theDiscreteGeneticDataType,
                  Integer.parseInt(measurement));
      } catch (NumberFormatException e) {
         return false;
      } catch (IllegalArgumentException e) {
         return false;
      }
      return this.theOncoPrintGeneDisplaySpec.satisfy(theDiscreteGeneticDataType, theMeasuredGeneticTypeLevel);
   }

   /**
    * 
    * @return the CNA level of this ValueParser, or null if none.
    */
   public GeneticTypeLevel getCNAlevel() {
      for (GeneticTypeLevel theGeneticTypeLevel : GeneticTypeLevel.values()) {
         if (theGeneticTypeLevel.getTheGeneticDataType().equals(GeneticDataTypes.CopyNumberAlteration)) {
            if (isDiscreteTypeThisLevel(GeneticDataTypes.CopyNumberAlteration, theGeneticTypeLevel)) {
               return theGeneticTypeLevel;
            }
         }
      }
      return null;
   }

   // I'd prefer to just export getCNAlevel(), etc., but I guess
   // these are helpful for some callers
   public boolean isCnaAmplified() {
      return isDiscreteTypeThisLevel(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.Amplified);
   }

   public boolean isCnaGained() {
      return isDiscreteTypeThisLevel(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.Gained);
   }

   public boolean isCnaDiploid() {
      return isDiscreteTypeThisLevel(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.Diploid);
   }

   public boolean isCnaHemizygouslyDeleted() {
      return isDiscreteTypeThisLevel(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.HemizygouslyDeleted);
   }

   public boolean isCnaHomozygouslyDeleted() {
      return isDiscreteTypeThisLevel(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.HomozygouslyDeleted);
   }

   // general case for continuous types
   private boolean doesContinuousValueExceedThreshold(GeneticDataTypes theContinuousGeneticDataType,
            Direction theDirection) {
      /*
       * if the measurement is available and it passes through the filter (which
       * checks the threshold) then yes else no
       */

      // check that theContinuousGeneticDataType is a continuous datatype
      if (!theContinuousGeneticDataType.getTheDataTypeCategory().equals(DataTypeCategory.Continuous)) {
         throw new IllegalArgumentException("theContinuousGeneticDataType is not a continuous datatype");
      }

      String value = getValue(convertGeneticType(theContinuousGeneticDataType));
      // out.println( value );
      if (null == value) {
         // out.println( "no value" );
         return false;
      }
      float measuredValue;
      try {
         measuredValue = Float.parseFloat(value);
      } catch (NumberFormatException e) {
         // out.println( value + " not float" );
         return false;
      }
      // out.println( measuredValue );

      return this.theOncoPrintGeneDisplaySpec.satisfy(theContinuousGeneticDataType, measuredValue, theDirection);
   }

   public boolean isMRNAWayUp() {
      return doesContinuousValueExceedThreshold(GeneticDataTypes.Expression, Direction.higher);
   }

   public boolean isMRNAWayDown() {
      return doesContinuousValueExceedThreshold(GeneticDataTypes.Expression, Direction.lower);
   }

   /**
    * a special case, because mutation values can take any of { NaN, [CnnnD], 1}
    * where the NaN indicates no mutation and the latter two indicates a
    * mutation. A [CnnnD] is the standard amino acid encoding of a mutation.
    * 
    * @return true if the gene was mutated and mutations were to be shown
    */
   public boolean isMutated() {
      // don't use isDiscreteTypeThisLevel because of mutation's complex values
      String mutationValue = datatypeToValueMap.get
              (GeneticAlterationType.MUTATION_EXTENDED.toString());
      if (mutationValue != null) {
         // TODO: fix: this is a little dangerous, because it means that ANY
         // value for mutation other than these will be reported as a mutation;
         // I would prefer a positive test
         if (mutationValue.equalsIgnoreCase(GeneticAlterationType.NAN)
                  || mutationValue.equals(GeneticAlterationType.ZERO)) {
            return false;
         } else {
            return this.theOncoPrintGeneDisplaySpec.satisfy(GeneticDataTypes.Mutation, GeneticTypeLevel.Mutated);
         }
      }
      return false;
   }

   /**
    * report on whether the gene was sequenced, as based on the mutation data,
    * irrespective of what the oncoPrint filter says.
    * 
    * @return
    */
   public boolean wasSequenced() {
      if (isMutated()) {
         return true;
      } else {
         String mutationValue = datatypeToValueMap.get
                 (GeneticAlterationType.MUTATION_EXTENDED.toString());
         if (mutationValue != null) {
            if (mutationValue.equalsIgnoreCase(GeneticAlterationType.NAN)) {
               return false;
            } else {
               return true;
            }
         } else {
            return false;
         }
      }
   }

   /**
    * returns true if the gene has any of the alterations defined in the
    * OncoPrintGeneDisplaySpec.
    * 
    * @return
    */
   public boolean isGeneAltered() {
      return isMutated() || this.isDiscreteTypeAltered( GeneticDataTypes.CopyNumberAlteration ) ||
      isMRNAWayUp() || isMRNAWayDown();
   }

   // TODO: combine the union of all genetic types into one, include all, like
   // GeneticAlterationType.METHYLATION_BINARY
   private static GeneticAlterationType convertGeneticType(GeneticDataTypes theDiscreteGeneticDataType) {
      switch (theDiscreteGeneticDataType) {
         case CopyNumberAlteration:
            return GeneticAlterationType.COPY_NUMBER_ALTERATION;
         case Expression:
            return GeneticAlterationType.MRNA_EXPRESSION;
         case Methylation:
            return GeneticAlterationType.METHYLATION;
         case Mutation:
            return GeneticAlterationType.MUTATION_EXTENDED;
      }
      // unreachable code; keep compiler happy
      // TODO: throw an exception
      return null;
   }

   private String getValue(GeneticAlterationType theGeneticAlterationType) {
      return datatypeToValueMap.get(theGeneticAlterationType.toString());
   }

   public String getCnaValue() {
      return datatypeToValueMap.get(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString());
   }

   public String getUnparsedMRNAValue() {
      return datatypeToValueMap.get(GeneticAlterationType.MRNA_EXPRESSION.toString());
   }

   public String getUnparsedMethylationValue() {
      return datatypeToValueMap.get(GeneticAlterationType.METHYLATION.toString());
   }

   public String getUnparsedProteinLevelValue() {
      return datatypeToValueMap.get(GeneticAlterationType.PROTEIN_LEVEL.toString());
   }

   public String getUnparsedPhosphorylationValue() {
      return datatypeToValueMap.get(GeneticAlterationType.PHOSPHORYLATION.toString());
   }

   @Override
   public String toString() {
      // TODO: also return string of this.datatypeToValueMap
      return this.originalValue + "\n" + this.theOncoPrintGeneDisplaySpec.toString();
   }

   /**
    * next 3 methods determine glyphs for the heatmap.
    * <p>
    * TODO: these belong in heatmap.java, when it gets made.
    * 
    * this provides the HTML for the location of the mutation glyph
    */
   public String getMutationGlyph() {
      if (this.isMutated()) {
         return "<img src='images/bullseye.png'>";
      } else {
         if (this.wasSequenced()) {
            return "";
         } else {
            return "<img src='images/na_status.gif'>";
         }
      }
   }

   /**
    * 
    * @param geneWithScore
    * @return
    */
   public String getMRNAGlyph() {
      if (this.isMRNAWayUp()) {
         return "<img src='images/up2.png'>";
      }
      if (this.isMRNAWayDown()) {
         return "<img src='images/down2.png'>";
      }
      return "";
   }

   public String getCopyNumberStyle() {
      GeneticTypeLevel theGeneticTypeLevel = this.getCNAlevel();
      if (null != theGeneticTypeLevel) {
         switch (theGeneticTypeLevel) {
            case Amplified:
               return "amp_2";
            case Gained:
               return "gained_2";
            case Diploid:
               return "no_change";
            case HemizygouslyDeleted:
               return "hetloss_2";
            case HomozygouslyDeleted:
               return "del_2";
         }
      }
      return "no_change";
   }
}