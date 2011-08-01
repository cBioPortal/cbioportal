
package org.mskcc.cgds.model;

/**
 *
 * @author jj
 */
public class ProteinArrayTarget {
    private String arrayId;
    private long entrezGeneId;
    private String residue;

    public ProteinArrayTarget(String arrayId, long entrezGeneId) {
        this(arrayId, entrezGeneId, null);
    }

    public ProteinArrayTarget(String arrayId, long entrezGeneId, String residue) {
        this.arrayId = arrayId;
        this.entrezGeneId = entrezGeneId;
        this.residue = residue;
    }

    public String getArrayId() {
        return arrayId;
    }

    public void setArrayId(String arrayId) {
        this.arrayId = arrayId;
    }

    public long getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(long entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getResidue() {
        return residue;
    }

    public void setResidue(String residue) {
        this.residue = residue;
    }
    
    
}
