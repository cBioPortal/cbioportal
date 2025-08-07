package org.cbioportal.shared;

import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;

public record MutationSearchCriteria(Projection projection, Integer pageSize, Integer pageNumber, String sortBy, Direction direction ) {
}
