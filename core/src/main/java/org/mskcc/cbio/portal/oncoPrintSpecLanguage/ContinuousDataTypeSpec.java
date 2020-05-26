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

package org.mskcc.cbio.portal.oncoPrintSpecLanguage;
import static java.lang.System.out;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.DataTypeSpecEnumerations.DataTypeCategory;
import org.mskcc.cbio.portal.util.EqualsUtil;

/**
 * generically record and access the Continuous DataTypeSpecs, Expression and
 * Methylation. 
 * 
 * @author Arthur Goldberg
 */
public class ContinuousDataTypeSpec extends DataTypeSpecInequality{

   public ContinuousDataTypeSpec(GeneticDataTypes theGeneticDataType,
         ComparisonOp comparisonOp, float threshold ) throws IllegalArgumentException{
        if( !theGeneticDataType.getTheDataTypeCategory().equals(DataTypeCategory.Continuous)){
            throw new IllegalArgumentException("ContinuousDataTypeSpec only takes Continuous GeneticDataTypes");
        }
        this.theGeneticDataType = theGeneticDataType;
        this.comparisonOp = comparisonOp;
        this.threshold = Float.valueOf(threshold); 
    }
 
   /**
    * generate a ContinuousDataTypeSpec from String only arguments; ease implementation of the grammar
    * motto: do as little as possible in the grammar!
    * 
    * @param theGeneticDataTypeString
    * @param theComparisonOpString
    * @param theThresholdString
    * @return
    */
   public static ContinuousDataTypeSpec continuousDataTypeSpecGenerator(String theGeneticDataTypeString, 
         String theComparisonOpString, String theThresholdString ){
      try {
         //out.println( "continuousDataTypeSpecGenerator" + ":" + theGeneticDataTypeString + ":" + theComparisonOpString + ":" + theThresholdString );
         GeneticDataTypes theGeneticDataType = ContinuousDataTypeSpec.findDataType( theGeneticDataTypeString );
         ComparisonOp theComparisonOp = ComparisonOp.convertCode( theComparisonOpString );
         if( null == theThresholdString ){
            return null;
         }
         float theThreshold = Float.parseFloat( theThresholdString );
         return new ContinuousDataTypeSpec(    theGeneticDataType, theComparisonOp, theThreshold );
      } catch (IllegalArgumentException e) {
         return null;
      }
   }
 
    /**
     * Copy constructor.
     */
     public ContinuousDataTypeSpec(ContinuousDataTypeSpec aContinuousDataTypeSpec) {
       this( aContinuousDataTypeSpec.getTheGeneticDataType(), aContinuousDataTypeSpec.getComparisonOp(), 
                ((Float)aContinuousDataTypeSpec.getThreshold()).floatValue() );
       //no defensive copies are created here, since 
       //there are no mutable object fields
     }
     
     public static ContinuousDataTypeSpec newInstance( ContinuousDataTypeSpec aContinuousDataTypeSpec ){
        if( null == aContinuousDataTypeSpec ){
           return null;
        }
        return new ContinuousDataTypeSpec( aContinuousDataTypeSpec );
     }

     public static GeneticDataTypes findDataType( String name )
    throws IllegalArgumentException{
        return DataTypeSpecInequality.genericFindDataType( name, DataTypeCategory.Continuous );
    }
        
    /**
     * indicate whether value satisfies this ContinuousDataTypeSpec
     * 
     * @param value
     * @return true if value satisfies this ContinuousDataTypeSpec
     */
    public boolean satisfy( double value){
        Float f = (Float)this.threshold;
        float t = f.floatValue();
        switch (this.comparisonOp) {
        case GreaterEqual:
            return( t <= value );
        case Greater:
            return( t < value );
        case LessEqual:
            return( value <= t );
        case Less:
            return( value < t  );
        }
        // keep compiler happy
        (new UnreachableCodeException( "")).printStackTrace();
        return false; 
    }
    
    /**
     * combine aContinuousDataTypeSpec into this ContinuousDataTypeSpec, so that this ContinuousDataTypeSpec
     * accepts any value that would have been accepted by either ContinuousDataTypeSpec.
     * Both ContinuousDataTypeSpecs must have ComparisonOps with the same direction (> or <). 
     * Used to simplify a specification.
     * 
     * @param aContinuousDataTypeSpec
     * @throws IllegalArgumentException when the ContinuousDataTypeSpecs have ComparisonOps with different directions.
     */
    public void combine(ContinuousDataTypeSpec aContinuousDataTypeSpec) throws IllegalArgumentException{
        if( !this.comparisonOp.getTheComparisonOpDirection().equals( aContinuousDataTypeSpec.getComparisonOp().getTheComparisonOpDirection() ) ){
            throw new IllegalArgumentException( "the ContinuousDataTypeSpecs must have ComparisonOps with the same direction" );
        }
        switch( this.comparisonOp.getTheComparisonOpDirection() ){
        case Smaller:
            if( ((Float)aContinuousDataTypeSpec.getThreshold()).floatValue() > ((Float)this.threshold).floatValue()){
                this.threshold = aContinuousDataTypeSpec.getThreshold();
                this.comparisonOp = aContinuousDataTypeSpec.getComparisonOp();
            } else if( ((Float)aContinuousDataTypeSpec.getThreshold()).floatValue() == ((Float)this.threshold).floatValue()){
                if(this.comparisonOp.equals(ComparisonOp.LessEqual) || aContinuousDataTypeSpec.getComparisonOp().equals(ComparisonOp.LessEqual) ){
                    this.comparisonOp = ComparisonOp.LessEqual;
                }
            }
            break;

        case Bigger:
            if( ((Float)aContinuousDataTypeSpec.getThreshold()).floatValue() < ((Float)this.threshold).floatValue()){
                this.threshold = aContinuousDataTypeSpec.getThreshold();
                this.comparisonOp = aContinuousDataTypeSpec.getComparisonOp();
            } else if( ((Float)aContinuousDataTypeSpec.getThreshold()).floatValue() == ((Float)this.threshold).floatValue()){
                if(this.comparisonOp.equals(ComparisonOp.GreaterEqual) || aContinuousDataTypeSpec.getComparisonOp().equals(ComparisonOp.GreaterEqual) ){
                    this.comparisonOp = ComparisonOp.GreaterEqual;
                }
            }
            break;
            
        }
        
    }
    
    @Override
    public boolean equals( Object aThat ) {
       if ( this == aThat ) return true;
       if ( !(aThat instanceof ContinuousDataTypeSpec) ) return false;
       return
          super.equals(aThat);
    }

    @Override public final Object clone() throws CloneNotSupportedException {
       throw new CloneNotSupportedException();
     }   
    
}