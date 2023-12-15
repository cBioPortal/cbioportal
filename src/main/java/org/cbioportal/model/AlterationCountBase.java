package org.cbioportal.model;

<<<<<<<< HEAD:src/main/java/org/cbioportal/model/AlterationCountBase.java
import java.io.Serializable;
import java.util.Set;

public abstract class AlterationCountBase implements Serializable {
========
import java.math.BigDecimal;

public class AlterationCountByGene extends AlterationCountBase {
>>>>>>>> master:model/src/main/java/org/cbioportal/model/AlterationCountByGene.java

    private Integer numberOfAlteredCases;
<<<<<<<< HEAD:src/main/java/org/cbioportal/model/AlterationCountBase.java
    private Integer totalCount;
    private Integer numberOfProfiledCases;
    private Set<String> matchingGenePanelIds;
========
    private BigDecimal qValue;
>>>>>>>> master:model/src/main/java/org/cbioportal/model/AlterationCountByGene.java

    public Integer getNumberOfAlteredCases() {
        return numberOfAlteredCases;
    }

    public void setNumberOfAlteredCases(Integer numberOfAlteredCases) {
        this.numberOfAlteredCases = numberOfAlteredCases;
    }

<<<<<<<< HEAD:src/main/java/org/cbioportal/model/AlterationCountBase.java
    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
    
    public Integer getNumberOfProfiledCases() {
        return numberOfProfiledCases;
    }

    public void setNumberOfProfiledCases(Integer numberOfProfiledCases) {
        this.numberOfProfiledCases = numberOfProfiledCases;
    }

    public Set<String> getMatchingGenePanelIds() {
        return matchingGenePanelIds;
========
    public BigDecimal getqValue() {
        return qValue;
    }

    public void setqValue(BigDecimal qValue) {
        this.qValue = qValue;
    }

    @Override
    public String getUniqueEventKey() {
        return hugoGeneSymbol;
>>>>>>>> master:model/src/main/java/org/cbioportal/model/AlterationCountByGene.java
    }

    @Override
    public String[] getHugoGeneSymbols() {
        return new String[]{hugoGeneSymbol};
    }
<<<<<<<< HEAD:src/main/java/org/cbioportal/model/AlterationCountBase.java
    
    public abstract String getUniqueEventKey();
    
    public abstract String[] getHugoGeneSymbols();
    
    public abstract Integer[] getEntrezGeneIds();
========

    @Override
    public Integer[] getEntrezGeneIds() {
        return new Integer[]{entrezGeneId};
    }
>>>>>>>> master:model/src/main/java/org/cbioportal/model/AlterationCountByGene.java

}
