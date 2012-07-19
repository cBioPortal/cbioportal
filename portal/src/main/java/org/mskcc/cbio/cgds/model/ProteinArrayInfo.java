
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
    private String source;
    private String gene;
    private String residue;
    private boolean validated;
    private Set<Integer> cancerStudies; 

    public ProteinArrayInfo(String id, String type, String source, String gene, 
            String residue, boolean validated, Set<Integer> cancerStudies) {
        this.id = id;
        this.type = type;
        this.source = source;
        this.gene = gene;
        this.residue = residue;
        this.validated = validated;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
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
