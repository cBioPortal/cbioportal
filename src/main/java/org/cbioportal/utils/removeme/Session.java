package org.cbioportal.utils.removeme;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.bson.Document;
import org.springframework.data.annotation.Id;
import org.springframework.util.DigestUtils;

// TODO this class was taken from session service. The session service dependency had to be dropped
// since it forced cbioportal into autoconfiguration of mongoDB connections.
// When session service is updated reinstate the correct session service dependency.

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Session {

    public enum SessionType {
        main_session,
        virtual_study,
        group,
        comparison_session,
        settings,
        custom_data,
        genomic_chart,
        custom_gene_list
    }
    
    @Id
    private String id;
    @NotNull
    private String checksum;
    @NotNull
    private Object data;
    @NotNull
    @Size(min=3, message="source has a minimum length of 3")
    private String source;
    @NotNull
    private SessionType type;


    @JsonView(Session.Views.IdOnly.class)
    public String getId() {
        return id;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setData(Object data) {
        if(data instanceof String) {
            this.data = Document.parse((String)data);
        } else {
            this.data = data;
        }
        this.checksum = DigestUtils.md5DigestAsHex(this.data.toString().getBytes());
    }

    @JsonView(Session.Views.Full.class)
    public Object getData() {
        return data;
    }

    public void setType(SessionType type) {
        this.type = type;
    }

    @JsonView(org.cbioportal.utils.removeme.Session.Views.Full.class)
    public SessionType getType() {
        return type;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @JsonView(Session.Views.Full.class)
    public String getSource() {
        return source;
    }

    public static final class Views {
        // show only id
        public interface IdOnly {}

        // show all data
        public interface Full extends Session.Views.IdOnly {}
    }
}
