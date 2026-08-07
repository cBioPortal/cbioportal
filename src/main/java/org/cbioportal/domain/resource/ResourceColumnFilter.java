package org.cbioportal.domain.resource;

import java.util.List;

public record ResourceColumnFilter(String columnId, String operator, List<String> values) {}
