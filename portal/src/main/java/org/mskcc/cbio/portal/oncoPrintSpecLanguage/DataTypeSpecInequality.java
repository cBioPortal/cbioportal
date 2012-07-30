package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import org.mskcc.cbio.portal.util.EqualsUtil;
import org.mskcc.cbio.portal.util.HashCodeUtil;

/**
 * Generically record and access a DataTypeSpec inequality, that is a spec that says
 * a value must be greater or less (or >= or <=) a threshold.
 * Subclassed by concrete classes DiscreteDataTypeSpec and ContinuousDataTypeSpec.
 * 
 * @author Arthur Goldberg
 */
public abstract class DataTypeSpecInequality extends DataTypeSpec{

    /**
     * record the range over which a value satisfies the specification.
     * @author Arthur Goldberg
     */
    ComparisonOp comparisonOp;
    Object threshold; // A level within theGeneticDataType; 

    public ComparisonOp getComparisonOp() {
        return comparisonOp;
    }

    public Object getThreshold() {
        return threshold;
    }

    @Override
    public String toString() {
        return theGeneticDataType.toString() + comparisonOp.getToken() + 
            threshold.toString();
    }

    @Override
    public boolean equals( Object otherDataTypeSpec) {
        if( this == otherDataTypeSpec ) return true;
        if ( !(otherDataTypeSpec instanceof DataTypeSpecInequality) ) return false;
        DataTypeSpecInequality that = (DataTypeSpecInequality) otherDataTypeSpec;
        return
            EqualsUtil.areEqual(this.theGeneticDataType, that.theGeneticDataType) &&
            EqualsUtil.areEqual(this.threshold, that.threshold) &&
            EqualsUtil.areEqual(this.comparisonOp, that.comparisonOp);
    }

    // TODO: TEST
    @Override
    public int hashCode( ) {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash( result, theGeneticDataType );
        result = HashCodeUtil.hash( result, threshold );
        result = HashCodeUtil.hash( result, comparisonOp );
        return result;
    }
}