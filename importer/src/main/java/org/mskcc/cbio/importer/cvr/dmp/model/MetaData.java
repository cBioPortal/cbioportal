
package org.mskcc.cbio.importer.cvr.dmp.model;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "alys2sample_id",
    "cbx_patient_id",
    "cbx_sample_id",
    "dmp_alys_task_id",
    "dmp_alys_task_name",
    "dmp_patient_id",
    "dmp_sample_id",
    "dmp_sample_so_id",
    "gender",
    "is_metastasis",
    "legacy_patient_id",
    "legacy_sample_id",
    "metastasis_site",
    "mrev_comments",
    "retrieve_status",
    "sample_coverage",
    "so_comments",
    "so_status_name",
    "tumor_purity",
    "tumor_type_name"
})
public class MetaData {

    @JsonProperty("alys2sample_id")
    private Integer alys2sampleId;
    @JsonProperty("cbx_patient_id")
    private Integer cbxPatientId;
    @JsonProperty("cbx_sample_id")
    private Integer cbxSampleId;
    @JsonProperty("dmp_alys_task_id")
    private Integer dmpAlysTaskId;
    @JsonProperty("dmp_alys_task_name")
    private String dmpAlysTaskName;
    @JsonProperty("dmp_patient_id")
    private String dmpPatientId;
    @JsonProperty("dmp_sample_id")
    private String dmpSampleId;
    @JsonProperty("dmp_sample_so_id")
    private Integer dmpSampleSoId;
    @JsonProperty("gender")
    private Integer gender;
    @JsonProperty("is_metastasis")
    private Object isMetastasis;
    @JsonProperty("legacy_patient_id")
    private String legacyPatientId;
    @JsonProperty("legacy_sample_id")
    private Object legacySampleId;
    @JsonProperty("metastasis_site")
    private Object metastasisSite;
    @JsonProperty("mrev_comments")
    private String mrevComments;
    @JsonProperty("retrieve_status")
    private Integer retrieveStatus;
    @JsonProperty("sample_coverage")
    private Integer sampleCoverage;
    @JsonProperty("so_comments")
    private String soComments;
    @JsonProperty("so_status_name")
    private String soStatusName;
    @JsonProperty("tumor_purity")
    private Object tumorPurity;
    @JsonProperty("tumor_type_name")
    private String tumorTypeName;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The alys2sampleId
     */
    @JsonProperty("alys2sample_id")
    public Integer getAlys2sampleId() {
        return alys2sampleId;
    }

    /**
     * 
     * @param alys2sampleId
     *     The alys2sample_id
     */
    @JsonProperty("alys2sample_id")
    public void setAlys2sampleId(Integer alys2sampleId) {
        this.alys2sampleId = alys2sampleId;
    }

    /**
     * 
     * @return
     *     The cbxPatientId
     */
    @JsonProperty("cbx_patient_id")
    public Integer getCbxPatientId() {
        return cbxPatientId;
    }

    /**
     * 
     * @param cbxPatientId
     *     The cbx_patient_id
     */
    @JsonProperty("cbx_patient_id")
    public void setCbxPatientId(Integer cbxPatientId) {
        this.cbxPatientId = cbxPatientId;
    }

    /**
     * 
     * @return
     *     The cbxSampleId
     */
    @JsonProperty("cbx_sample_id")
    public Integer getCbxSampleId() {
        return cbxSampleId;
    }

    /**
     * 
     * @param cbxSampleId
     *     The cbx_sample_id
     */
    @JsonProperty("cbx_sample_id")
    public void setCbxSampleId(Integer cbxSampleId) {
        this.cbxSampleId = cbxSampleId;
    }

    /**
     * 
     * @return
     *     The dmpAlysTaskId
     */
    @JsonProperty("dmp_alys_task_id")
    public Integer getDmpAlysTaskId() {
        return dmpAlysTaskId;
    }

    /**
     * 
     * @param dmpAlysTaskId
     *     The dmp_alys_task_id
     */
    @JsonProperty("dmp_alys_task_id")
    public void setDmpAlysTaskId(Integer dmpAlysTaskId) {
        this.dmpAlysTaskId = dmpAlysTaskId;
    }

