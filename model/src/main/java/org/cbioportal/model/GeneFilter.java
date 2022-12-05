package org.cbioportal.model;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class GeneFilter implements Serializable {

    private Set<String> molecularProfileIds;
    private List<List<GeneFilterQuery>> geneQueries;
    private List<List<StructVarFilterQuery>> structVarQueries;

    @AssertTrue(message = "'specialValue' field of gene1/gene2 StructVarGeneSubQueries cannot be both ANY_GENE or NO_GENE.")
    private boolean isGeneQueriesSpecialValueCorrect() {
        return structVarQueries.stream()
            .flatMap(queryList -> queryList.stream())
            .filter(structVarFilterQuery -> structVarFilterQuery.getGene1HugoGeneSymbol().getSpecialValue() != null
                && structVarFilterQuery.getGene2HugoGeneSymbol().getSpecialValue() != null
            )
            .noneMatch(structVarFilterQuery -> 
                structVarFilterQuery.getGene1HugoGeneSymbol().getSpecialValue() == 
                structVarFilterQuery.getGene2HugoGeneSymbol().getSpecialValue()
            );
    }

    @AssertTrue(message = "'geneId' field of gene1/gene2 StructVarGeneSubQueries cannot be both null.")
    private boolean isGeneQueriesGeneIdCorrect() {
        return structVarQueries.stream()
            .flatMap(queryList -> queryList.stream())
            .noneMatch(structVarFilterQuery -> 
                structVarFilterQuery.getGene1HugoGeneSymbol().getGeneId() == null &&
                structVarFilterQuery.getGene2HugoGeneSymbol().getGeneId() == null
            );
    }

    public Set<String> getMolecularProfileIds() {
        return molecularProfileIds;
    }

    public void setMolecularProfileIds(Set<String> molecularProfileIds) {
        this.molecularProfileIds = molecularProfileIds;
    }

    public List<List<GeneFilterQuery>> getGeneQueries() {
        return geneQueries;
    }

    public void setGeneQueries(List<List<GeneFilterQuery>> geneQueries) {
        this.geneQueries = geneQueries;
    }

    public List<List<StructVarFilterQuery>> getStructVarQueries() {
        return structVarQueries;
    }

    public void setStructVarQueries(List<List<StructVarFilterQuery>> structVarQueries) {
        this.structVarQueries = structVarQueries;
    }
}