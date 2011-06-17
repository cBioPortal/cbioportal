package org.mskcc.portal.oncoPrintSpecLanguage;

import java.util.ArrayList;

public class OncoPrintSpec {
    
    // set of these, for each data type, subject to multiple constraints
    // no constraints for a dataType means show all values
    // at most 2 constraints for any dataType, which must define 1 or 2 non-empty intervals
    ArrayList<ContinuousDataTypeSpec> theContinuousDataTypeSpec;
    ArrayList<DiscreteDataTypeSpec> theDiscreteDataTypeSpec;
    
    public OncoPrintSpec() {
        theContinuousDataTypeSpec = new ArrayList<ContinuousDataTypeSpec>();
        theDiscreteDataTypeSpec = new ArrayList<DiscreteDataTypeSpec>();
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        // TODO: order by DataType
        for( ContinuousDataTypeSpec aContinuousDataTypeSpec : theContinuousDataTypeSpec ){
            sb.append( aContinuousDataTypeSpec.toString() );
        }
        for( DiscreteDataTypeSpec aDiscreteDataTypeSpec : theDiscreteDataTypeSpec ){
            sb.append( aDiscreteDataTypeSpec.toString() );
        }
        return sb.toString();
    }

}
