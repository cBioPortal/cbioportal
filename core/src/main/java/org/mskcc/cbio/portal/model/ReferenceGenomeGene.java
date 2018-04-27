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
 * Class to wrap Reference Genome Gene.
 * @author Kelsey Zhu
 */
public class ReferenceGenomeGene {
    private int referenceGenomeId;
    private long entrezGeneId;
    private String chr;
    private String cytoband;
    private int exonicLength;
    private long start;
    private long end;
    
    /**
     *
     * @param entrezGeneId ENTREZ_GENE_ID
     * @param referenceGenomeId REFERENCE_GENOME_ID
     */
    public ReferenceGenomeGene(long entrezGeneId, int referenceGenomeId) {
        this.entrezGeneId = entrezGeneId;
        this.referenceGenomeId = referenceGenomeId;
    }
    
    /**
     *
     * @param referenceGenomeId REFERENCE_GENOME_ID  
     * @param chr Chromosome name
     * @param cytoband  CYTOBAND of the gene
     * @param exonicLength  EXONIC LENGTH of the gene
     * @param start start point of the gene 
     * @param end end point of the gene             
     */
    public ReferenceGenomeGene(long entrezGeneId, int referenceGenomeId, String chr, 
                               String cytoband, int exonicLength, long start, long end) {

        this.referenceGenomeId = referenceGenomeId;
        this.entrezGeneId = entrezGeneId;
        this.chr = chr;
        this.cytoband = cytoband;
        this.exonicLength = exonicLength;
        this.start = start;
        this.end = end;
    }
    

    public void setReferenceGenomeId(int referenceGenomeId) { this.referenceGenomeId = referenceGenomeId; }
    
    public int getReferenceGenomeId() {
        return referenceGenomeId;
    }
    
    public void setEntrezGeneId(long entrezGeneId) { this.entrezGeneId = entrezGeneId; }
    
    public long getEntrezGeneId() { return entrezGeneId; }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }
    public String getCytoband() {
        return cytoband;
    }

    public void setCytoband(String cytoband) {
        this.cytoband = cytoband;
    }

    public int getExonicLength() {
        return exonicLength;
    }

    public void setExonicLength(int exonicLength) {
        this.exonicLength = exonicLength;
    }

    public long getStart() { return this.start; }

    public void setStart(long start) { this.start = start; }

    public long getEnd() { return this.end = end; }

    public void setEnd(long end) { this.end = end; }

    @Override
    public boolean equals(Object obj0) {
        if (!(obj0 instanceof ReferenceGenomeGene)) {
            return false;
        }

        ReferenceGenomeGene gene0 = (ReferenceGenomeGene) obj0;
        if (gene0.entrezGeneId == entrezGeneId && gene0.referenceGenomeId == referenceGenomeId) {
            return true;
        }
        return false;
    }


    @Override
    public int hashCode() {
        int result = 2;
        result = 31 * result + (int)this.entrezGeneId;
        result = 31 * result + this.referenceGenomeId;
        return result;
    }

}