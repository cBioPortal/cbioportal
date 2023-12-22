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

package org.cbioportal.model;

import java.io.Serializable;
import java.net.Inet4Address;
import jakarta.validation.constraints.NotNull;

/**
 * Class to wrap Reference Genome Gene.
 * @author Kelsey Zhu
 */
public class ReferenceGenomeGene implements Serializable {
    @NotNull
    private Integer referenceGenomeId;
    @NotNull
    private Integer entrezGeneId;
    private String hugoGeneSymbol;
    private String chromosome;
    private String cytoband;
    private Long start;
    private Long end;
    
    public void setReferenceGenomeId(Integer referenceGenomeId) { this.referenceGenomeId = referenceGenomeId; }

    public Integer getReferenceGenomeId() {
        return referenceGenomeId;
    }

    public Integer getEntrezGeneId() { return entrezGeneId; }

    public void setEntrezGeneId(Integer entrezGeneId) { this.entrezGeneId = entrezGeneId; }

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }
    
    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }
    
    public String getCytoband() {
        return cytoband;
    }

    public void setCytoband(String cytoband) {
        this.cytoband = cytoband;
    }
    
    public Long getStart() { return this.start; }

    public void setStart(Long start) { this.start = start; }

    public Long getEnd() { return this.end; }

    public void setEnd(Long end) { this.end = end; }

}
