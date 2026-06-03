package org.cbioportal.domain.resource;

import java.util.List;

public record ResourceTabsRequest(List<String> studyIds, List<String> patientIds, List<String> sampleIds) {}
