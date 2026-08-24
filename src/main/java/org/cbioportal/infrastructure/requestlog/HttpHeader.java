package org.cbioportal.infrastructure.requestlog;

/**
 * A single captured request header. The full set of headers for a request is serialized to JSON as
 * an array of these {@code {name, value}} objects and stored in a single column.
 *
 * <p>Headers are kept as a list of name/value pairs rather than a map so that duplicate header
 * names are preserved and attacker-supplied header names never need to become structural keys.
 */
public record HttpHeader(String name, String value) {}
