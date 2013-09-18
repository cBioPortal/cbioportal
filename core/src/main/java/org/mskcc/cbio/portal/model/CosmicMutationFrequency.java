
package org.mskcc.cbio.portal.model;

/**
 *
 * @author jgao
 */
public class CosmicMutationFrequency {
    private String id;
    private String chr;
    private long startPosition;
    private String referenceAllele;
    private String tumorSeqAllele;
    private String strand;
    private String cds;
    private long entrezGeneId;
    private String aminoAcidChange;
    private String keyword;
    private int frequency;

    public CosmicMutationFrequency(String id, long entrezGeneId, String aminoAcidChange,
            String keyword, int frequency) {
        this.id = id;
        this.entrezGeneId = entrezGeneId;
        this.aminoAcidChange = aminoAcidChange;
        this.keyword = keyword;
        this.frequency = frequency;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public String getReferenceAllele() {
        return referenceAllele;
    }

    public void setReferenceAllele(String referenceAllele) {
        this.referenceAllele = referenceAllele;
    }

    public String getTumorSeqAllele() {
        return tumorSeqAllele;
    }

    public void setTumorSeqAllele(String tumorSeqAllele) {
        this.tumorSeqAllele = tumorSeqAllele;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getCds() {
        return cds;
    }

    public void setCds(String cds) {
        this.cds = cds;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CosmicMutationFrequency other = (CosmicMutationFrequency) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
