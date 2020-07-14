package org.cbioportal.model.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Used to distinguish selecting all items (no filtering) from select none (filter all).
 * Basically it is Iterable that implements the Null Object pattern.
 * @param <T>
 */
public class Select<T> implements Iterable<T> {

    private final Iterable<T> iterable;
    private boolean hasAllExternal = false;

    private static final Select<?> ALL = new Select<>(null);
    @SuppressWarnings("unchecked")
    public static <T> Select<T> all() {
        return (Select<T>) ALL;
    }
    private static final Select<?> NONE = new Select<>(Collections.emptyList());
    @SuppressWarnings("unchecked")
    public static <T> Select<T> none() {
        return (Select<T>) NONE;
    }

    public static <T> Select<T> byValues(Iterable<T> iterable) {
        return new Select<>(iterable);
    }

    public static <T> Select<T> byValues(Stream<T> stream) {
        return stream == null ? all() : new Select<>(stream.collect(Collectors.toList()));
    }

    public <R> Select<R> map(Function<T, R> function) {
        if (hasAll()) {
            return all();
        }
        if (hasNone()) {
            return none();
        }
        return Select.byValues(StreamSupport.stream(this.spliterator(), false).map(function));
    }

    private Select(Iterable<T> iterable) {
        this.iterable = iterable;
    }

    public final boolean hasAll() {
        return null == iterable || hasAllExternal;
    }

    public final boolean hasNone() {
        return !hasAllExternal && !hasAll() && !hasValues();
    }

    public final boolean hasValues() {
        return null != iterable && iterable.iterator().hasNext();
    }

    public void hasAll(boolean value) {
        this.hasAllExternal = value;
    }

    @Override
    public Iterator<T> iterator() {
        if (null == iterable) {
            throw new UnsupportedOperationException("Iteration over Select.ALL is not defined.");
        }
        return iterable.iterator();
    }
}
