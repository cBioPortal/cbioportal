package org.cbioportal.web.parameter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public class MutationDataFilter implements Serializable {
    private String hugoGeneSymbol;
    private String profileType;
    @NotNull
    private MutationOption categorization; // filter by either having mutations or by mutation types

    private List<List<DataFilterValue>> values;

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }

    public MutationOption getCategorization() {
        return categorization;
    }

    public void setCategorization(MutationOption categorization) {
        this.categorization = categorization;
    }

    public List<List<DataFilterValue>> getValues() {
        return values;
    }

    public void setValues(List<List<DataFilterValue>> values) {
        this.values = values;
    }
}

