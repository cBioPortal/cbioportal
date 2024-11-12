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
    
    List<String> getStudyIds();
    
    List<String> getSupportedEndpoints();
    
    CompletableFuture<List<ClinicalAttribute>> fetchClinicalAttributes(List<String> studyIds, Projection projection);

    CompletableFuture<List<ClinicalDataCountItem>> fetchClinicalDataCounts(ClinicalDataCountFilter filter);

    CompletableFuture<List<ClinicalDataBin>> fetchClinicalDataBinCounts(ClinicalDataBinCountFilter filter, DataBinMethod dataBinMethod);
}
