package org.cbioportal.application.file.model;

import java.util.SequencedMap;

/**
 * Interface representing a row in a table.
 * <p>
 * This interface provides methods to retrieve the row data.
 * </p>
 */
public interface TableRow {
    /**
     * Retrieves the row data as a sequenced map.
     * keys are the column names and values are the corresponding data.
     *
     * @return a sequenced map containing the row data.
     * Order of the map has to be the same as the order of the columns in the header.
     */
    SequencedMap<String, String> toRow();
}
