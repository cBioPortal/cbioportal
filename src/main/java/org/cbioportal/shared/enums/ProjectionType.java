package org.cbioportal.shared.enums;
/**
 * Enum representing different levels of data projection for API responses.
 * <p>
 * This enum is used in the <b>domain layer</b> to control the amount of data
 * included in API responses, ensuring that only the necessary fields are
 * returned based on the specific use case. By limiting the data returned,
 * this enum helps improve performance, reduce network overhead, and simplify
 * client-side processing.
 * </p>
 * <p>
 * Each projection level corresponds to a specific set of fields or data
 * representation, allowing the domain layer to tailor responses to the
 * requirements of the client or the context of the request.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * {@code
 * public ResponseEntity<EntityDto> getEntity(@RequestParam Projection projection) {
 *     Entity entity = entityService.getEntity(projection);
 *     EntityDto dto = entityMapper.toDto(entity, projection);
 *     return ResponseEntity.ok(dto);
 * }
 * }
 * </pre>
 * </p>
 */
public enum ProjectionType {

    /**
     * <b>ID Projection</b> - Includes only the essential identifier fields of the entity.
     * <p>
     * This projection is suitable for scenarios where only the unique identifier
     * of the entity is required, such as when referencing the entity in another
     * context or performing lightweight operations.
     * </p>
     * <p>
     * <b>Example Use Case:</b> Fetching entity IDs for batch processing or
     * creating relationships between entities.
     * </p>
     */
    ID,

    /**
     * <b>SUMMARY Projection</b> - Includes a limited set of fields to provide
     * a high-level overview of the entity.
     * <p>
     * This projection is useful for lightweight data representations, such as
     * displaying a list of entities in a UI or providing a quick summary without
     * the full details.
     * </p>
     * <p>
     * <b>Example Use Case:</b> Displaying a list of entities in a dropdown or
     * search results.
     * </p>
     */
    SUMMARY,

    /**
     * <b>DETAILED Projection</b> - Includes all available fields of the entity.
     * <p>
     * This projection provides a complete representation of the entity, including
     * all attributes and relationships. It is best suited for scenarios where
     * the client requires a full view of the entity.
     * </p>
     * <p>
     * <b>Example Use Case:</b> Viewing the full details of an entity in a
     * detailed view or edit form.
     * </p>
     */
    DETAILED,

    /**
     * <b>META Projection</b> - Focuses on metadata, such as counts, statistics,
     * or aggregated information about the underlying data.
     * <p>
     * This projection is useful for scenarios where the client needs summary
     * information rather than individual records, such as displaying totals,
     * averages, or other aggregated data.
     * </p>
     * <p>
     * <b>Example Use Case:</b> Displaying dashboard metrics or summary reports.
     * </p>
     */
    META
}

