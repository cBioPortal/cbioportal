package org.cbioportal.service;

import java.util.List;
import org.cbioportal.model.CustomDriverAnnotationReport;

public interface AlterationDriverAnnotationService {
  CustomDriverAnnotationReport getCustomDriverAnnotationProps(List<String> molecularProfileIds);
}
