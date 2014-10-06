package org.mskcc.cbio.importer.dmp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// @JsonIgnoreProperties(ignoreUnknown = true)
// public class DMPsession {

//     private String disclaimer;
//     private String time_created;
//     private String session_id;
//     private String time_expired;

//     public String getDisclaimer() {
//         return disclaimer;
//     }

//     public String getSessionId() {
//         return session_id;
//     }

//     public String getTimeCreated() {
//         return time_created;
//     }

//     public String getTimeExpired() {
//         return time_expired;
//     }

// }

@JsonIgnoreProperties(ignoreUnknown = true)
public class DMPsession {

    private String name;
    private String about;
    private String phone;
    private String website;
    private boolean can_post;
    private String description;

    public String getDescription() {
        return description;
    }

    public boolean getCanPost() {
        return can_post;
    }

    public String getName() {
        return name;
    }

    public String getAbout() {
        return about;
    }

    public String getPhone() {
        return phone;
    }

    public String getWebsite() {
        return website;
    }

}