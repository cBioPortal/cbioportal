package org.cbioportal.web.parameter.filter;

/**
 * A Filter determines whether a subject S belongs in a collection.
 * It uses information I to make this decision.
 * If filter evaluates to true, the subject is included.
 * @param <S> subject: the item to be filtered.
 * @param <I> information: the information needed to make the decision
 */
public interface Filter<S, I> {
    /**
     * Determines whether the subject should be included / excluded
     *
     * @param subject the item filter is to include / exclude
     * @param information the information filter needs to make this decision.
     *                    If the decision can be made without an additional
     *                    information other than the subject, this can be null.
     * @return true if the subject should be included, otherwise false
     */
    public boolean filter(S subject, I information);
}
