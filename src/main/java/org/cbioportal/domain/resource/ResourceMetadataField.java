package org.cbioportal.domain.resource;

/**
 * One field declaration from the {@code resource_definition.custom_metadata} contract.
 *
 * <p>The contract describes the shape of the instance values stored in {@code
 * resource_data.METADATA}: which keys exist, how they are typed, and how they should be presented.
 * Only the presentation subset is modelled here — {@code required}, {@code enum} and {@code format}
 * belong to import-time validation, and {@code renderAs} to cell rendering; neither is implemented
 * yet.
 *
 * <p>Every field except {@code key} is optional. A null means "not declared", which leaves the
 * corresponding decision to auto-detection rather than forcing a default.
 */
public record ResourceMetadataField(
    String key,
    String type,
    String label,
    String description,
    Boolean filterable,
    Boolean visibleByDefault) {}
