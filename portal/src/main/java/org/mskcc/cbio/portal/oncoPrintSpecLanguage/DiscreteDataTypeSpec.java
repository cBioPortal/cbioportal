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

package org.mskcc.cbio.portal.oncoPrintSpecLanguage;
import static java.lang.System.out;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.DataTypeSpecEnumerations.DataTypeCategory;

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

        this.threshold = threshold;
        // verify that threshold is a level within theGeneticDataType
        if( theGeneticDataType == GeneticDataTypes.CopyNumberAlteration
                && !theGeneticDataType.equals( ((GeneticTypeLevel) threshold).getTheGeneticDataType() )) {
            // assumes that levels are organized in increasing order
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
          Object threshold;
          if (theGeneticDataType == GeneticDataTypes.CopyNumberAlteration) {
              threshold = GeneticTypeLevel.findDataTypeLevel(levelString);
          } else {
              threshold = levelString;
          }
          return new DiscreteDataTypeSpec( theGeneticDataType, theComparisonOp, threshold );
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
    public boolean satisfy( Object value) {
        if (value instanceof GeneticTypeLevel) {
            if (!(threshold instanceof GeneticTypeLevel)) {
                return false;
            }
            GeneticTypeLevel gtl = (GeneticTypeLevel)value;
            if( !gtl.getTheGeneticDataType().equals(theGeneticDataType) ){
                return false;
            }
            GeneticTypeLevel theCNAthreshold = (GeneticTypeLevel)this.threshold;
            switch (this.comparisonOp) {
            case GreaterEqual:
                return( 0 <= gtl.compareTo(theCNAthreshold) );
            case Greater:
                return( 0 < gtl.compareTo(theCNAthreshold) );
            case LessEqual:
                return( gtl.compareTo(theCNAthreshold) <= 0 );
            case Less:
                return( gtl.compareTo(theCNAthreshold) < 0 );
            case Equal:
                return ( gtl.compareTo(theCNAthreshold) == 0 );
            } 
        }
        else if (value instanceof String) {
            if (!(threshold instanceof String)) {
                return false;
            }
            return comparisonOp == ComparisonOp.Equal && value.equals(threshold.toString());
        }
        // keep compiler happy
        (new UnreachableCodeException( "")).printStackTrace();
        //System.exit(1);
        return false; 
   }
    
    /**
     * convert this DiscreteDataTypeSpec into a DiscreteDataTypeSetSpec
     * @return a DiscreteDataTypeSetSpec that satisfies the same levels as the DiscreteDataTypeSpec 
     */
    // TODO: test
    public DiscreteDataTypeSetSpec convertToDiscreteDataTypeSetSpec( ){
        if (threshold instanceof GeneticTypeLevel) {
            DiscreteDataTypeSetSpec aDiscreteDataTypeSetSpec = new DiscreteDataTypeSetSpec( this.theGeneticDataType);
            for( GeneticTypeLevel aGeneticTypeLevel : GeneticTypeLevel.values()){
                if( aGeneticTypeLevel.getTheGeneticDataType().getTheDataTypeCategory().equals(DataTypeCategory.Discrete) &&
                        this.satisfy(aGeneticTypeLevel) ){
                    aDiscreteDataTypeSetSpec.addLevel(aGeneticTypeLevel);
                }
            }
            return aDiscreteDataTypeSetSpec;
        } else if (theGeneticDataType == GeneticDataTypes.Mutation && threshold instanceof String) {
            return DiscreteDataTypeSetSpec.specificMutationDataTypeSetSpecGenerator((String)threshold);
        }
        
        return null;
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