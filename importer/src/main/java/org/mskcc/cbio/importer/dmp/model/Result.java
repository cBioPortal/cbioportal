
package org.mskcc.cbio.importer.dmp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    "cnv-intragenic-variants",
    "cnv-variants",
    "meta-data",
    "snp-exonic",
    "snp-silent"
})
public class Result {

    @JsonProperty("cnv-intragenic-variants")
    private List<Object> cnvIntragenicVariants = new ArrayList<Object>();
    @JsonProperty("cnv-variants")
    private List<CnvVariant> cnvVariants = new ArrayList<CnvVariant>();
    @JsonProperty("meta-data")
    private MetaData metaData;
    @JsonProperty("snp-exonic")
    private List<SnpExonic> snpExonic = new ArrayList<SnpExonic>();
    @JsonProperty("snp-silent")
    private List<SnpSilent> snpSilent = new ArrayList<SnpSilent>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The cnvIntragenicVariants
     */
    @JsonProperty("cnv-intragenic-variants")
    public List<Object> getCnvIntragenicVariants() {
        return cnvIntragenicVariants;
    }

    /**
     * 
     * @param cnvIntragenicVariants
     *     The cnv-intragenic-variants
     */
    @JsonProperty("cnv-intragenic-variants")
    public void setCnvIntragenicVariants(List<Object> cnvIntragenicVariants) {
        this.cnvIntragenicVariants = cnvIntragenicVariants;
    }

    /**
     * 
     * @return
     *     The cnvVariants
     */
    @JsonProperty("cnv-variants")
    public List<CnvVariant> getCnvVariants() {
        return cnvVariants;
    }

    /**
     * 
     * @param cnvVariants
     *     The cnv-variants
     */
    @JsonProperty("cnv-variants")
    public void setCnvVariants(List<CnvVariant> cnvVariants) {
        this.cnvVariants = cnvVariants;
    }

    /**
     * 
     * @return
     *     The metaData
     */
    @JsonProperty("meta-data")
    public MetaData getMetaData() {
        return metaData;
    }

    /**
     * 
     * @param metaData
     *     The meta-data
     */
    @JsonProperty("meta-data")
    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    /**
     * 
     * @return
     *     The snpExonic
     */
    @JsonProperty("snp-exonic")
    public List<SnpExonic> getSnpExonic() {
        return snpExonic;
    }

    /**
     * 
     * @param snpExonic
     *     The snp-exonic
     */
    @JsonProperty("snp-exonic")
    public void setSnpExonic(List<SnpExonic> snpExonic) {
        this.snpExonic = snpExonic;
    }

    /**
     * 
     * @return
     *     The snpSilent
     */
    @JsonProperty("snp-silent")
    public List<SnpSilent> getSnpSilent() {
        return snpSilent;
    }

    /**
     * 
     * @param snpSilent
     *     The snp-silent
     */
    @JsonProperty("snp-silent")
    public void setSnpSilent(List<SnpSilent> snpSilent) {
        this.snpSilent = snpSilent;
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
        return new HashCodeBuilder().append(cnvIntragenicVariants).append(cnvVariants).append(metaData).append(snpExonic).append(snpSilent).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Result) == false) {
            return false;
        }
        Result rhs = ((Result) other);
        return new EqualsBuilder().append(cnvIntragenicVariants, rhs.cnvIntragenicVariants).append(cnvVariants, rhs.cnvVariants).append(metaData, rhs.metaData).append(snpExonic, rhs.snpExonic).append(snpSilent, rhs.snpSilent).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
