/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.importer.dmp.model;

import com.fasterxml.jackson.annotation.*; 

@JsonIgnoreProperties(ignoreUnknown = true)
public class DMPsession {

        private String disclaimer;
        private String session_id;
        private String time_created;
        private String time_expired;

        public DMPsession(String _session_id, String _time_created, String _time_expired) {
            session_id = _session_id;
            time_created = _time_created;
            time_expired = _time_expired;
        }

        @JsonProperty("session_id")
        public String getSessionId() {
            return session_id;
        }

        @JsonProperty("time_created")
        public String getTimeCreated() {
            return time_created;
        }

        @JsonProperty("time_expired")
        public String getTimeExpired() {
            return time_expired;
        }

        @JsonProperty("disclaimer")
        public String getDisclaimer() {
            return disclaimer;
        }

}