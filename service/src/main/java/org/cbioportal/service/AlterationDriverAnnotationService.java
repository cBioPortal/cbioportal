package org.cbioportal.service;

import org.cbioportal.model.CustomDriverAnnotationReport;

import java.util.List;

public interface AlterationDriverAnnotationService {
    CustomDriverAnnotationReport getCustomDriverAnnotationProps(List<String> molecularProfileIds);
}