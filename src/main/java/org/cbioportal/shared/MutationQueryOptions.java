package org.cbioportal.shared;

import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.shared.enums.ProjectionType;

public record MutationQueryOptions(ProjectionType projection,
                                   Integer pageSize,
                                   Integer pageNumber,
                                   String sortBy,
                                   Direction direction ) {
}
