
package org.mskcc.portal.model;

/**
 *
 * @author jj
 */
public class ProteinArrayInfo {
    private String gene;
    private String arrayId;
    private String arrayType;
    private String residue;
    private String source;
    private boolean validated;

    public ProteinArrayInfo(String gene, String arrayId, String arrayType, String residue, String source, boolean validated) {
        this.gene = gene;
        this.arrayId = arrayId;
        this.arrayType = arrayType;
        this.residue = residue;
        this.source = source;
        this.validated = validated;
    }

    public String getArrayId() {
        return arrayId;
    }

    public void setArrayId(String arrayId) {
        this.arrayId = arrayId;
    }

    public String getArrayType() {
        return arrayType;
    }

    public void setArrayType(String arrayType) {
        this.arrayType = arrayType;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }
    
}
