package org.cbioportal.infrastructure.requestlog;

/**
 * A single captured request header, stored as a {@code {name, value}} sub-document.
 *
 * <p>Headers are kept as a list of these rather than a {@code Map} so that attacker-supplied header
 * names (which can legally contain characters such as {@code $} or {@code .}) never become MongoDB
 * field keys, where they would be rejected or interpreted as operators/paths.
 */
public record HttpHeader(String name, String value) {}
