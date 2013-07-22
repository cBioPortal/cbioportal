
package org.mskcc.cbio.cgds.model;

/**
 *
 * @author jgao
 */
public class CosmicMutationFrequency {
    private int id; 
    private long entrezGeneId;
    private String aminoAcidChange;
    private int frequency;

    public CosmicMutationFrequency(int id, long entrezGeneId, String aminoAcidChange, int frequency) {
        this.id = id;
        this.entrezGeneId = entrezGeneId;
        this.aminoAcidChange = aminoAcidChange;
        this.frequency = frequency;
    }

    public CosmicMutationFrequency(long entrezGeneId, String aminoAcidChange, int frequency) {
        this.entrezGeneId = entrezGeneId;
        this.aminoAcidChange = aminoAcidChange;
        this.frequency = frequency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(long entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getAminoAcidChange() {
        return aminoAcidChange;
    }

    public void setAminoAcidChange(String aminoAcidChange) {
        this.aminoAcidChange = aminoAcidChange;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
