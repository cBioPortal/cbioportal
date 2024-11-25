package org.cbioportal.service;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.service.exception.FederationException;
import org.cbioportal.web.parameter.*;

import java.util.List;

public interface FederatedViewService {
    
    List<ClinicalAttribute> fetchClinicalAttributes(
        List<String> studyIds,
        String projection
    ) throws FederationException;

    List<ClinicalDataCountItem> fetchClinicalDataCounts(
        ClinicalDataCountFilter filter
    ) throws FederationException;
    
    List<ClinicalDataBin> fetchClinicalDataBinCounts(
        ClinicalDataBinCountFilter filter,
        DataBinMethod dataBinMethod
    ) throws FederationException;
}
