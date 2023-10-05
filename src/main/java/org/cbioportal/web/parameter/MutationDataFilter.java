package org.cbioportal.web.parameter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public class MutationDataFilter extends GenomicDataFilter {
    private MutationOption categorization;

    public MutationOption getCategorization() { return categorization; }

    public void setCategorization(MutationOption categorization) {
        this.categorization = categorization;
    }
}

