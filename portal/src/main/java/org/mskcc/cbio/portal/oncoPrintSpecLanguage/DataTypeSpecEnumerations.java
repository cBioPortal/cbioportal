package org.mskcc.portal.oncoPrintSpecLanguage;

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
