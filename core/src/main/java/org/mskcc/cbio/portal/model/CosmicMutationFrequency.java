/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    public CosmicMutationFrequency(
        String id,
        long entrezGeneId,
        String aminoAcidChange,
        String keyword,
        int frequency
    ) {
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
        if (
            (this.id == null) ? (other.id != null) : !this.id.equals(other.id)
        ) {
            return false;
        }
        return true;
    }
}
