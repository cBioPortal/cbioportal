package org.cbioportal.web.parameter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public class MutationDataFilter extends GenomicDataFilter {
    private String categorization;

    public String getCategorization() { return categorization; }

    public void setCategorization(String categorization) {
        this.categorization = categorization;
    }
}