    /**
     * 
     * @return
     *     The dmpAlysTaskName
     */
    @JsonProperty("dmp_alys_task_name")
    public String getDmpAlysTaskName() {
        return dmpAlysTaskName;
    }

    /**
     * 
     * @param dmpAlysTaskName
     *     The dmp_alys_task_name
     */
    @JsonProperty("dmp_alys_task_name")
    public void setDmpAlysTaskName(String dmpAlysTaskName) {
        this.dmpAlysTaskName = dmpAlysTaskName;
    }

    /**
     * 
     * @return
     *     The dmpPatientId
     */
    @JsonProperty("dmp_patient_id")
    public String getDmpPatientId() {
        return dmpPatientId;
    }

    /**
     * 
     * @param dmpPatientId
     *     The dmp_patient_id
     */
    @JsonProperty("dmp_patient_id")
    public void setDmpPatientId(String dmpPatientId) {
        this.dmpPatientId = dmpPatientId;
    }

    /**
     * 
     * @return
     *     The dmpSampleId
     */
    @JsonProperty("dmp_sample_id")
    public String getDmpSampleId() {
        return dmpSampleId;
    }

    /**
     * 
     * @param dmpSampleId
     *     The dmp_sample_id
     */
    @JsonProperty("dmp_sample_id")
    public void setDmpSampleId(String dmpSampleId) {
        this.dmpSampleId = dmpSampleId;
    }

    /**
     * 
     * @return
     *     The dmpSampleSoId
     */
    @JsonProperty("dmp_sample_so_id")
    public Integer getDmpSampleSoId() {
        return dmpSampleSoId;
    }

    /**
     * 
     * @param dmpSampleSoId
     *     The dmp_sample_so_id
     */
    @JsonProperty("dmp_sample_so_id")
    public void setDmpSampleSoId(Integer dmpSampleSoId) {
        this.dmpSampleSoId = dmpSampleSoId;
    }

    /**
     * 
     * @return
     *     The gender
     */
    @JsonProperty("gender")
    public Integer getGender() {
        return gender;
    }

    /**
     * 
     * @param gender
     *     The gender
     */
    @JsonProperty("gender")
    public void setGender(Integer gender) {
        this.gender = gender;
    }

    /**
     * 
     * @return
     *     The isMetastasis
     */
    @JsonProperty("is_metastasis")
    public Object getIsMetastasis() {
        return isMetastasis;
    }

    /**
     * 
     * @param isMetastasis
     *     The is_metastasis
     */
    @JsonProperty("is_metastasis")
    public void setIsMetastasis(Object isMetastasis) {
        this.isMetastasis = isMetastasis;
    }

    /**
     * 
     * @return
     *     The legacyPatientId
     */
    @JsonProperty("legacy_patient_id")
    public String getLegacyPatientId() {
        return legacyPatientId;
    }

    /**
     * 
     * @param legacyPatientId
     *     The legacy_patient_id
     */
    @JsonProperty("legacy_patient_id")
    public void setLegacyPatientId(String legacyPatientId) {
        this.legacyPatientId = legacyPatientId;
    }

    /**
     * 
     * @return
     *     The legacySampleId
     */
    @JsonProperty("legacy_sample_id")
    public Object getLegacySampleId() {
        return legacySampleId;
    }

    /**
     * 
     * @param legacySampleId
     *     The legacy_sample_id
     */
    @JsonProperty("legacy_sample_id")
    public void setLegacySampleId(Object legacySampleId) {
        this.legacySampleId = legacySampleId;
    }

    /**
     * 
     * @return
     *     The metastasisSite
     */
    @JsonProperty("metastasis_site")
    public Object getMetastasisSite() {
        return metastasisSite;
    }

    /**
     * 
     * @param metastasisSite
     *     The metastasis_site
     */
    @JsonProperty("metastasis_site")
    public void setMetastasisSite(Object metastasisSite) {
        this.metastasisSite = metastasisSite;
    }

    /**
     * 
     * @return
     *     The mrevComments
     */
    @JsonProperty("mrev_comments")
    public String getMrevComments() {
        return mrevComments;
    }

    /**
     * 
     * @param mrevComments
     *     The mrev_comments
     */
    @JsonProperty("mrev_comments")
    public void setMrevComments(String mrevComments) {
        this.mrevComments = mrevComments;
    }

