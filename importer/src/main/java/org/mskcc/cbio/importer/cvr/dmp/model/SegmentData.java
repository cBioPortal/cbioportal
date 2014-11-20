package org.mskcc.cbio.importer.cvr.dmp.model;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "ID",
        "chrom",
        "loc.start",
        "loc.end",
        "num.mark",
        "seg.mean"
})
/**
 * Created by criscuof on 10/27/14.
 */
public class SegmentData {
    @JsonProperty("ID")
    private String ID;
    @JsonProperty("chrom")
    private String chromosome;
    @JsonProperty("loc.start")
    private String locStart;
    @JsonProperty("loc.end")
    private String locEnd;
    @JsonProperty("num.mark")
    private String numMark;
    @JsonProperty("seg.mean")
    private Double segMean;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("ID")
    public String getID() {
        return ID;
    }
    @JsonProperty("ID")
    public void setID(String ID) {
        this.ID = ID;
    }
    @JsonProperty("chrom")
    public String getChromosome() {
        return chromosome;
    }
    @JsonProperty("chrom")
    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }
    @JsonProperty("loc.start")
    public String getLocStart() {
        return locStart;
    }
    @JsonProperty("loc.start")
    public void setLocStart(String locStart) {
        this.locStart = locStart;
    }
    @JsonProperty("loc.end")
    public String getLocEnd() {
        return locEnd;
    }
    @JsonProperty("loc.end")
    public void setLocEnd(String locEnd) {
        this.locEnd = locEnd;
    }
    @JsonProperty("num.mark")
    public String getNumMark() {
        return numMark;
    }
    @JsonProperty("num.mark")
    public void setNumMark(String numMark) {
        this.numMark = numMark;
    }
    @JsonProperty("seg.mean")
    public Double getSegMean() {
        return segMean;
    }
    @JsonProperty("seg.mean")
    public void setSegMean(Double segMean) {
        this.segMean = segMean;
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
        return new HashCodeBuilder().append(chromosome).append(locStart).append(locEnd).append(numMark).append(segMean).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SegmentData) == false) {
            return false;
        }
        SegmentData rhs = ((SegmentData) other);
        return new EqualsBuilder().append(chromosome, rhs.chromosome).append(locStart, rhs.locStart)
                .append(locEnd,rhs.locEnd)
                .append(numMark, rhs.numMark).append(segMean, rhs.segMean)
                 .append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
