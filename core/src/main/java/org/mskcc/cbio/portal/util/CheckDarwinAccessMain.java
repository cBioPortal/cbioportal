/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.util;

//import org.mskcc.cbio.portal.model.DarwinAccess;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;

import org.apache.commons.cli.*;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 *
 * @author ochoaa
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
"darwinAuthResponse",
"p_userName",
"p_dmp_pid"
})
public class CheckDarwinAccessMain {
    
    public static class CheckDarwinAccess {
        private static String DARWIN_AUTH_URL;
        private static String DARWIN_RESPONSE_URL;
        private static String[] DARWIN_AUTH_STUDIES;

        public static String checkAccess(String cancerStudy, String userName, String patientId){
            if (!checkDarwinProperties() || !checkCancerStudy(cancerStudy)) return "";

            RestTemplate restTemplate = new RestTemplate();                 
            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = getRequestEntity(userName.split("@")[0], patientId);  
            ResponseEntity<DarwinAccess> responseEntity = restTemplate.exchange(DARWIN_AUTH_URL, HttpMethod.POST, requestEntity, DarwinAccess.class);  
            DarwinAccess response = responseEntity.getBody();            

            if (response.getDarwinAuthResponse().equals("valid")) {
                return DARWIN_RESPONSE_URL+patientId;
            }

            return "";
        }

        private static HttpEntity<LinkedMultiValueMap<String, Object>> getRequestEntity(String userName, String patientId) {
            LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            map.add("p_userName", userName);
            map.add("p_dmp_pid", patientId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            return new HttpEntity<LinkedMultiValueMap<String, Object>>(map, headers);
        }

        private static Boolean checkCancerStudy(String cancerStudy) {
            for (String study: DARWIN_AUTH_STUDIES) {
                if (study.trim().equals(cancerStudy.trim())) {
                    return true;
                }
            }
            return false;
        }

        private static Boolean checkDarwinProperties() {
            DARWIN_AUTH_URL = GlobalProperties.getDarwinAuthUrl();
            DARWIN_RESPONSE_URL = GlobalProperties.getDarwinResponseUrl();
            DARWIN_AUTH_STUDIES = GlobalProperties.getDarwinAuthStudies();
            if (!DARWIN_AUTH_URL.isEmpty() && !DARWIN_RESPONSE_URL.isEmpty() &&
                    DARWIN_AUTH_STUDIES != null) {
                return true;
            }
            return false;        
        }  
    }

    public static class DarwinAccess {
        /**
        * (Required)
        **/
        @JsonProperty("darwinAuthResponse")
        private String darwinAuthResponse;
        /**
        * (Required)
        **/
        @JsonProperty("p_userName")
        private String p_userName;
        /**
        * (Required)
        **/
        @JsonProperty("p_dmp_pid")
        private String p_dmp_pid;

        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
        * No args constructor for use in serialization
        **/
        public DarwinAccess() {}

        /**
        * @param darwinAuthResponse
        * @param p_userName
        * @param p_dmp_pid
        **/
        public DarwinAccess(String darwinAuthResponse, String p_userName, String p_dmp_pid) {
        this.darwinAuthResponse = darwinAuthResponse;
        this.p_userName = p_userName;
        this.p_dmp_pid = p_dmp_pid;
        }

        /**
        * (Required)
        * @return
        * The darwinAuthResponse
        **/
        @JsonProperty("darwinAuthResponse")
        public String getDarwinAuthResponse() {
        return darwinAuthResponse;
        }

        /**
        * (Required)
        * @param darwinAuthResponse
        * The Darwin authorization response
        **/
        @JsonProperty("darwinAuthResponse")
        public void setDarwinAuthResponse(String darwinAuthResponse) {
        this.darwinAuthResponse = darwinAuthResponse;
        }

        public DarwinAccess withDarwinAuthResponse(String darwinAuthResponse) {
        this.darwinAuthResponse = darwinAuthResponse;
        return this;
        }

        /**
        * (Required)
        * @return
        * The p_userName
        **/
        @JsonProperty("p_userName")
        public String getP_UserName() {
        return p_userName;
        }

        /**
        * (Required)
        * @param p_userName
        * The p_userName
        **/
        @JsonProperty("p_userName")
        public void setP_UserName(String p_userName) {
        this.p_userName = p_userName;
        }

        public DarwinAccess withP_UserName(String p_userName) {
        this.p_userName = p_userName;
        return this;
        }

        /**
        * (Required)
        * @return
        * The p_dmp_pid
        **/
        @JsonProperty("p_dmp_pid")
        public String getP_Dmp_Pid() {
        return p_dmp_pid;
        }

        /**
        * (Required)
        * @param p_dmp_pid
        * The p_dmp_pid
        **/
        @JsonProperty("p_dmp_pid")
        public void setP_Dmp_Pid(String p_dmp_pid) {
        this.p_dmp_pid = p_dmp_pid;
        }

        public DarwinAccess withP_Dmp_Pid(String p_dmp_pid) {
        this.p_dmp_pid = p_dmp_pid;
        return this;
        }

        @Override
        public String toString() {
        return ToStringBuilder.reflectionToString(this);
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        }

        public DarwinAccess withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
        }
    }    
    
    private static Options getOptions(String[] args) {
        Options gnuOptions = new Options();
        gnuOptions.addOption("h", "help", false, "shows this help document and quits.")
            .addOption("cancer_study", "cancer_study", true, "cancer_study")
            .addOption("user_name", "user_name", true, "user_name")
            .addOption("patient_id", "patient_id", true, "patient_id");

        return gnuOptions;
    }
    private static void help(Options gnuOptions, int exitStatus) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("CheckDarwinAccess", gnuOptions);
        System.exit(exitStatus);
    }    

    public static void main(String[] args) throws Exception {
        Options gnuOptions = CheckDarwinAccessMain.getOptions(args);
        CommandLineParser parser = new GnuParser();
        CommandLine commandLine = parser.parse(gnuOptions, args);
        if (commandLine.hasOption("h") || 
            !commandLine.hasOption("cancer_study") ||
            !commandLine.hasOption("user_name") ||
            !commandLine.hasOption("patient_id")) {
            help(gnuOptions, 0);
        }

        String darwinAccessUrl = CheckDarwinAccess.checkAccess(
                commandLine.getOptionValue("cancer_study"), 
                commandLine.getOptionValue("user_name"),
                commandLine.getOptionValue("patient_id"));
        if (darwinAccessUrl != null && !darwinAccessUrl.isEmpty()){
            System.out.println(darwinAccessUrl);
        }
        else {
            System.out.println("User "+commandLine.getOptionValue("user_name")+
                    " does not have access to patient "+commandLine.getOptionValue("patient_id")+
                    " or "+commandLine.getOptionValue("cancer_study")+
                    " is not an authorized study.");
        }            
    }
}