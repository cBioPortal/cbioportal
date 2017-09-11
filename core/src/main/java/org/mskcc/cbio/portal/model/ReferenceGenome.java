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

import java.util.Date;
import org.mskcc.cbio.portal.util.*;



/**
 * This represents a reference genome used by genetic profiling
 *
 * @author Kelsey Zhu
 */
public class ReferenceGenome {
    /**
     * NO_SUCH_STUDY Internal ID has not been assigned yet.
     */

    private int referenceGenomeId; // assigned by dbms auto increment
    private String genomeName;
    private String species;
    private String buildName;
    private long genomeSize;
    private String url;
    private Date releaseDate;


    /**
     * Constructor.
     * @param genomeName              Name of Reference Genome.
     * @param species           Species of reference genome.
     * @param buildName         Name of genome build 
     * @param genomeSize        Effective genome size
     * @param url               URL to download genome size
     * @param releaseDate       Date When Reference Genome Assembly Released            
     */
    public ReferenceGenome(String genomeName, String species, String buildName) {
        super();
        this.genomeName = genomeName;
        this.species = species;
        this.buildName = buildName;
    }

    public void setReferenceGenomeId(int referenceGenomeId) {
        this.referenceGenomeId = referenceGenomeId;
    }
    
    public int getReferenceGenomeId() {
        return referenceGenomeId;
    }
    
    public void setGenomeName(String genomeName) {
        this.genomeName = genomeName;
    }
    
    public String getGenomeName() {
        return this.genomeName;
    }
    
    public void setSpecies(String species) {
        this.species = species;
    }

    public String getSpecies() {
        return this.species;
    }
    
    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }
    
    public String getBuildName () {
        return this.buildName;
    }
    
    public void setGenomeSize(long genomeSize) {
        this.genomeSize = genomeSize;
    }
    
    public long getGenomeSize() {
        return this.genomeSize;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }
    
    public Date getReleaseDate() {
        return this.releaseDate;
    }
    
    /**
     * Equals.
     * @param otherReferenceGenome Other Reference Genome.
     * @return true of false.
     */
    @Override
    public boolean equals(Object otherReferenceGenome) {
        if (this == otherReferenceGenome) {
            return true;
        }

        if (!(otherReferenceGenome instanceof ReferenceGenome)) {
            return false;
        }

        ReferenceGenome that = (ReferenceGenome) otherReferenceGenome;
        return
            EqualsUtil.areEqual(this.genomeName, that.genomeName) &&
                EqualsUtil.areEqual(this.species,
                    that.species) &&
                EqualsUtil.areEqual(this.buildName, that.buildName) &&
                EqualsUtil.areEqual(this.genomeName, that.genomeName);
    }

    @Override
    public int hashCode() {
        int hash = 4;
        hash = 11 * hash + this.referenceGenomeId;
        hash = 11 * hash + (this.genomeName != null ? this.genomeName.hashCode() : 0);
        hash = 11 * hash + (this.buildName != null ? this.buildName.hashCode() : 0);
        hash = 11 * hash + (this.species != null ? this.species.hashCode() : 0);
        return hash;
    }

    /**
     * toString() Override.
     * @return string summary of reference genome
     */
    @Override
    public String toString() {
        return "CancerStudy [genomeID=" + referenceGenomeId + ", genomeName=" + genomeName + ", species="
            + species + ", buildName=" + buildName + "]";
    }
    
}

