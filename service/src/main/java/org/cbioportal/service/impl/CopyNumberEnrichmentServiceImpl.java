package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CopyNumberSampleCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.service.CopyNumberEnrichmentService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CopyNumberEnrichmentServiceImpl implements CopyNumberEnrichmentService {

    @Autowired
    private DiscreteCopyNumberService discreteCopyNumberService;
    @Autowired
    private AlterationEnrichmentUtil alterationEnrichmentUtil;
    
    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<AlterationEnrichment> getCopyNumberEnrichments(String geneticProfileId, List<String> alteredSampleIds,
                                                               List<String> unalteredSampleIds,
                                                               List<Integer> entrezGeneIds, 
                                                               List<Integer> alterationTypes)
        throws GeneticProfileNotFoundException {

        List<String> allSampleIds = new ArrayList<>(alteredSampleIds);
        allSampleIds.addAll(unalteredSampleIds);
        List<CopyNumberSampleCountByGene> copyNumberSampleCountByGeneList = discreteCopyNumberService
            .getSampleCountByGeneAndAlterationAndSampleIds(geneticProfileId, allSampleIds, null, null);
        copyNumberSampleCountByGeneList.removeIf(m -> entrezGeneIds.contains(m.getEntrezGeneId()) || 
            !alterationTypes.contains(m.getAlteration()));

        List<DiscreteCopyNumberData> discreteCopyNumberDataList = discreteCopyNumberService
            .fetchDiscreteCopyNumbersInGeneticProfile(geneticProfileId, alteredSampleIds, null, alterationTypes, "ID");

        return alterationEnrichmentUtil.createAlterationEnrichments(alteredSampleIds.size(), unalteredSampleIds.size(),
            copyNumberSampleCountByGeneList, discreteCopyNumberDataList);
    }
}