package org.cbioportal.web.util;

import java.math.BigDecimal;

public class DensityPlotParameters {
    private final Integer xAxisBinCount;
    private final Integer yAxisBinCount;
    private final BigDecimal xAxisStart;
    private final BigDecimal xAxisEnd;
    private final BigDecimal yAxisStart;
    private final BigDecimal yAxisEnd;
    private final Boolean xAxisLogScale;
    private final Boolean yAxisLogScale;
    private final String xAxisAttributeId;
    private final String yAxisAttributeId;
    
    DensityPlotParameters(Builder builder) {
        this.xAxisBinCount = builder.xAxisBinCount;
        this.yAxisBinCount = builder.yAxisBinCount;
        this.xAxisStart = builder.xAxisStart;
        this.xAxisEnd = builder.xAxisEnd;
        this.yAxisStart = builder.yAxisStart;
        this.yAxisEnd = builder.yAxisEnd;
        this.xAxisAttributeId = builder.xAxisAttributeId;
        this.yAxisAttributeId = builder.yAxisAttributeId;
        this.xAxisLogScale = builder.xAxisLogScale;
        this.yAxisLogScale = builder.yAxisLogScale;
    }
    
    public Integer getXAxisBinCount() {
        return xAxisBinCount;
    }
    public Integer getYAxisBinCount() {
        return yAxisBinCount;
    }
    public BigDecimal getXAxisStart() {
        return xAxisStart;
    }
    public BigDecimal getXAxisEnd() {
        return xAxisEnd;
    }
    public BigDecimal getYAxisStart() {
        return yAxisStart;
    }
    public BigDecimal getYAxisEnd() {
        return yAxisEnd;
    }
    public Boolean getXAxisLogScale() {
        return xAxisLogScale;
    }
    public Boolean getYAxisLogScale() {
        return yAxisLogScale;
    }
    public String getXAxisAttributeId() {
        return xAxisAttributeId;
    }
    public String getYAxisAttributeId() {
        return yAxisAttributeId;
    }    
    
    public static class Builder {
        private Integer xAxisBinCount;
        private Integer yAxisBinCount;
        private BigDecimal xAxisStart;
        private BigDecimal xAxisEnd;
        private BigDecimal yAxisStart;
        private BigDecimal yAxisEnd;
        private Boolean xAxisLogScale;
        private Boolean yAxisLogScale;
        private String xAxisAttributeId;
        private String yAxisAttributeId;
        
        public Builder xAxisBinCount(Integer xAxisBinCount) {
            this.xAxisBinCount = xAxisBinCount;
            return this;
        }
        public Builder yAxisBinCount(Integer yAxisBinCount) {
            this.yAxisBinCount = yAxisBinCount;
            return this;
        }
        public Builder xAxisStart(BigDecimal xAxisStart) {
            this.xAxisStart = xAxisStart;
            return this;
        }
        public Builder xAxisEnd(BigDecimal xAxisEnd) {
            this.xAxisEnd = xAxisEnd;
            return this;
        }
        public Builder yAxisStart(BigDecimal yAxisStart) {
            this.yAxisStart = yAxisStart;
            return this;
        }
        public Builder yAxisEnd(BigDecimal yAxisEnd) {
            this.yAxisEnd = yAxisEnd;
            return this;
        }
        public Builder xAxisLogScale(Boolean xAxisLogScale) {
            this.xAxisLogScale = xAxisLogScale;
            return this;
        }
        public Builder yAxisLogScale(Boolean yAxisLogScale) {
            this.yAxisLogScale = yAxisLogScale;
            return this;
        }
        public Builder xAxisAttributeId(String xAxisAttributeId) {
            this.xAxisAttributeId = xAxisAttributeId;
            return this;
        }
        public Builder yAxisAttributeId(String yAxisAttributeId) {
            this.yAxisAttributeId = yAxisAttributeId;
            return this;
        }
        public DensityPlotParameters build() {
            return new DensityPlotParameters(this);
            
        }
    }
}
