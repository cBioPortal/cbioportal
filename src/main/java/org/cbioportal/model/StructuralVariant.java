/*
 * Copyright (c) 2018 - 2022 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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

package org.cbioportal.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import org.cbioportal.model.UniqueKeyBase;

import java.io.Serializable;

public class StructuralVariant extends UniqueKeyBase implements Serializable {

    private String molecularProfileId;
    private String sampleId;
    private String patientId;
    private String studyId;
    private Integer site1EntrezGeneId;
    private String site1HugoSymbol;
    private String site1EnsemblTranscriptId;
    private String site1Chromosome;
    private Integer site1Position;
    private String site1Contig;
    private String site1Region;
    private Integer site1RegionNumber;
    private String site1Description;
    private Integer site2EntrezGeneId;
    private String site2HugoSymbol;
    private String site2EnsemblTranscriptId;
    private String site2Chromosome;
    private Integer site2Position;
    private String site2Contig;
    private String site2Region;
    private Integer site2RegionNumber;
    private String site2Description;
    private String site2EffectOnFrame;
    private String ncbiBuild;
    private String dnaSupport;
    private String rnaSupport;
    private Integer normalReadCount;
    private Integer tumorReadCount;
    private Integer normalVariantCount;
    private Integer tumorVariantCount;
    private Integer normalPairedEndReadCount;
    private Integer tumorPairedEndReadCount;
    private Integer normalSplitReadCount;
    private Integer tumorSplitReadCount;
    private String annotation;
    private String breakpointType;
    private String connectionType;
    private String eventInfo;
    private String variantClass;
    private Integer length;
    private String comments;
    private String driverFilter;
    private String driverFilterAnn;
    private String driverTiersFilter;
    private String driverTiersFilterAnn;
    private String svStatus;

    @JsonRawValue
    @Schema(type = "java.util.Map")
    private Object annotationJson;

    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String tumorSampleBarcode) {
        this.sampleId = tumorSampleBarcode;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public Integer getSite1EntrezGeneId() {
        return site1EntrezGeneId;
    }

    public void setSite1EntrezGeneId(Integer site1EntrezGeneId) {
        this.site1EntrezGeneId = site1EntrezGeneId;
    }

    public String getSite1HugoSymbol() {
        return site1HugoSymbol;
    }

    public void setSite1HugoSymbol(String site1HugoSymbol) {
        this.site1HugoSymbol = site1HugoSymbol;
    }

    public String getSite1EnsemblTranscriptId() {
        return site1EnsemblTranscriptId;
    }

    public void setSite1EnsemblTranscriptId(String site1TranscriptId) {
        this.site1EnsemblTranscriptId = site1TranscriptId;
    }
    
    public String getSite1Chromosome() {
        return site1Chromosome;
    }

    public void setSite1Chromosome(String site1Chrom) {
        this.site1Chromosome = site1Chrom;
    }

    public Integer getSite1Position() {
        return site1Position;
    }

    public void setSite1Position(Integer site1Pos) {
        this.site1Position = site1Pos;
    }

    public String getSite1Description() {
        return site1Description;
    }

    public void setSite1Description(String site1Desc) {
        this.site1Description = site1Desc;
    }

    public Integer getSite2EntrezGeneId() {
        return site2EntrezGeneId;
    }

    public void setSite2EntrezGeneId(Integer site2EntrezGeneId) {
        this.site2EntrezGeneId = site2EntrezGeneId;
    }

    public String getSite2HugoSymbol() {
        return site2HugoSymbol;
    }

    public void setSite2HugoSymbol(String site2HugoSymbol) {
        this.site2HugoSymbol = site2HugoSymbol;
    }

    public String getSite2EnsemblTranscriptId() {
        return site2EnsemblTranscriptId;
    }

    public void setSite2EnsemblTranscriptId(String site2TranscriptId) {
        this.site2EnsemblTranscriptId = site2TranscriptId;
    }
    
    public String getSite2Chromosome() {
        return site2Chromosome;
    }

    public void setSite2Chromosome(String site2Chrom) {
        this.site2Chromosome = site2Chrom;
    }

    public Integer getSite2Position() {
        return site2Position;
    }

    public void setSite2Position(Integer site2Pos) {
        this.site2Position = site2Pos;
    }

    public String getSite2Description() {
        return site2Description;
    }

    public void setSite2Description(String site2Desc) {
        this.site2Description = site2Desc;
    }

    public String getSite2EffectOnFrame() {
        return site2EffectOnFrame;
    }

    public void setSite2EffectOnFrame(String site2EffectOnFrame) {
        this.site2EffectOnFrame = site2EffectOnFrame;
    }

    public String getNcbiBuild() {
        return ncbiBuild;
    }

    public void setNcbiBuild(String ncbiBuild) {
        this.ncbiBuild = ncbiBuild;
    }

    public String getDnaSupport() {
        return dnaSupport;
    }

    public void setDnaSupport(String dnaSupport) {
        this.dnaSupport = dnaSupport;
    }

    public String getRnaSupport() {
        return rnaSupport;
    }

    public void setRnaSupport(String rnaSupport) {
        this.rnaSupport = rnaSupport;
    }

    public Integer getNormalReadCount() {
        return normalReadCount;
    }

    public void setNormalReadCount(Integer normalReadCount) {
        this.normalReadCount = normalReadCount;
    }

    public Integer getTumorReadCount() {
        return tumorReadCount;
    }

    public void setTumorReadCount(Integer tumorReadCount) {
        this.tumorReadCount = tumorReadCount;
    }

    public Integer getNormalVariantCount() {
        return normalVariantCount;
    }

    public void setNormalVariantCount(Integer normalVariantCount) {
        this.normalVariantCount = normalVariantCount;
    }

    public Integer getTumorVariantCount() {
        return tumorVariantCount;
    }

    public void setTumorVariantCount(Integer tumorVariantCount) {
        this.tumorVariantCount = tumorVariantCount;
    }

    public Integer getNormalPairedEndReadCount() {
        return normalPairedEndReadCount;
    }

    public void setNormalPairedEndReadCount(Integer normalPairedEndReadCount) {
        this.normalPairedEndReadCount = normalPairedEndReadCount;
    }

    public Integer getTumorPairedEndReadCount() {
        return tumorPairedEndReadCount;
    }

    public void setTumorPairedEndReadCount(Integer tumorPairedEndReadCount) {
        this.tumorPairedEndReadCount = tumorPairedEndReadCount;
    }

    public Integer getNormalSplitReadCount() {
        return normalSplitReadCount;
    }

    public void setNormalSplitReadCount(Integer normalSplitReadCount) {
        this.normalSplitReadCount = normalSplitReadCount;
    }

    public Integer getTumorSplitReadCount() {
        return tumorSplitReadCount;
    }

    public void setTumorSplitReadCount(Integer tumorSplitReadCount) {
        this.tumorSplitReadCount = tumorSplitReadCount;
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

    public String getVariantClass() {
        return variantClass;
    }

    public void setVariantClass(String variantClass) {
        this.variantClass = variantClass;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getDriverFilter() {
        return driverFilter;
    }

    public void setDriverFilter(String driverFilter) {
        this.driverFilter = driverFilter;
    }

    public String getDriverFilterAnn() {
        return driverFilterAnn;
    }

    public void setDriverFilterAnn(String driverFilterAnn) {
        this.driverFilterAnn = driverFilterAnn;
    }

    public String getDriverTiersFilter() {
        return driverTiersFilter;
    }

    public void setDriverTiersFilter(String driverTiersFilter) {
        this.driverTiersFilter = driverTiersFilter;
    }

    public String getDriverTiersFilterAnn() {
        return driverTiersFilterAnn;
    }

    public void setDriverTiersFilterAnn(String driverTiersFilterAnn) {
        this.driverTiersFilterAnn = driverTiersFilterAnn;
    }

    public String getSvStatus() {
        return svStatus;
    }

    public void setSvStatus(String svStatus) {
        this.svStatus = svStatus;
    }

    public String getSite1Contig() {
        return site1Contig;
    }

    public void setSite1Contig(String site1Contig) {
        this.site1Contig = site1Contig;
    }

    public String getSite1Region() {
        return site1Region;
    }

    public void setSite1Region(String site1Region) {
        this.site1Region = site1Region;
    }

    public Integer getSite1RegionNumber() {
        return site1RegionNumber;
    }

    public void setSite1RegionNumber(Integer site1RegionNumber) {
        this.site1RegionNumber = site1RegionNumber;
    }

    public String getSite2Contig() {
        return site2Contig;
    }

    public void setSite2Contig(String site2Contig) {
        this.site2Contig = site2Contig;
    }

    public String getSite2Region() {
        return site2Region;
    }

    public void setSite2Region(String site2Region) {
        this.site2Region = site2Region;
    }

    public Integer getSite2RegionNumber() {
        return site2RegionNumber;
    }

    public void setSite2RegionNumber(Integer site2RegionNumber) {
        this.site2RegionNumber = site2RegionNumber;
    }

    public Object getAnnotationJson() {
        return annotationJson;
    }

    public void setAnnotationJson(String annotationJson) {
        this.annotationJson = annotationJson;
    }
}
