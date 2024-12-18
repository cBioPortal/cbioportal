package org.cbioportal.persistence.fedapi;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.web.parameter.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FederatedDataSource {
    
    String getName();
    
    String getBaseUrl();
    
    CompletableFuture<List<ClinicalAttribute>> fetchClinicalAttributes();

    // ClinicalDataCountFilter - controls which attributes to fetch for, plus what filters to apply to the source cohort
    CompletableFuture<List<ClinicalDataCountItem>> fetchClinicalDataCounts(ClinicalDataCountFilter filter);

    // ClinicalDataBinCountFilter - controls which attributes to fetch for, plus what filters to apply to the source cohort
    CompletableFuture<List<ClinicalDataBin>> fetchClinicalDataBinCounts(ClinicalDataBinCountFilter filter);
}
