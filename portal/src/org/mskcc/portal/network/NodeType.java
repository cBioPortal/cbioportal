
package org.mskcc.portal.network;

/**
 *
 * @author jgao
 */
public enum NodeType {
    PROTEIN("Protein"), SMALL_MOLECULE("SmallMolecule"), UNKNOWN("Unknown");
    
    private final String desc;
    
    private NodeType(String desc) {
        this.desc = desc;
    }
    
    @Override
    public String toString() {
        return desc;
    }
}
