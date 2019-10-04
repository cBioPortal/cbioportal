package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PageSettingsIdentifier implements Serializable {

    @NotNull
    private SessionPage page;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    List<String> origin;

    public SessionPage getPage() {
        return page;
    }

    public void setPage(SessionPage page) {
        this.page = page;
    }

    public List<String> getOrigin() {
        return origin;
    }

    public void setOrigin(List<String> origin) {
        this.origin = origin;
    }

}
