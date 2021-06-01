package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationDriverAnnotation;
import org.cbioportal.model.CustomDriverAnnotationReport;
import org.cbioportal.persistence.AlterationDriverAnnotationRepository;
import org.cbioportal.service.AlterationDriverAnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlterationDriverAnnotationServiceImpl implements AlterationDriverAnnotationService {
    
    @Autowired
    private AlterationDriverAnnotationRepository alterationDriverAnnotationRepository;
    private final Set<String> NO_DATA_TIERS = new HashSet<>(Arrays.asList(null, "", "NA"));
    
    public CustomDriverAnnotationReport getCustomDriverAnnotationProps(List<String> molecularProfileIds) {
        
        List<AlterationDriverAnnotation> rows = alterationDriverAnnotationRepository
            .getAlterationDriverAnnotations(molecularProfileIds);
        
        Set<String> tiers = rows.stream()
            .map(AlterationDriverAnnotation::getDriverTiersFilter)
            .filter(driverTiersFilter -> !NO_DATA_TIERS.contains(driverTiersFilter))
            .collect(Collectors.toCollection(TreeSet::new));
        boolean hasBinary = rows.stream().anyMatch(d ->
            "Putative_Driver".equals(d.getDriverFilter()) ||
                "Putative_Passenger".equals(d.getDriverFilter()));
        
        return new CustomDriverAnnotationReport(hasBinary, tiers);
    }
}