
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
    "chrom",
    "loc.start",
    "loc.end",
    "num.mark",
    "seg.mean"
})
public class SegmentDatum {

    @JsonProperty("chrom")
    private String chrom;
    @JsonProperty("loc.start")
    private String locStart;
    @JsonProperty("loc.end")
    private String locEnd;
    @JsonProperty("num.mark")
    private String numMark;
    @JsonProperty("seg.mean")
    private String segMean;
    @JsonIgnore
    private String id;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The chrom
     */
    @JsonProperty("chrom")
    public String getChrom() {
        return chrom;
    }

    /**
     * 
     * @param chrom
     *     The chrom
     */
    @JsonProperty("chrom")
    public void setChrom(String chrom) {
        this.chrom = chrom;
    }

    /**
     * 
     * @return
     *     The locStart
     */
    @JsonProperty("loc.start")
    public String getLocStart() {
        return locStart;
    }

    /**
     * 
     * @param locStart
     *     The loc.start
     */
    @JsonProperty("loc.start")
    public void setLocStart(String locStart) {
        this.locStart = locStart;
    }

    /**
     * 
     * @return
     *     The locEnd
     */
    @JsonProperty("loc.end")
    public String getLocEnd() {
        return locEnd;
    }

    /**
     * 
     * @param locEnd
     *     The loc.end
     */
    @JsonProperty("loc.end")
    public void setLocEnd(String locEnd) {
        this.locEnd = locEnd;
    }

    /**
     * 
     * @return
     *     The numMark
     */
    @JsonProperty("num.mark")
    public String getNumMark() {
        return numMark;
    }

    /**
     * 
     * @param numMark
     *     The num.mark
     */
    @JsonProperty("num.mark")
    public void setNumMark(String numMark) {
        this.numMark = numMark;
    }

    /**
     * 
     * @return
     *     The segMean
     */
    @JsonProperty("seg.mean")
    public String getSegMean() {
        return segMean;
    }

    /**
     * 
     * @param segMean
     *     The seg.mean
     */
    @JsonProperty("seg.mean")
    public void setSegMean(String segMean) {
        this.segMean = segMean;
    }


    @JsonAnyGetter
    public String getID() { return this.id;}
    @JsonAnySetter
    public void setID(String id) { this.id = id;}


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
        return new HashCodeBuilder().append(chrom).append(locStart).append(locEnd).append(numMark).append(segMean).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SegmentDatum) == false) {
            return false;
        }
        SegmentDatum rhs = ((SegmentDatum) other);
        return new EqualsBuilder().append(chrom, rhs.chrom).append(locStart, rhs.locStart).append(locEnd, rhs.locEnd).append(numMark, rhs.numMark).append(segMean, rhs.segMean).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
