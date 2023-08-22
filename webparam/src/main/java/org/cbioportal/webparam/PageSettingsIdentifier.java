package org.cbioportal.webparam;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;

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
