/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import org.mskcc.cbio.portal.util.EqualsUtil;

/**
 * holds a data type spec; an OncoPrintGeneDisplaySpec contains one of these for each data type.
 * 
 * @author Arthur Goldberg
 */
public class ResultDataTypeSpec {
    GeneticDataTypes theGeneticDataType;
    boolean acceptAll;
    DiscreteDataTypeSetSpec theDiscreteDataTypeSetSpec;
    
    // means that all values that exceed the threshold in combinedGreaterContinuousDataTypeSpec indicate alteration
    ContinuousDataTypeSpec combinedGreaterContinuousDataTypeSpec;
    // same meaning, but lesser
    ContinuousDataTypeSpec combinedLesserContinuousDataTypeSpec;

    public ResultDataTypeSpec(GeneticDataTypes theGeneticDataType) {
       this.theGeneticDataType = theGeneticDataType;
   }

    /**
     * Alternative style for a copy constructor, using a static newInstance
     * method.
     */
     public static ResultDataTypeSpec newInstance( ResultDataTypeSpec aResultDataTypeSpec ) {
        if( null == aResultDataTypeSpec ){
           return null;
        }
        ResultDataTypeSpec theNewResultDataTypeSpec = new ResultDataTypeSpec( aResultDataTypeSpec.theGeneticDataType );
        theNewResultDataTypeSpec.setAcceptAll( aResultDataTypeSpec.isAcceptAll() );
        theNewResultDataTypeSpec.setTheDiscreteDataTypeSetSpec( 
                 DiscreteDataTypeSetSpec.newInstance( aResultDataTypeSpec.getTheDiscreteDataTypeSetSpec() ) );
        
        theNewResultDataTypeSpec.setCombinedGreaterContinuousDataTypeSpec( 
                 ContinuousDataTypeSpec.newInstance( aResultDataTypeSpec.getCombinedGreaterContinuousDataTypeSpec() ) );

        theNewResultDataTypeSpec.setCombinedLesserContinuousDataTypeSpec(  
                 ContinuousDataTypeSpec.newInstance( aResultDataTypeSpec.getCombinedLesserContinuousDataTypeSpec() ) );

       return theNewResultDataTypeSpec;
     }
    
    public DiscreteDataTypeSetSpec getTheDiscreteDataTypeSetSpec() {
        return theDiscreteDataTypeSetSpec;
    }

    public void setAcceptAll(boolean acceptAll) {
        this.acceptAll = acceptAll;
    }

    public boolean isAcceptAll() {
      return acceptAll;
   }

   public void setCombinedGreaterContinuousDataTypeSpec(
            ContinuousDataTypeSpec combinedGreaterContinuousDataTypeSpec) {
        this.combinedGreaterContinuousDataTypeSpec = combinedGreaterContinuousDataTypeSpec;
    }

    public void setCombinedLesserContinuousDataTypeSpec(
            ContinuousDataTypeSpec combinedLesserContinuousDataTypeSpec) {
        this.combinedLesserContinuousDataTypeSpec = combinedLesserContinuousDataTypeSpec;
    }

    public void setTheDiscreteDataTypeSetSpec(
            DiscreteDataTypeSetSpec theDiscreteDataTypeSetSpec) {
        this.theDiscreteDataTypeSetSpec = theDiscreteDataTypeSetSpec;
    }

    public ContinuousDataTypeSpec getCombinedGreaterContinuousDataTypeSpec() {
      return combinedGreaterContinuousDataTypeSpec;
   }

   public ContinuousDataTypeSpec getCombinedLesserContinuousDataTypeSpec() {
      return combinedLesserContinuousDataTypeSpec;
   }

   @Override
    public String toString() {
        if( acceptAll ){
            return theGeneticDataType.toString();
        }

        switch( this.theGeneticDataType.getTheDataTypeCategory()){
        case Continuous:
            StringBuffer sb = new StringBuffer(); 
            if( combinedLesserContinuousDataTypeSpec != null ){
                sb.append(combinedLesserContinuousDataTypeSpec.toString()).append(" ");
            }
            if( combinedGreaterContinuousDataTypeSpec != null ){
                sb.append(combinedGreaterContinuousDataTypeSpec.toString()).append(" ");
            }
            return sb.append(" ").toString();

        case Discrete:
            return theDiscreteDataTypeSetSpec.toString()+" ";
        }
        // keep compiler happy
        (new UnreachableCodeException( "")).printStackTrace();
        return "compiler happy";
    }
   
   @Override public final Object clone() throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
    }   

   @Override
   public boolean equals( Object aThat ) {
       if( this == aThat ) return true;
       if ( !(aThat instanceof ResultDataTypeSpec ) ) return false;
       ResultDataTypeSpec that = (ResultDataTypeSpec) aThat;
       return
          EqualsUtil.areEqual( this.theGeneticDataType, that.theGeneticDataType) &&
          EqualsUtil.areEqual( this.acceptAll, that.acceptAll ) &&
          EqualsUtil.areEqual( this.theDiscreteDataTypeSetSpec, that.theDiscreteDataTypeSetSpec ) &&
          EqualsUtil.areEqual( this.combinedGreaterContinuousDataTypeSpec, that.combinedGreaterContinuousDataTypeSpec ) &&
          EqualsUtil.areEqual( this.combinedLesserContinuousDataTypeSpec, that.combinedLesserContinuousDataTypeSpec );
   }
   
}