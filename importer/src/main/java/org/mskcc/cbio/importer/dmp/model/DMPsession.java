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

        private String session_id;
        private String time_created;
        private String time_expired;

        public DMPsession(String _session_id, String _time_created, String _time_expired) {
            session_id = _session_id;
            time_created = _time_created;
            time_expired = _time_expired;
        }

        public String getSessionId() {
            return session_id;
        }

        public String getTimeCreated() {
            return time_created;
        }

        public String getTimeExpired() {
            return time_expired;
        }

}