package org.cbioportal.service;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.web.parameter.*;

import java.util.List;

public interface SummaryDataService {
    
    boolean supportsStudies(List<String> studyIds);
    
    List<ClinicalAttribute> fetchClinicalAttributes(
        List<String> studyIds,
        Projection projection
    );

    List<ClinicalDataCountItem> fetchClinicalDataCounts(
        ClinicalDataCountFilter filter
    );
    
    List<ClinicalDataBin> fetchClinicalDataBinCounts(
        ClinicalDataBinCountFilter filter,
        DataBinMethod dataBinMethod
    );
}
