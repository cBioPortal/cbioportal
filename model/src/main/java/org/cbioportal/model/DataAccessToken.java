package org.cbioportal.model;

import java.io.Serializable;
import java.util.Date;

public class DataAccessToken implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String token;
    private String username;
    private Date expiration;
    private Date creation;

    public DataAccessToken() {}    

    public DataAccessToken(String token) {
        this.token = token;
    }    

    public DataAccessToken(String token, String username, Date expiration, Date creation) {
        this.token = token;
        this.username = username;
        this.expiration = expiration;
        this.creation = creation;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }
 
    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }

    public boolean hasEarlierExpirationThanToken(DataAccessToken dataAccessToken) {
        return (this.expiration != null && this.expiration.before(dataAccessToken.getExpiration()));
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder().append("token: ").append(token).append("\n");
        if (creation != null) {
            b.append("creation_date: ").append(creation.toString()).append("\n");
        }
        if (expiration != null) {
            b.append("expiration_date: ").append(expiration.toString()).append("\n");
        }
        return b.toString();
    }
}
