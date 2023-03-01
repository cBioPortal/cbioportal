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

import org.mskcc.cbio.portal.oncoPrintSpecLanguage.DataTypeSpecEnumerations.DataTypeCategory;

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