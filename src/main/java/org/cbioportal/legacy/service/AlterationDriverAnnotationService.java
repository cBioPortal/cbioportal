package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.CustomDriverAnnotationReport;

import java.util.List;

public interface AlterationDriverAnnotationService {
    CustomDriverAnnotationReport getCustomDriverAnnotationProps(List<String> molecularProfileIds);
}