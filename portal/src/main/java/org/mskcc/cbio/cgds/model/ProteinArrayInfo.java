
package org.mskcc.cbio.cgds.model;

import java.util.Collections;
import java.util.Set;

/**
 * Class for protein array information
 * @author jj
 */
public class ProteinArrayInfo {
    private String id;
    private String type;
    private String gene;
    private String residue;
    private Set<Integer> cancerStudies; 

    public ProteinArrayInfo(String id, String type, String gene, 
            String residue, Set<Integer> cancerStudies) {
        this.id = id;
        this.type = type;
        this.gene = gene;
        this.residue = residue;
        this.cancerStudies = cancerStudies;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getResidue() {
        return residue;
    }

    public void setResidue(String residue) {
        this.residue = residue;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<Integer> getCancerStudies() {
        if (cancerStudies==null) {
            return Collections.emptySet();
        }
        return cancerStudies;
    }

    public void setCancerStudies(Set<Integer> cancerStudies) {
        this.cancerStudies = cancerStudies;
    }
}
