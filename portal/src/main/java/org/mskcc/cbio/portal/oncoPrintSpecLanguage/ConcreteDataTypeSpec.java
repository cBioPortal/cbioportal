package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import org.mskcc.portal.util.EqualsUtil;
import org.mskcc.portal.util.HashCodeUtil;

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
