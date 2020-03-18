package org.cbioportal.web.parameter.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;

/**
 * Given a collection of filters F[], and some background information I,
 * BooleanOperatorJoinedFilters will determine whether subjects S
 * should be included / excluded by anding / oring the results of applying F[] to S.
 *
 * @param <S> subject: the subject being included / excluded
 * @param <I> info: the extra information needed to filter the subject
 * @param <F> filter: the filters being used to filter the subject
 */
public class BooleanOperatorJoinedFilters<S, I, F extends Filter<S, I>> implements Filter<S, I> {

    public enum BooleanOperator {
        And, Or, Unknown
    }

    private Collection<F> filters;
    private BooleanOperator operator;

    public boolean filter(S subject, I info) {
        if (isEmpty()) {
            return true;
        }
        // this switch is a little gross. I found that this was the cleanest way to support
        // null values without having force me to make an unreachable switch case or return statement
        switch (operator == null ? BooleanOperator.Unknown : operator) {
            case Or:
                return filters.stream().anyMatch(f -> f.filter(subject, info));
            case And:
                return filters.stream().allMatch(f -> f.filter(subject, info));
            default:
                return false;
        }
    }

    public int size() {
        return filters.size();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return size() == 0;
    }

    public Collection<F> getFilters() {
        return filters;
    }

    public void setFilters(Collection<F> filters) {
        this.filters = filters;
    }

    public BooleanOperator getOperator() {
        return operator;
    }

    public void setOperator(BooleanOperator operator) {
        this.operator = operator;
    }
}
