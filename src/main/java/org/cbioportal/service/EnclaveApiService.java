package org.cbioportal.service;

import org.cbioportal.model.enclave.*;

public interface EnclaveApiService {
    
    CohortInfo fetchCohortInfo(FilterParams params);
    
    TopMutations fetchTopMutations(FilterParams filters, int n);
}
