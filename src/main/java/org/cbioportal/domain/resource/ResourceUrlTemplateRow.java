package org.cbioportal.domain.resource;

/** One study's {@code resource_definition.url_template} for the resource a request names. */
public record ResourceUrlTemplateRow(String studyId, String urlTemplate) {}