    /**
     * 
     * @return
     *     The retrieveStatus
     */
    @JsonProperty("retrieve_status")
    public Integer getRetrieveStatus() {
        return retrieveStatus;
    }

    /**
     * 
     * @param retrieveStatus
     *     The retrieve_status
     */
    @JsonProperty("retrieve_status")
    public void setRetrieveStatus(Integer retrieveStatus) {
        this.retrieveStatus = retrieveStatus;
    }

    /**
     * 
     * @return
     *     The sampleCoverage
     */
    @JsonProperty("sample_coverage")
    public Integer getSampleCoverage() {
        return sampleCoverage;
    }

    /**
     * 
     * @param sampleCoverage
     *     The sample_coverage
     */
    @JsonProperty("sample_coverage")
    public void setSampleCoverage(Integer sampleCoverage) {
        this.sampleCoverage = sampleCoverage;
    }

    /**
     * 
     * @return
     *     The soComments
     */
    @JsonProperty("so_comments")
    public String getSoComments() {
        return soComments;
    }

    /**
     * 
     * @param soComments
     *     The so_comments
     */
    @JsonProperty("so_comments")
    public void setSoComments(String soComments) {
        this.soComments = soComments;
    }

    /**
     * 
     * @return
     *     The soStatusName
     */
    @JsonProperty("so_status_name")
    public String getSoStatusName() {
        return soStatusName;
    }

    /**
     * 
     * @param soStatusName
     *     The so_status_name
     */
    @JsonProperty("so_status_name")
    public void setSoStatusName(String soStatusName) {
        this.soStatusName = soStatusName;
    }

    /**
     * 
     * @return
     *     The tumorPurity
     */
    @JsonProperty("tumor_purity")
    public Object getTumorPurity() {
        return tumorPurity;
    }

    /**
     * 
     * @param tumorPurity
     *     The tumor_purity
     */
    @JsonProperty("tumor_purity")
    public void setTumorPurity(Object tumorPurity) {
        this.tumorPurity = tumorPurity;
    }

    /**
     * 
     * @return
     *     The tumorTypeName
     */
    @JsonProperty("tumor_type_name")
    public String getTumorTypeName() {
        return tumorTypeName;
    }

    /**
     * 
     * @param tumorTypeName
     *     The tumor_type_name
     */
    @JsonProperty("tumor_type_name")
    public void setTumorTypeName(String tumorTypeName) {
        this.tumorTypeName = tumorTypeName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(alys2sampleId).append(cbxPatientId).append(cbxSampleId).append(dmpAlysTaskId).append(dmpAlysTaskName).append(dmpPatientId).append(dmpSampleId).append(dmpSampleSoId).append(gender).append(isMetastasis).append(legacyPatientId).append(legacySampleId).append(metastasisSite).append(mrevComments).append(retrieveStatus).append(sampleCoverage).append(soComments).append(soStatusName).append(tumorPurity).append(tumorTypeName).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MetaData) == false) {
            return false;
        }
        MetaData rhs = ((MetaData) other);
        return new EqualsBuilder().append(alys2sampleId, rhs.alys2sampleId).append(cbxPatientId, rhs.cbxPatientId).append(cbxSampleId, rhs.cbxSampleId).append(dmpAlysTaskId, rhs.dmpAlysTaskId).append(dmpAlysTaskName, rhs.dmpAlysTaskName).append(dmpPatientId, rhs.dmpPatientId).append(dmpSampleId, rhs.dmpSampleId).append(dmpSampleSoId, rhs.dmpSampleSoId).append(gender, rhs.gender).append(isMetastasis, rhs.isMetastasis).append(legacyPatientId, rhs.legacyPatientId).append(legacySampleId, rhs.legacySampleId).append(metastasisSite, rhs.metastasisSite).append(mrevComments, rhs.mrevComments).append(retrieveStatus, rhs.retrieveStatus).append(sampleCoverage, rhs.sampleCoverage).append(soComments, rhs.soComments).append(soStatusName, rhs.soStatusName).append(tumorPurity, rhs.tumorPurity).append(tumorTypeName, rhs.tumorTypeName).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
