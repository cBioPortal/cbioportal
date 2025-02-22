package org.cbioportal.legacy.service;

import java.util.List;
import org.cbioportal.legacy.model.CustomDriverAnnotationReport;

public interface AlterationDriverAnnotationService {
  CustomDriverAnnotationReport getCustomDriverAnnotationProps(List<String> molecularProfileIds);
}
