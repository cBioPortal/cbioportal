package org.cbioportal.web.util;

import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.web.parameter.CNAFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudyViewCNAFilterApplier {
    DiscreteCopyNumberService copyNumberService;

    @Autowired
    public StudyViewCNAFilterApplier(DiscreteCopyNumberService copyNumberService) {
        this.copyNumberService = copyNumberService;
    }
    
    public List<SampleIdentifier> applyFilters(List<SampleIdentifier> unfilitered, List<CNAFilter> filters) {
        return unfilitered;
    }
    
    
}
