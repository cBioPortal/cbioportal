package org.cbioportal.model;

import jakarta.validation.constraints.AssertTrue;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class StudyViewStructuralVariantFilter implements Serializable {

    private Set<String> molecularProfileIds;
    private List<List<StructuralVariantFilterQuery>> structVarQueries;

    @AssertTrue(message = "'specialValue' field of gene1/gene2 StructVarGeneSubQueries cannot be both ANY_GENE or NO_GENE.")
    private boolean isGeneQueriesSpecialValueCorrect() {
        return structVarQueries.stream()
            .flatMap(queryList -> queryList.stream())
            .filter(structVarFilterQuery -> structVarFilterQuery.getGene1Query().getSpecialValue() != null
                && structVarFilterQuery.getGene2Query().getSpecialValue() != null
            )
            .noneMatch(structVarFilterQuery -> 
                structVarFilterQuery.getGene1Query().getSpecialValue() == 
                structVarFilterQuery.getGene2Query().getSpecialValue()
            );
    }

    @AssertTrue(message = "'geneId' field of gene1/gene2 StructVarGeneSubQueries cannot be both null.")
    private boolean isGeneQueriesGeneIdCorrect() {
        return structVarQueries.stream()
            .flatMap(queryList -> queryList.stream())
            .noneMatch(structVarFilterQuery -> 
                structVarFilterQuery.getGene1Query().getHugoSymbol() == null &&
                structVarFilterQuery.getGene2Query().getHugoSymbol() == null
            );
    }
                
    @AssertTrue(message = "'geneId' field of gene1/gene2 StructVarGeneSubQueries cannot be null when no specialValue set.")
    private boolean isGeneQueriesHasGeneIdWhenSpecialValueNull() {
        return structVarQueries.stream()
            .flatMap(queryList -> queryList.stream())
            .noneMatch(structVarFilterQuery ->
                (structVarFilterQuery.getGene1Query().getHugoSymbol() == null &&
                structVarFilterQuery.getGene1Query().getSpecialValue() == null)
                || (structVarFilterQuery.getGene2Query().getHugoSymbol() == null &&
                    structVarFilterQuery.getGene2Query().getSpecialValue() == null)
            );
    }

    public Set<String> getMolecularProfileIds() {
        return molecularProfileIds;
    }

    public void setMolecularProfileIds(Set<String> molecularProfileIds) {
        this.molecularProfileIds = molecularProfileIds;
    }

    public List<List<StructuralVariantFilterQuery>> getStructVarQueries() {
        return structVarQueries;
    }

    public void setStructVarQueries(List<List<StructuralVariantFilterQuery>> structVarQueries) {
        this.structVarQueries = structVarQueries;
    }
}