package org.cbioportal.model;

import java.util.List;
import java.util.Set;

public class AlterationCountDetailed {
    private List<AlterationCountByGene> alterationCountsByGene;
    private Long profiledCasesCount;
    private Set<GenePanelToGene> profiledGenes;
    private boolean allGenesProfiled;

    public AlterationCountDetailed(
        List<AlterationCountByGene> alterationCountsByGene,
        Long profiledCasesCount,
        Set<GenePanelToGene> profiledGenes,
        boolean allGenesProfiled
    ) {
        this.alterationCountsByGene = alterationCountsByGene;
        this.profiledCasesCount = profiledCasesCount;
        this.profiledGenes = profiledGenes;
        this.allGenesProfiled = allGenesProfiled;
    }

    public List<AlterationCountByGene> getAlterationCountsByGene() {
        return alterationCountsByGene;
    }

    public void setAlterationCountsByGene(List<AlterationCountByGene> alterationCountsByGene) {
        this.alterationCountsByGene = alterationCountsByGene;
    }

    public Long getProfiledCasesCount() {
        return profiledCasesCount;
    }

    public void setProfiledCasesCount(Long profiledCasesCount) {
        this.profiledCasesCount = profiledCasesCount;
    }

    public Set<GenePanelToGene> getProfiledGenes() {
        return profiledGenes;
    }

    public void setProfiledGenes(Set<GenePanelToGene> profiledGenes) {
        this.profiledGenes = profiledGenes;
    }

    public boolean isAllGenesProfiled() {
        return allGenesProfiled;
    }

    public void setAllGenesProfiled(boolean allGenesProfiled) {
        this.allGenesProfiled = allGenesProfiled;
    }
}
