
package org.mskcc.portal.network;

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
