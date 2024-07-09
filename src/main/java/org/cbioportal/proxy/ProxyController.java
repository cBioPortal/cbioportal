package org.cbioportal.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.cbioportal.proxy.util.CheckDarwinAccessUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

// TODO Consider creating separate DispatcherServlets as in the original web.xml
// See: https://stackoverflow.com/a/30686733/11651683

@RestController
@RequestMapping("/proxy")
public class ProxyController {
    private static final String DEFAULT_ONCOKB_URL = "https://public.api.oncokb.org/api/v1";

    @Autowired
    private Monkifier monkifier;
    
    @Value("${oncokb.token:}")
    private String oncokbToken;
    
    @Value("${oncokb.public_api.url:https://public.api.oncokb.org/api/v1}")
    private String oncokbApiUrl;
    
    @Value("${show.oncokb:false}")
    private Boolean showOncokb;

    @Value("${darwin.auth_url:}")
    private String darwinAuthUrl;

    @Value("${ddp.response_url:}")
    private String ddpResponseUrl;

    @Value("${cis.user:}")
    private String cisUser;

    @Value("${darwin.regex:Test}")
    private String darwinRegex;

    /**
     * This dev endpoint can be used (with a personal access token) instead of the production endpoint.
     * This is useful when debugging the frontend proxy API calls.
     */
    @RequestMapping("/dev/oncokb/**")
    public String devProxyOncokb(
        @RequestBody(required = false) String body,
        HttpMethod method,
        HttpServletRequest request
    ) throws URISyntaxException {
        String token = request.getHeader("X-Proxy-User-Agreement");
        token = (token == null || token.isEmpty()) ? "NA": token;
        
        return exchangeOncokbData(
            body,
            request.getPathInfo().replaceFirst("/dev/oncokb", ""),
            request.getQueryString(),
            method,
            getOncokbHeaders(request, token)
        );
    }
    
    @RequestMapping("/A8F74CD7851BDEE8DCD2E86AB4E2A711/**")
    public String proxyEncodedOncokb(
        @RequestBody(required = false) String body,
        HttpMethod method, 
        HttpServletRequest request
    ) throws URISyntaxException, UnsupportedEncodingException {
        // make sure that the custom Proxy User Agreement header exists
        String proxyUserAgreement = request.getHeader("X-Proxy-User-Agreement");
        if (proxyUserAgreement == null || !proxyUserAgreement.equals(
            "I/We do NOT use this obfuscated proxy to programmatically obtain private OncoKB data. " +
            "I/We know that I/we should get a valid data access token by registering at https://www.oncokb.org/account/register."
        )) {
            throw new OncoKBProxyUserAgreementException();
        }
        
        String decodedBody = body == null ? null: this.monkifier.decodeBase64(body);
        String encodedPath = request.getRequestURI().replaceFirst("/proxy/A8F74CD7851BDEE8DCD2E86AB4E2A711/", "");
        String decodedPath = this.monkifier.decodeBase64(encodedPath);
        String decodedQueryString = this.monkifier.decodeQueryString(request);
        
        String response = exchangeOncokbData(
            decodedBody,
            decodedPath,
            decodedQueryString,
            method,
            getOncokbHeaders(request)
        );
        
        return "\"" + this.monkifier.encodeBase64(response) + "\"";
    }
    
    private String exchangeOncokbData(
        String body,
        String pathInfo,
        String queryString,
        HttpMethod method,
        HttpHeaders httpHeaders
    ) throws URISyntaxException {
        return exchangeData(
            body,
            buildUri(this.oncokbApiUrl + pathInfo, queryString),
            method,
            httpHeaders,
            String.class
        ).getBody();
    }
    
    private HttpHeaders getOncokbHeaders(HttpServletRequest request) {
        return this.getOncokbHeaders(request, null);
    }
    
    private HttpHeaders getOncokbHeaders(HttpServletRequest request, String token) {
        String oncokbToken = token == null ? this.oncokbToken : token;

        if (!this.showOncokb) {
            throw new OncoKBServiceIsDisabledException();
        }

        HttpHeaders httpHeaders = initHeaders(request);
        
        if (!StringUtils.isEmpty(oncokbToken)) {
            httpHeaders.add("Authorization", "Bearer " + oncokbToken);
        }
        
        return httpHeaders;
    }
    
    @GetMapping("/checkDarwinAccess")
    public ResponseEntity<String> checkDarwinAccess(HttpServletRequest request, Authentication authentication) {
        String user = authentication != null ? authentication.getName(): "anonymousUser";
        String darwinResponse = CheckDarwinAccessUtil.checkAccess(request, darwinAuthUrl, ddpResponseUrl, cisUser, Pattern.compile(darwinRegex), user); 
        return new ResponseEntity<>(darwinResponse, HttpStatus.OK); 
    }

    private HttpHeaders initHeaders(HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        
        String contentType = request.getHeader("Content-Type");
        if (contentType != null) {
            httpHeaders.setContentType(MediaType.valueOf(contentType));
        }
        
        return httpHeaders;
    }
    
    private URI buildUri(String path, String queryString, boolean useSecureProtocol) throws URISyntaxException {
        return buildUri((useSecureProtocol ? "https" : "http") + ":/" + path, queryString);
    }

    private URI buildUri(String path, String queryString) throws URISyntaxException {
        return new URI(path + (queryString == null ? "" : "?" + queryString));
    }

    private <T> ResponseEntity<T> exchangeData(String body, URI uri, HttpMethod method, HttpHeaders httpHeaders, Class<T> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate.exchange(uri, method, new HttpEntity<>(body, httpHeaders), responseType);
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "OncoKB service is disabled")
    public class OncoKBServiceIsDisabledException extends RuntimeException {
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Fair Usage Agreement is missing")
    public class OncoKBProxyUserAgreementException extends RuntimeException {
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Unknown/Invalid hostname")
    public class UnknownHostException extends RuntimeException {
    }
}