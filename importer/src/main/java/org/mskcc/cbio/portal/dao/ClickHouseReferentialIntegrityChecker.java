package org.mskcc.cbio.portal.dao;

import java.util.List;

/**
 * @deprecated Use {@link ClickHouseConstraintChecker} for ClickHouse constraint checks.
 */
@Deprecated
public class ClickHouseReferentialIntegrityChecker {

    public static final String REFERER_TABLE = ClickHouseConstraintChecker.REFERER_TABLE;
    public static final String REFERER_COLUMNS = ClickHouseConstraintChecker.REFERER_COLUMNS;
    public static final String REFERER_VALUES = ClickHouseConstraintChecker.REFERER_VALUES;
    public static final String REFERRED_TABLE = ClickHouseConstraintChecker.REFERRED_TABLE;
    public static final String REFERRED_COLUMNS = ClickHouseConstraintChecker.REFERRED_COLUMNS;

    private ClickHouseReferentialIntegrityChecker() {
    }

    public record ReferenceViolation(
            String refererTable,
            String refererColumns,
            String refererValues,
            String referredTable,
            String referredColumns
    ) {
    }

    public static List<ReferenceViolation> findReferenceViolations() throws DaoException {
        return ClickHouseConstraintChecker.findForeignKeyViolations().stream()
                .map(v -> new ReferenceViolation(
                        v.refererTable(),
                        v.refererColumns(),
                        v.refererValues(),
                        v.referredTable(),
                        v.referredColumns()
                ))
                .toList();
    }
}
