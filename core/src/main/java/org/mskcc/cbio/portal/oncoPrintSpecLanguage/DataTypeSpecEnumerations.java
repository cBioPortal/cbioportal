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

import static java.lang.System.out;

import java.util.ArrayList;

public class DataTypeSpecEnumerations {
    static public boolean isDataTypeName(String id) {

        if (null == UniqueEnumPrefix.findUniqueEnumMatchingPrefix(
                GeneticDataTypes.class, id)
                && null == UniqueEnumPrefix.findUniqueEnumWithNicknameMatchingPrefix(
                                GeneticDataTypes.class, id)) {
            // out.format( "isDataTypeName: returning false for '%s'%n", id );
            return false;
        }
        // out.format( "isDataTypeName: returning true for '%s'%n", id );
        return true;
    }

    static public boolean isDataTypeLevel(String id) {
        if (null == UniqueEnumPrefix.findUniqueEnumMatchingPrefix(
                GeneticTypeLevel.class, id)
                && null == UniqueEnumPrefix
                        .findUniqueEnumWithNicknameMatchingPrefix(
                                GeneticTypeLevel.class, id)) {
            //out.format( "isDataTypeLevel: returning false for '%s'%n", id );
            ArrayList<String> levels = new ArrayList<String>();
            for( GeneticTypeLevel aGeneticTypeLevel: GeneticTypeLevel.values()){
                levels.add(aGeneticTypeLevel.toString());
                for( String s : aGeneticTypeLevel.getNicknames()){
                    levels.add( s );
                }
            }
            out.format( "'%s' is not a unique data level; valid levels are %s.%n", id, levels );
            return false;
        }
        // out.format( "isDataTypeLevel: returning true for '%s'%n", id );
        return true;
    }

    public enum DataTypeCategory {
       Continuous, Discrete
   };
}
