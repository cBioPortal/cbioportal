package org.cbioportal.legacy.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.AlterationDriverAnnotation;
import org.cbioportal.legacy.model.CustomDriverAnnotationReport;
import org.cbioportal.legacy.persistence.AlterationDriverAnnotationRepository;
import org.cbioportal.legacy.service.AlterationDriverAnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlterationDriverAnnotationServiceImpl implements AlterationDriverAnnotationService {

  @Autowired private AlterationDriverAnnotationRepository alterationDriverAnnotationRepository;
  private final Set<String> NO_DATA_TIERS = new HashSet<>(Arrays.asList(null, "", "NA"));

  public CustomDriverAnnotationReport getCustomDriverAnnotationProps(
      List<String> molecularProfileIds) {

    List<AlterationDriverAnnotation> rows =
        alterationDriverAnnotationRepository.getAlterationDriverAnnotations(molecularProfileIds);

    Set<String> tiers =
        rows.stream()
            .map(AlterationDriverAnnotation::getDriverTiersFilter)
            .filter(driverTiersFilter -> !NO_DATA_TIERS.contains(driverTiersFilter))
            .collect(Collectors.toCollection(TreeSet::new));
    boolean hasBinary =
        rows.stream()
            .anyMatch(
                d ->
                    "Putative_Driver".equals(d.getDriverFilter())
                        || "Putative_Passenger".equals(d.getDriverFilter()));

    return new CustomDriverAnnotationReport(hasBinary, tiers);
  }
}
