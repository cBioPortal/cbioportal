package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import org.mskcc.portal.oncoPrintSpecLanguage.DataTypeSpecEnumerations.DataTypeCategory;

/**
 * DataTypeSpec is an abstract class at the root of a set of classes that 
 * record and provide access to data type specifications.
 * 
 * @author Arthur Goldberg
 */
public abstract class DataTypeSpec {
    GeneticDataTypes theGeneticDataType;

    public GeneticDataTypes getTheGeneticDataType() {
        return theGeneticDataType;
    }
    
    /**
     * if a string name identifies a the unique genetic data type return the type, else 
     * throw IllegalArgumentException.
     * @param name
     * @param subType if non null, the unique genetic data type must have this DataTypeCategory
     * @return
     * @throws IllegalArgumentException
     */
    public static GeneticDataTypes genericFindDataType( String name, DataTypeCategory subType )
    throws IllegalArgumentException{
        
        GeneticDataTypes gdt = (GeneticDataTypes) UniqueEnumPrefix.findUniqueEnumMatchingPrefix( GeneticDataTypes.class, name );
        if( gdt == null ) {
            gdt = (GeneticDataTypes)UniqueEnumPrefix.findUniqueEnumWithNicknameMatchingPrefix( GeneticDataTypes.class, name );
        }
        if( gdt == null ) {
           throw new IllegalArgumentException( "Invalid DataType: " + name );           
        }
        if( null == subType ){
            return gdt;
        }else{
            if( gdt.getTheDataTypeCategory() == subType ){
                return gdt;
            }else{
               throw new IllegalArgumentException( "Invalid DataType: " + name );
            }
        }
    }
    
    public static GeneticDataTypes genericFindDataType( String name )
    throws IllegalArgumentException{
        
        return genericFindDataType( name, null );
    }
    
}