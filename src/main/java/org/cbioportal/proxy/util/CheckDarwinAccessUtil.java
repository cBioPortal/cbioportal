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
    
    private CheckDarwinAccessUtil() {
        throw new IllegalStateException("Utility class"); 
    }
    
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
            logger.debug("Error Sending CheckDarwinAccess API");
        }

        return darwinResponse;
    }

    public static String getResponse(String userName, String patientId, String darwinAuthUrl, String ddpResponseUrl) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = getRequestEntity(userName, patientId);
        ResponseEntity<DarwinAccess> responseEntity = restTemplate.exchange(darwinAuthUrl, HttpMethod.POST, requestEntity, DarwinAccess.class);
        String darwinResponse = Objects.requireNonNull(responseEntity.getBody()).getDarwinAuthResponse();
        String deidentificationId = Objects.requireNonNull(responseEntity.getBody()).getDeidentificationId();
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
        private String pUserName;
        /**
         * (Required)
         **/
        @JsonProperty("p_dmp_pid")
        private String pDmpPid;
        /**
         * (Required)
         **/
        @JsonProperty("deidentification_id")
        private String deidentificationId;

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
        public DarwinAccess(String darwinAuthResponse, String pUserName, String pDmpPid, String deidentificationId) {
            this.darwinAuthResponse = darwinAuthResponse;
            this.pUserName = pUserName;
            this.pDmpPid = pDmpPid;
            this.deidentificationId = deidentificationId;
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
        public String getPUserName() {
            return pUserName;
        }

        /**
         * (Required)
         *
         * @param pUserName The p_userName
         **/
        @JsonProperty("p_userName")
        public void setPUserName(String pUserName) {
            this.pUserName = pUserName;
        }

        public DarwinAccess withPUserName(String pUserName) {
            this.pUserName = pUserName;
            return this;
        }

        /**
         * (Required)
         *
         * @return The p_dmp_pid
         **/
        @JsonProperty("p_dmp_pid")
        public String getPDmpPid() {
            return pDmpPid;
        }

        /**
         * (Required)
         *
         * @param pDmpPid The p_dmp_pid
         **/
        @JsonProperty("p_dmp_pid")
        public void setPDmpPid(String pDmpPid) {
            this.pDmpPid = pDmpPid;
        }

        /**
         * (Required)
         *
         * @return The deidentification_id
         **/
        @JsonProperty("deidentification_id")
        public String getDeidentificationId() {
            return deidentificationId;
        }

        /**
         * (Required)
         *
         * @param deidentificationId The deidentification_id
         **/
        @JsonProperty("deidentification_id")
        public void setDeidentificationId(String deidentificationId) {
            this.deidentificationId = deidentificationId;
        }

        public DarwinAccess withPDmpPid(String pDmpPid) {
            this.pDmpPid = pDmpPid;
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