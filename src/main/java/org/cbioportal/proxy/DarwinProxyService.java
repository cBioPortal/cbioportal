package org.cbioportal.proxy;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.mvc.ProxyExchange;

@RestController
public class DarwinProxyService {
	private static final Logger logger = LoggerFactory.getLogger(DarwinProxyService.class);
	private static final String DDP_INFO_ENDPOINT = "/info";

	@Value("${darwin.auth_url:}")
	private String darwinAuthUrl;

	@Value("${ddp.response_url:}")
	private String ddpResponseUrl;

	@Value("${cis.user:}")
	private String cisUser;

	@Value("${darwin.regex:Test}")
	private String darwinRegex;

	private Pattern sampleIdRegex;

	public DarwinProxyService() {
		this.sampleIdRegex = Pattern.compile(darwinRegex);
	}

	@GetMapping("/checkDarwinAccess")
	public String proxy(ProxyExchange<DarwinAccess> proxy, Authentication authentication,
			@RequestParam(name = "sample_id") String sampleId, @RequestParam(name = "patient_id") String patientId)
			throws Exception {
		String user = authentication != null ? authentication.getName() : "anonymousUser";
		// Check Access
		logger.debug("checkDarwinAccess Requested");
		String userName = user.split("@")[0];
		String darwinResponse = "";

		String[] sampleIds = sampleId.split(",");

		if (this.sampleIdRegex.matcher(sampleIds[0]).find() && !cisUser.equals(userName)) {

			proxy.header("p_userName", user);
			proxy.header("p_dmp_pid", patientId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			proxy.headers(headers);

			ResponseEntity<DarwinAccess> re = proxy.uri(this.darwinAuthUrl).post();

			darwinResponse = Objects.requireNonNull(re.getBody()).getDarwinAuthResponse();
			String deidentificationId = re.getBody().getDeidentification_Id();
			
			if (darwinResponse.equals("valid") && !deidentificationId.isEmpty()) {
				return ddpResponseUrl + deidentificationId + DDP_INFO_ENDPOINT;
			}
		}
		return "";
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
		public DarwinAccess(String darwinAuthResponse, String p_userName, String p_dmp_pid,
				String deidentification_id) {
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
