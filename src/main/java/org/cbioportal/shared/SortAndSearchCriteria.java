package org.cbioportal.shared;

public record SortAndSearchCriteria(String searchTerm, String sortField, String sortOrder) {
    boolean isSortable() {
        return (sortField != null && !sortField.isEmpty())
            && (sortOrder != null && !sortOrder.isEmpty());
    }

    boolean isSearchable() {
        return searchTerm != null && !searchTerm.isEmpty();
    }
}
