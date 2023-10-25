package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PageSettingsIdentifier implements Serializable {

    @NotNull
    private SessionPage page;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    // type of it should be similar to PageSettingsData -> origin
    Set<String> origin;

    public SessionPage getPage() {
        return page;
    }

    public void setPage(SessionPage page) {
        this.page = page;
    }

    public Set<String> getOrigin() {
        return origin;
    }

    public void setOrigin(Set<String> origin) {
        this.origin = origin;
    }

}
