package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.ClickHouseConstraintChecker;
import org.mskcc.cbio.portal.dao.ClickHouseConstraintChecker.ForeignKeyViolation;
import org.mskcc.cbio.portal.dao.ClickHouseConstraintChecker.UniqueKeyViolation;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Checks ClickHouse foreign-key and unique-key constraints for the portal schema.
 */
public class CheckClickHouseConstraints extends ConsoleRunnable {

    private static final int SAMPLE_VALUE_LIMIT = 5;

    private record ForeignKeyGroupKey(
            String refererTable,
            String refererColumns,
            String referredTable,
            String referredColumns
    ) {
    }

    private static final class ForeignKeyGroup {
        private int totalCount;
        private final List<String> sampleValues = new ArrayList<>();

        private void add(String refererValue) {
            totalCount++;
            if (sampleValues.size() < SAMPLE_VALUE_LIMIT && !sampleValues.contains(refererValue)) {
                sampleValues.add(refererValue);
            }
        }
    }

    private record UniqueKeyGroupKey(
            String table,
            String columns
    ) {
    }

    private static final class UniqueKeyGroup {
        private int totalKeyValues;
        private long totalRowCount;
        private final List<String> sampleValues = new ArrayList<>();

        private void add(UniqueKeyViolation violation) {
            totalKeyValues++;
            totalRowCount += violation.duplicateCount();
            String sample = formatUniqueSample(violation);
            if (sampleValues.size() < SAMPLE_VALUE_LIMIT && !sampleValues.contains(sample)) {
                sampleValues.add(sample);
            }
        }
    }

    @Override
    public void run() {
        ProgressMonitor.setCurrentMessage("Checking ClickHouse constraints...");
        try {
            List<ForeignKeyViolation> foreignKeyViolations = ClickHouseConstraintChecker.findForeignKeyViolations();
            List<UniqueKeyViolation> uniqueKeyViolations = ClickHouseConstraintChecker.findUniqueKeyViolations();

            if (foreignKeyViolations.isEmpty() && uniqueKeyViolations.isEmpty()) {
                ProgressMonitor.setCurrentMessage("No ClickHouse constraint violations found.");
            } else {
                Map<ForeignKeyGroupKey, ForeignKeyGroup> fkGroups = groupForeignKeyViolations(foreignKeyViolations);
                Map<UniqueKeyGroupKey, UniqueKeyGroup> uniqueGroups = groupUniqueKeyViolations(uniqueKeyViolations);
                ProgressMonitor.setCurrentMessage(String.format(
                        Locale.ROOT,
                        "ClickHouse constraint violations found: %d foreign-key rows across %d relationships; %d unique-key groups across %d constraints.",
                        foreignKeyViolations.size(),
                        fkGroups.size(),
                        uniqueKeyViolations.size(),
                        uniqueGroups.size()
                ));

                for (var entry : fkGroups.entrySet()) {
                    ProgressMonitor.logWarning(formatForeignKeyGroup(entry.getKey(), entry.getValue()));
                }
                for (var entry : uniqueGroups.entrySet()) {
                    ProgressMonitor.logWarning(formatUniqueKeyGroup(entry.getKey(), entry.getValue()));
                }
                ConsoleUtil.showWarnings();
                System.exit(1);
            }
        } catch (DaoException e) {
            throw new RuntimeException("Failed to check ClickHouse constraints.", e);
        }
        ProgressMonitor.setCurrentMessage("Done.");
    }

    private static Map<ForeignKeyGroupKey, ForeignKeyGroup> groupForeignKeyViolations(List<ForeignKeyViolation> violations) {
        Map<ForeignKeyGroupKey, ForeignKeyGroup> grouped = new LinkedHashMap<>();
        for (ForeignKeyViolation violation : violations) {
            ForeignKeyGroupKey key = new ForeignKeyGroupKey(
                    violation.refererTable(),
                    violation.refererColumns(),
                    violation.referredTable(),
                    violation.referredColumns()
            );
            grouped.computeIfAbsent(key, unused -> new ForeignKeyGroup()).add(violation.refererValues());
        }
        return grouped;
    }

    private static Map<UniqueKeyGroupKey, UniqueKeyGroup> groupUniqueKeyViolations(List<UniqueKeyViolation> violations) {
        Map<UniqueKeyGroupKey, UniqueKeyGroup> grouped = new LinkedHashMap<>();
        for (UniqueKeyViolation violation : violations) {
            UniqueKeyGroupKey key = new UniqueKeyGroupKey(
                    violation.table(),
                    violation.columns()
            );
            grouped.computeIfAbsent(key, unused -> new UniqueKeyGroup()).add(violation);
        }
        return grouped;
    }

    private static String formatForeignKeyGroup(ForeignKeyGroupKey key, ForeignKeyGroup group) {
        String sampleText = group.sampleValues.isEmpty()
                ? "No sample values captured."
                : String.join(", ", group.sampleValues);
        int remainingCount = Math.max(0, group.totalCount - group.sampleValues.size());
        String remainingText = remainingCount > 0
                ? " (+" + remainingCount + " more)"
                : "";
        return String.format(
                Locale.ROOT,
                "Table %s (columns: %s) has %d values that do not exist in table %s (columns: %s). Examples: %s%s.",
                key.refererTable(),
                key.refererColumns(),
                group.totalCount,
                key.referredTable(),
                key.referredColumns(),
                sampleText,
                remainingText
        );
    }

    private static String formatUniqueSample(UniqueKeyViolation violation) {
        return violation.keyValues() + " (count=" + violation.duplicateCount() + ")";
    }

    private static String formatUniqueKeyGroup(UniqueKeyGroupKey key, UniqueKeyGroup group) {
        String sampleText = group.sampleValues.isEmpty()
                ? "No sample values captured."
                : String.join(", ", group.sampleValues);
        int remainingCount = Math.max(0, group.totalKeyValues - group.sampleValues.size());
        String remainingText = remainingCount > 0
                ? " (+" + remainingCount + " more)"
                : "";
        return String.format(
                Locale.ROOT,
                "Table %s (columns: %s) has %d duplicated keys across %d rows. Examples: %s%s.",
                key.table(),
                key.columns(),
                group.totalKeyValues,
                group.totalRowCount,
                sampleText,
                remainingText
        );
    }

    public CheckClickHouseConstraints(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        ConsoleRunnable runner = new CheckClickHouseConstraints(args);
        runner.runInConsole();
    }
}
