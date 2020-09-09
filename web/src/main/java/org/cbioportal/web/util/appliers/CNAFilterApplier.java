package org.cbioportal.web.util.appliers;

import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.web.parameter.CNAFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CNAFilterApplier {
    @Autowired
    DiscreteCopyNumberService copyNumberService;
    
    public List<SampleIdentifier> applyFilters(List<SampleIdentifier> unfilitered, List<CNAFilter> filters) {
        return unfilitered;
    }
    
    
}
