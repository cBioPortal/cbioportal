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
 * This represents the reference genome used by molecular profiling
 *
 * @author Kelsey Zhu
 */
public class ReferenceGenome {
    /**
     * NO_SUCH_STUDY Internal ID has not been assigned yet.
     */

    private int referenceGenomeId; // assigned by DB, auto increment sequence number
    private String genomeName;
    private String species;
    private String buildName; //genome assembly name
    private long genomeSize; //effective genome size
    private String url;
    private Date releaseDate;


    /**
     * Constructor.
     * @param genomeName        Name of the reference genome.
     * @param species           Species of the reference genome.
     * @param buildName         Name of genome assembly
     * @param genomeSize        Effective genome size
     * @param url               URL to download reference genome
     * @param releaseDate       Date genome assembly released            
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
                EqualsUtil.areEqual(this.buildName, that.buildName);
    }

    @Override
    public int hashCode() {
        int result = 3;
        result = 31 * result + this.referenceGenomeId;
        result = 31 * result + (this.genomeName != null ? this.genomeName.hashCode() : 0);
        result = 31 * result + (this.buildName != null ? this.buildName.hashCode() : 0);
        result = 31 * result + (this.species != null ? this.species.hashCode() : 0);
        return result;
    }

    /**
     * toString() Override.
     * @return string summary of reference genome
     */
    @Override
    public String toString() {
        return "Reference Genome [referenceGenomeID=" + referenceGenomeId + ", genomeName=" + genomeName + ", species="
            + species + ", buildName=" + buildName + "]";
    }

}