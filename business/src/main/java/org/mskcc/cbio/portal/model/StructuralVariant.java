/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
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

import org.cbioportal.model.Gene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;

import java.io.Serializable;

public class StructuralVariant implements Serializable {
    private Integer sampleId;
    private String annotation;
    private String breakpointType;
    private String comments;
    private String confidenceClass;
    private String connectionType;
    private String eventInfo;
    private String mapq;
    private Integer normalReadCount;
    private Integer normalVariantCount;
    private Integer pairedEndReadSupport;
    private String site1Chrom;
    private String site1Desc;
    private String site1Gene;
    private Integer site1Pos;
    private String site2Chrom;
    private String site2Desc;
    private String site2Gene;
    private Integer site2Pos;
    private Integer splitReadSupport;
    private String svClassName;
    private String svDesc;
    private Integer svLength;
    private Integer tumorReadCount;
    private Integer tumorVariantCount;
    private String variantStatusName;
    private Integer geneticProfileId;
    private MolecularProfile geneticProfile;
    private Gene gene1;
    private Gene gene2;
    private Sample sample;

    public MolecularProfile getGeneticProfile() {
        return geneticProfile;
    }

    public void setGeneticProfile(MolecularProfile geneticProfile) {
        this.geneticProfile = geneticProfile;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public Gene getGene1() {
        return gene1;
    }

    public void setGene1(Gene gene1) {
        this.gene1 = gene1;
    }

    public Gene getGene2() {
        return gene2;
    }

    public void setGene2(Gene gene2) {
        this.gene2 = gene2;
    }

    public Integer getSampleId() {
        return sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getBreakpointType() {
        return breakpointType;
    }

    public void setBreakpointType(String breakpointType) {
        this.breakpointType = breakpointType;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getConfidenceClass() {
        return confidenceClass;
    }

    public void setConfidenceClass(String confidenceClass) {
        this.confidenceClass = confidenceClass;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getEventInfo() {
        return eventInfo;
    }

    public void setEventInfo(String eventInfo) {
        this.eventInfo = eventInfo;
    }

    public String getMapq() {
        return mapq;
    }

    public void setMapq(String mapq) {
        this.mapq = mapq;
    }

    public Integer getNormalReadCount() {
        return normalReadCount;
    }

    public void setNormalReadCount(Integer normalReadCount) {
        this.normalReadCount = normalReadCount;
    }

    public Integer getNormalVariantCount() {
        return normalVariantCount;
    }

    public void setNormalVariantCount(Integer normalVariantCount) {
        this.normalVariantCount = normalVariantCount;
    }

    public Integer getPairedEndReadSupport() {
        return pairedEndReadSupport;
    }

    public void setPairedEndReadSupport(Integer pairedEndReadSupport) {
        this.pairedEndReadSupport = pairedEndReadSupport;
    }

    public String getSite1Chrom() {
        return site1Chrom;
    }

    public void setSite1Chrom(String site1Chrom) {
        this.site1Chrom = site1Chrom;
    }

    public String getSite1Desc() {
        return site1Desc;
    }

    public void setSite1Desc(String site1Desc) {
        this.site1Desc = site1Desc;
    }

    public String getSite1Gene() {
        return site1Gene;
    }

    public void setSite1Gene(String site1Gene) {
        this.site1Gene = site1Gene;
    }

    public Integer getSite1Pos() {
        return site1Pos;
    }

    public void setSite1Pos(Integer site1Pos) {
        this.site1Pos = site1Pos;
    }

    public String getSite2Chrom() {
        return site2Chrom;
    }

    public void setSite2Chrom(String site2Chrom) {
        this.site2Chrom = site2Chrom;
    }

    public String getSite2Desc() {
        return site2Desc;
    }

    public void setSite2Desc(String site2Desc) {
        this.site2Desc = site2Desc;
    }

    public String getSite2Gene() {
        return site2Gene;
    }

    public void setSite2Gene(String site2Gene) {
        this.site2Gene = site2Gene;
    }

    public Integer getSite2Pos() {
        return site2Pos;
    }

    public void setSite2Pos(Integer site2Pos) {
        this.site2Pos = site2Pos;
    }

    public Integer getSplitReadSupport() {
        return splitReadSupport;
    }

    public void setSplitReadSupport(Integer splitReadSupport) {
        this.splitReadSupport = splitReadSupport;
    }

    public String getSvClassName() {
        return svClassName;
    }

    public void setSvClassName(String svClassName) {
        this.svClassName = svClassName;
    }

    public String getSvDesc() {
        return svDesc;
    }

    public void setSvDesc(String svDesc) {
        this.svDesc = svDesc;
    }

    public Integer getSvLength() {
        return svLength;
    }

    public void setSvLength(Integer svLength) {
        this.svLength = svLength;
    }

    public Integer getTumorReadCount() {
        return tumorReadCount;
    }

    public void setTumorReadCount(Integer tumorReadCount) {
        this.tumorReadCount = tumorReadCount;
    }

    public Integer getTumorVariantCount() {
        return tumorVariantCount;
    }

    public void setTumorVariantCount(Integer tumorVariantCount) {
        this.tumorVariantCount = tumorVariantCount;
    }

    public String getVariantStatusName() {
        return variantStatusName;
    }

    public void setVariantStatusName(String variantStatusName) {
        this.variantStatusName = variantStatusName;
    }

    public Integer getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(Integer geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }
}
