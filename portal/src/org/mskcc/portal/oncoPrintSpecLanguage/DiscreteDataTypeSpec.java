package org.mskcc.portal.oncoPrintSpecLanguage;
import static java.lang.System.out;
import org.mskcc.portal.oncoPrintSpecLanguage.DataTypeSpecEnumerations.DataTypeCategory;

/**
 * Generically record and access an inequality of one of the Discrete DataTypeSpecs, CNA and Mutation.
 * 
 * @author Arthur Goldberg
 */
// TODO: since I need DiscreteDataTypeSetSpec, could reimplement this class with that
public class DiscreteDataTypeSpec extends DataTypeSpecInequality{

    // TODO: constructor that takes text and use it in grammar to simplify that code
    public DiscreteDataTypeSpec(
            GeneticDataTypes theGeneticDataType,
            ComparisonOp comparisonOp, Object threshold) throws IllegalArgumentException{
        this.theGeneticDataType = theGeneticDataType;
        this.comparisonOp = comparisonOp;

        // verify that threshold is a level within theGeneticDataType
        if( theGeneticDataType.equals( ((GeneticTypeLevel) threshold).getTheGeneticDataType() )) {
            // assumes that levels are organized in increasing order
            this.threshold = threshold;
        }else{
            throw new IllegalArgumentException( "threshold is not a level within theGeneticDataType" );
        }
    }
    
    /**
     * generate a DiscreteDataTypeSpec from the strings found by discreteDataType in the completeOncoPrintSpecAST
     * @param theGeneticDataTypeString
     * @param comparisonOpString
     * @param levelString
     * @return the DiscreteDataTypeSpec, or null if any inputs were incorrect
     */
    public static DiscreteDataTypeSpec discreteDataTypeSpecGenerator( String theGeneticDataTypeString,
            String comparisonOpString, String levelString ){
       try {
         GeneticDataTypes theGeneticDataType = DiscreteDataTypeSpec.findDataType( theGeneticDataTypeString );
          ComparisonOp theComparisonOp = ComparisonOp.convertCode( comparisonOpString );
          GeneticTypeLevel theGeneticTypeLevel = GeneticTypeLevel.findDataTypeLevel(levelString);
          return new DiscreteDataTypeSpec( theGeneticDataType, theComparisonOp, theGeneticTypeLevel );
      } catch (IllegalArgumentException e) {
         //out.println( e.getMessage() );
         return null;
      }
    }

    public static GeneticDataTypes findDataType( String name )
    throws IllegalArgumentException{
        return DataTypeSpecInequality.genericFindDataType( name, DataTypeCategory.Discrete );
    }
        
    /**
     * indicate whether value satisfies this DataTypeSpec
     * 
     * @param value
     * @return true if value satisfies this DataTypeSpec
     */
    public boolean satisfy( GeneticTypeLevel value) {
        if( !value.getTheGeneticDataType().equals(theGeneticDataType) ){
            return false;
        }

        GeneticTypeLevel theCNAthreshold = (GeneticTypeLevel)this.threshold;
        switch (this.comparisonOp) {
        case GreaterEqual:
            return( 0 <= value.compareTo(theCNAthreshold) );
        case Greater:
            return( 0 < value.compareTo(theCNAthreshold) );
        case LessEqual:
            return( value.compareTo(theCNAthreshold) <= 0 );
        case Less:
            return( value.compareTo(theCNAthreshold) < 0 );
        case Equal:
            return ( value.compareTo(theCNAthreshold) == 0 );
        }
        // keep compiler happy
        (new UnreachableCodeException( "")).printStackTrace();
        System.exit(1);
        return false; 
   }
    
    /**
     * convert this DiscreteDataTypeSpec into a DiscreteDataTypeSetSpec
     * @return a DiscreteDataTypeSetSpec that satisfies the same levels as the DiscreteDataTypeSpec 
     */
    // TODO: test
    public DiscreteDataTypeSetSpec convertToDiscreteDataTypeSetSpec( ){
        DiscreteDataTypeSetSpec aDiscreteDataTypeSetSpec = new DiscreteDataTypeSetSpec( this.theGeneticDataType);
        for( GeneticTypeLevel aGeneticTypeLevel : GeneticTypeLevel.values()){
            if( aGeneticTypeLevel.getTheGeneticDataType().getTheDataTypeCategory().equals(DataTypeCategory.Discrete) &&
                    this.satisfy(aGeneticTypeLevel) ){
                aDiscreteDataTypeSetSpec.addLevel(aGeneticTypeLevel);
            }
        }
        return aDiscreteDataTypeSetSpec;
    }
    
    // TODO: unit test
    @Override
    public boolean equals( Object aThat ) {
       if ( this == aThat ) return true;
       if ( !(aThat instanceof DiscreteDataTypeSpec) ) return false;
       return
          super.equals(aThat);
    }
           
}