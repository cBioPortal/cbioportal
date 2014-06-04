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


package org.mskcc.cbio.portal.network;

import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author jgao
 */
public enum NodeType {
    PROTEIN("Protein","ProteinReference"),
    SMALL_MOLECULE("SmallMolecule", "SmallMoleculeReference"),
    COMPLEX_GROUP("ComplexGroup","ComplexGroup"),
    GENERIC_PROTEIN("GenericProtein","GenericProtein"),
    GENERIC_COMPLEX("GenericComplex","GenericComplex"),
    GENERIC_SMALL_MOLECULE("GenericSmallMolecule","GenericSmallMolecule"),
    GENERIC("Generic","Generic"),
    DRUG("Drug", "Drug"),
    UNKNOWN("Unknown","Unknown");
    
    private final String desc;
    private final String cpath2Keyword;
    
    private NodeType(String desc, String cpath2Keyword) {
        this.desc = desc;
        this.cpath2Keyword = cpath2Keyword;
    }
    
    private static final Map<String,NodeType> mapCpath2NodeType;
    static {
        mapCpath2NodeType = new HashMap<String,NodeType>();
        for (NodeType type : NodeType.values()) {
            mapCpath2NodeType.put(type.cpath2Keyword, type);
        }
    }
    
    public static NodeType getByCpath2Keyword(final String cpath2Keyword) {
        NodeType nodeType = mapCpath2NodeType.get(cpath2Keyword);
        if (nodeType!=null) {
            return nodeType;
        }
        
        return UNKNOWN;
    }
    
    @Override
    public String toString() {
        return desc;
    }
}
