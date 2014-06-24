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

public class ConcreteDataTypeSpec extends DataTypeSpec {

    public ConcreteDataTypeSpec( GeneticDataTypes theGeneticDataType) {
        this.theGeneticDataType = theGeneticDataType;
    }

    public ConcreteDataTypeSpec( String name ) {
        this.theGeneticDataType = findDataType( name );
    }
    
    public static ConcreteDataTypeSpec concreteDataTypeSpecGenerator( String name ){
        try {
         return new ConcreteDataTypeSpec( DataTypeSpec.genericFindDataType( name ) );
      } catch (IllegalArgumentException e) {
         return null;
      }
    }
    
    public static GeneticDataTypes findDataType( String name )
    throws IllegalArgumentException{
        return DataTypeSpec.genericFindDataType( name );
    }
    
    public boolean satisfy( GeneticDataTypes value) {
        return this.theGeneticDataType.equals(value);
    }

    @Override
    public String toString() {
        return theGeneticDataType.toString();
    }

    @Override
    public boolean equals( Object otherConcreteDataTypeSpec) {
        if( this == otherConcreteDataTypeSpec ) return true;
        if ( !(otherConcreteDataTypeSpec instanceof ConcreteDataTypeSpec) ) return false;
        ConcreteDataTypeSpec that = (ConcreteDataTypeSpec) otherConcreteDataTypeSpec;
        return
            EqualsUtil.areEqual(this.theGeneticDataType, that.theGeneticDataType);
    }

    // TODO: TEST
    @Override
    public int hashCode( ) {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash( result, theGeneticDataType );
        return result;
    }
}
