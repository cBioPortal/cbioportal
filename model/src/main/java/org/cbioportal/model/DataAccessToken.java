package org.cbioportal.model;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

public class DataAccessToken implements Serializable {

    private String token;

    public DataAccessToken(String token) {
            this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
            this.token = token;
    }

}
