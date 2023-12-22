package org.cbioportal.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

public class Mutation extends Alteration implements Serializable {
    
    private String center;
    private String mutationStatus;
    private String validationStatus;
    private Integer tumorAltCount;
    private Integer tumorRefCount;
    private Integer normalAltCount;
    private Integer normalRefCount;
    private String aminoAcidChange;
    private String chr;
    private Long startPosition;
    private Long endPosition;
    private String referenceAllele;
    private String tumorSeqAllele;
    private String proteinChange;
    private String mutationType;
    private String ncbiBuild;
    private String variantType;
    private String refseqMrnaId;
    private Integer proteinPosStart;
    private Integer proteinPosEnd;
    private String keyword;
    private AlleleSpecificCopyNumber alleleSpecificCopyNumber;
    @JsonRawValue
    @Schema(type = "java.util.Map")
    private Object annotationJSON;
    
    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getMutationStatus() {
        return mutationStatus;
    }

    public void setMutationStatus(String mutationStatus) {
        this.mutationStatus = mutationStatus;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public Integer getTumorAltCount() {
        return tumorAltCount;
    }

    public void setTumorAltCount(Integer tumorAltCount) {
        this.tumorAltCount = tumorAltCount;
    }

    public Integer getTumorRefCount() {
        return tumorRefCount;
    }

    public void setTumorRefCount(Integer tumorRefCount) {
        this.tumorRefCount = tumorRefCount;
    }

    public Integer getNormalAltCount() {
        return normalAltCount;
    }

    public void setNormalAltCount(Integer normalAltCount) {
        this.normalAltCount = normalAltCount;
    }

    public Integer getNormalRefCount() {
        return normalRefCount;
    }

    public void setNormalRefCount(Integer normalRefCount) {
        this.normalRefCount = normalRefCount;
    }

    public String getAminoAcidChange() {
        return aminoAcidChange;
    }

    public void setAminoAcidChange(String aminoAcidChange) {
        this.aminoAcidChange = aminoAcidChange;
    }
    
    public String getChr() { return chr; }
    
    public void setChr(String chr) { this.chr = chr; }

    public Long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Long startPosition) {
        this.startPosition = startPosition;
    }

    public Long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(Long endPosition) {
        this.endPosition = endPosition;
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

    public String getProteinChange() {
        return proteinChange;
    }

    public void setProteinChange(String proteinChange) {
        this.proteinChange = proteinChange;
    }

    public String getMutationType() {
        return mutationType;
    }

    public void setMutationType(String mutationType) {
        this.mutationType = mutationType;
    }

    public String getNcbiBuild() {
        return ncbiBuild;
    }

    public void setNcbiBuild(String ncbiBuild) {
        this.ncbiBuild = ncbiBuild;
    }

    public String getVariantType() {
        return variantType;
    }

    public void setVariantType(String variantType) {
        this.variantType = variantType;
    }
    public String getRefseqMrnaId() {
        return refseqMrnaId;
    }

    public void setRefseqMrnaId(String refseqMrnaId) {
        this.refseqMrnaId = refseqMrnaId;
    }
    
    public Integer getProteinPosStart() {
        return proteinPosStart;
    }

    public void setProteinPosStart(Integer proteinPosStart) {
        this.proteinPosStart = proteinPosStart;
    }

    public Integer getProteinPosEnd() {
        return proteinPosEnd;
    }

    public void setProteinPosEnd(Integer proteinPosEnd) {
        this.proteinPosEnd = proteinPosEnd;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public AlleleSpecificCopyNumber getAlleleSpecificCopyNumber() {
        return alleleSpecificCopyNumber;
    }

    public void setAlleleSpecificCopyNumber(AlleleSpecificCopyNumber alleleSpecificCopyNumber) {
        this.alleleSpecificCopyNumber = alleleSpecificCopyNumber;
    }

    public Object getAnnotationJSON() {
        return annotationJSON;
    }

    public void setAnnotationJSON(String annotationJSON) {
        this.annotationJSON = annotationJSON;
    }

}
