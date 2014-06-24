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