package org.cbioportal.shared;

import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.shared.enums.ProjectionType;

public record MutationSearchCriteria(ProjectionType projection,
                                     Integer pageSize,
                                     Integer pageNumber,
                                     String sortBy,
                                     Direction direction ) {
}
