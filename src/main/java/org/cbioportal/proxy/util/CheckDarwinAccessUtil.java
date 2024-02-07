package org.cbioportal.proxy.util;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public final class CheckDarwinAccessUtil {
    private static final Logger logger = LoggerFactory.getLogger(CheckDarwinAccessUtil.class);

    private static final String DDP_INFO_ENDPOINT = "/info";
    public static final String SAMPLE_ID = "sample_id";
    public static final String PATIENT_ID = "case_id";

    public static String checkAccess(HttpServletRequest request, String darwinAuthUrl, String ddpResponseUrl, String cisUser, Pattern sampleIdRegex, String user) {
        logger.debug("checkDarwinAccess Requested");
        if (!existsDarwinProperties(darwinAuthUrl, ddpResponseUrl, cisUser, sampleIdRegex)) {
            logger.debug("Darwin Properties do not exists");
            return "";
        }
        // if sample id does not match regex or username matches cis username then return empty string
        String userName = user.split("@")[0];
        String darwinResponse = "";
        try {
            String[] sampleIds = request.getParameter(SAMPLE_ID).split(",");
            if (sampleIdRegex.matcher(sampleIds[0]).find() && !cisUser.equals(userName)) {
                String patientId = request.getParameter(PATIENT_ID);
                darwinResponse = getResponse(userName, patientId, darwinAuthUrl, ddpResponseUrl);
            }
        } catch (NullPointerException ignored) {
        }

        return darwinResponse;
    }

    public static String getResponse(String userName, String patientId, String darwinAuthUrl, String ddpResponseUrl) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = getRequestEntity(userName, patientId);
        ResponseEntity<DarwinAccess> responseEntity = restTemplate.exchange(darwinAuthUrl, HttpMethod.POST, requestEntity, DarwinAccess.class);
        String darwinResponse = Objects.requireNonNull(responseEntity.getBody()).getDarwinAuthResponse();
        String deidentificationId = responseEntity.getBody().getDeidentification_Id();
        if (!darwinResponse.equals("valid")) {
            return "";
        }
        if (deidentificationId.isEmpty()) {
            return "";
        }
        // construct URL 
        return ddpResponseUrl + deidentificationId + DDP_INFO_ENDPOINT;
    }

    private static HttpEntity<LinkedMultiValueMap<String, Object>> getRequestEntity(String userName, String patientId) {
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("p_userName", userName);
        map.add("p_dmp_pid", patientId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(map, headers);
    }

    public static boolean existsDarwinProperties(String darwinAuthUrl, String ddpResponseUrl, String cisUser, Pattern sampleIdRegex) {
        return (!darwinAuthUrl.isEmpty() && !ddpResponseUrl.isEmpty() && !cisUser.isEmpty() && !sampleIdRegex.toString().isEmpty());
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
        /**
         * (Required)
         **/
        @JsonProperty("deidentification_id")
        private String deidentification_id;

        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<>();

        /**
         * No args constructor for use in serialization
         **/
        public DarwinAccess() {
        }

        /**
         *
         **/
        public DarwinAccess(String darwinAuthResponse, String p_userName, String p_dmp_pid, String deidentification_id) {
            this.darwinAuthResponse = darwinAuthResponse;
            this.p_userName = p_userName;
            this.p_dmp_pid = p_dmp_pid;
            this.deidentification_id = deidentification_id;
        }

        /**
         * (Required)
         *
         * @return The darwinAuthResponse
         **/
        @JsonProperty("darwinAuthResponse")
        public String getDarwinAuthResponse() {
            return darwinAuthResponse;
        }

        /**
         * (Required)
         *
         * @param darwinAuthResponse The Darwin authorization response
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
         *
         * @return The p_userName
         **/
        @JsonProperty("p_userName")
        public String getP_UserName() {
            return p_userName;
        }

        /**
         * (Required)
         *
         * @param p_userName The p_userName
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
         *
         * @return The p_dmp_pid
         **/
        @JsonProperty("p_dmp_pid")
        public String getP_Dmp_Pid() {
            return p_dmp_pid;
        }

        /**
         * (Required)
         *
         * @param p_dmp_pid The p_dmp_pid
         **/
        @JsonProperty("p_dmp_pid")
        public void setP_Dmp_Pid(String p_dmp_pid) {
            this.p_dmp_pid = p_dmp_pid;
        }

        /**
         * (Required)
         *
         * @return The deidentification_id
         **/
        @JsonProperty("deidentification_id")
        public String getDeidentification_Id() {
            return deidentification_id;
        }

        /**
         * (Required)
         *
         * @param deidentification_id The deidentification_id
         **/
        @JsonProperty("deidentification_id")
        public void setDeidentification_Id(String deidentification_id) {
            this.deidentification_id = deidentification_id;
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
}