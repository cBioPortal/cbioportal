package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.service.FederatedService;
import org.cbioportal.service.exception.FederationException;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.cbioportal.web.parameter.ClinicalDataBinCountFilter;
import org.cbioportal.web.parameter.ClinicalDataCountFilter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(name = "fedapi.mode", havingValue = "NONE", matchIfMissing = true)
@Primary // default implementation
public class NullFederatedService implements FederatedService {
    @Override
    public List<ClinicalAttribute> fetchClinicalAttributes() throws FederationException {
        throw new FederationException("Federation is disabled");
    }

    @Override
    public List<ClinicalDataCountItem> fetchClinicalDataCounts(ClinicalDataCountFilter filter) throws FederationException {
        throw new FederationException("Federation is disabled");
    }

    @Override
    public List<ClinicalDataBin> fetchClinicalDataBinCounts(ClinicalDataBinCountFilter filter) throws FederationException {
        throw new FederationException("Federation is disabled");
    }
}
