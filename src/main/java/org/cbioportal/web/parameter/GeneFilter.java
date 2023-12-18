package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.validation.constraints.AssertTrue;
import org.apache.commons.collections4.CollectionUtils;
import org.cbioportal.model.CNA;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class GeneFilter implements Serializable {

    private Set<String> molecularProfileIds;
    private List<List<String>> geneQueries;

    private final String GENE_QUERY_PATTERN = "^(\\w+)[\\s]*?(?:\\:(?:[\\s]*(?:(AMP)|(GAIN)|(DIPLOID)|(HETLOSS)|(HOMDEL))\\b)+)?$";

    public class SingleGeneQuery implements Serializable {
        private String hugoGeneSymbol;
        private List<CNA> alterations;

        public String getHugoGeneSymbol() {
            return hugoGeneSymbol;
        }

        public void setHugoGeneSymbol(String hugoGeneSymbol) {
            this.hugoGeneSymbol = hugoGeneSymbol;
        }

        public List<CNA> getAlterations() {
            return alterations;
        }

        public void setAlterations(List<CNA> alterations) {
            this.alterations = alterations;
        }
    }

    @AssertTrue
    private boolean isValid() {
        if (!CollectionUtils.isEmpty(geneQueries) && !CollectionUtils.isEmpty(molecularProfileIds)) {
            return geneQueries.stream().flatMap(geneQuery -> geneQuery.stream().map(query -> {
                Pattern pattern = Pattern.compile(GENE_QUERY_PATTERN);
                Matcher matcher = pattern.matcher(query.trim());
                return matcher.matches();
            })).reduce(Boolean.TRUE, Boolean::logicalAnd);
        }
        return false;
    }

    public Set<String> getMolecularProfileIds() {
        return molecularProfileIds;
    }

    public void setMolecularProfileIds(Set<String> molecularProfileIds) {
        this.molecularProfileIds = molecularProfileIds;
    }

    public List<List<String>> getGeneQueries() {
        return geneQueries;
    }

    public void setGeneQueries(List<List<String>> geneQueries) {
        this.geneQueries = geneQueries;
    }

    @JsonIgnore
    public List<List<SingleGeneQuery>> getSingleGeneQueries() {

        return geneQueries.stream().map(geneQuery -> {

            List<SingleGeneQuery> singleGeneQueries = new ArrayList<SingleGeneQuery>();

            geneQuery.stream().forEach(query -> {
                Pattern pattern = Pattern.compile(GENE_QUERY_PATTERN);
                Matcher matcher = pattern.matcher(query.trim());

                if (matcher.find()) {

                    String hugoGeneSymbol = matcher.group(1);
                    Set<CNA> alterations = new HashSet<>();
                    for (int count = 2; count <= matcher.groupCount(); count++) {
                        if (matcher.group(count) != null) {
                            alterations.add(CNA.valueOf(matcher.group(count)));
                        }
                    }
                    SingleGeneQuery singleGeneQuery = new SingleGeneQuery();
                    singleGeneQuery.setHugoGeneSymbol(hugoGeneSymbol);
                    singleGeneQuery.setAlterations(new ArrayList<>(alterations));
                    singleGeneQueries.add(singleGeneQuery);
                }
            });

            return singleGeneQueries;
        }).collect(Collectors.toList());
    }

}