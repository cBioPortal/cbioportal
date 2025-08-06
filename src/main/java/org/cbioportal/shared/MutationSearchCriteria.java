package org.cbioportal.shared;

import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.sort.MutationSortBy;

public record MutationSearchCriteria(Projection projection, Integer pageSize, Integer pageNumber, String sortBy, Direction direction ) {
}
