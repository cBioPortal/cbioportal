package org.cbioportal.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

// TODO Consider creating separate DispatcherServlets as in the original web.xml
// See: https://stackoverflow.com/a/30686733/11651683

@RestController
@RequestMapping("/proxy")
public class ProxyController {
    private static final String DEFAULT_ONCOKB_URL = "https://public.api.oncokb.org/api/v1";
    private Properties properties;

    private Logger LOG = LoggerFactory.getLogger(ProxyController.class);

    @Autowired
    private Monkifier monkifier;
    
    @RequestMapping("/**")
    public String proxy(@RequestBody(required = false) String body, HttpMethod method, HttpServletRequest request)
        throws URISyntaxException {
        HttpHeaders httpHeaders = initHeaders(request);

        // TODO when reimplemeting different dispatcherservlets with different context roots
        // reset this to  'String requestPathInfo = request.getPathInfo();'
        String requestPathInfo = request.getPathInfo() == null? request.getServletPath() : request.getPathInfo();
        
        return exchangeData(body,
            buildUri(requestPathInfo, request.getQueryString(), false),
            method,
            httpHeaders,
            String.class
        ).getBody();
    }
    //TODO: Hey figure out if we need this
    @RequestMapping("/legacy/proxy/oncokb/**")
    public String legacyProxyOncokb(
        @RequestBody(required = false) String body,
        HttpMethod method,
        HttpServletRequest request
    ) throws URISyntaxException {
        String token = request.getHeader("X-Proxy-User-Agreement");
        token = (token == null || token.isEmpty()) ? "NA": token;
        
        return exchangeOncokbData(
            body,
            request.getPathInfo().replaceFirst("/oncokb", ""),
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
            buildUri(getOncokbApiUrl() + pathInfo, queryString),
            method,
            httpHeaders,
            String.class
        ).getBody();
    }
    
    private String getOncokbApiUrl() {
        return getProperty("oncokb.public_api.url", DEFAULT_ONCOKB_URL);
    }
    
    private HttpHeaders getOncokbHeaders(HttpServletRequest request) {
        return this.getOncokbHeaders(request, null);
    }
    
    private HttpHeaders getOncokbHeaders(HttpServletRequest request, String token) {
        // load portal.properties
        this.properties = loadProperties(getResourceStream("portal.properties"));
        boolean showOncokb = Boolean.parseBoolean(getProperty("show.oncokb", "true"));
        String oncokbToken = token == null ? getProperty("oncokb.token", ""): token;

        if (!showOncokb) {
            throw new OncoKBServiceIsDisabledException();
        }

        HttpHeaders httpHeaders = initHeaders(request);
        
        if (!StringUtils.isEmpty(oncokbToken)) {
            httpHeaders.add("Authorization", "Bearer " + oncokbToken);
        }
        
        return httpHeaders;
    }
    
    //TODO: Figure out what is different (Rebased from Spring Boot Branch)
    @RequestMapping("/proxy/oncokb/**")
    public String proxyOncokb(@RequestBody(required = false) String body, HttpMethod method, HttpServletRequest request)
        throws URISyntaxException {
        // load portal.properties
        this.properties = loadProperties(getResourceStream("portal.properties"));
        Boolean showOncokb = Boolean.parseBoolean(getProperty("show.oncokb", "true"));
        String oncokbToken = getProperty("oncokb.token", "");
        String oncokbApiUrl = getProperty("oncokb.public_api.url", DEFAULT_ONCOKB_URL);

        if (!showOncokb) {
            throw new OncoKBServiceIsDisabledException();
        }

        HttpHeaders httpHeaders = initHeaders(request);

        if (!ObjectUtils.isEmpty(oncokbToken)) {
            httpHeaders.add("Authorization", "Bearer " + oncokbToken);
        }

        // TODO when reimplemeting different dispatcherservlets with different context roots
        // reset this to  'String requestPathInfo = request.getPathInfo();'
        String requestPathInfo = request.getPathInfo() == null? request.getServletPath() : request.getPathInfo();
        String replaceString =  request.getPathInfo() == null? "/proxy/oncokb" : "/oncokb";
        return exchangeData(body,
            buildUri(oncokbApiUrl + requestPathInfo.replaceFirst(replaceString, ""), request.getQueryString()),
            method,
            httpHeaders,
            String.class).getBody();
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

    private String getProperty(String key, String defaultValue) {
        String propertyValue = this.properties.getProperty(key, defaultValue);
        return System.getProperty(key, propertyValue);
    }

    private InputStream getResourceStream(String propertiesFileName)
    {
        String resourceFilename = null;
        InputStream resourceFIS = null;

        try {
            String home = System.getenv("PORTAL_HOME");
            if (home != null) {
                resourceFilename =
                    home + File.separator + propertiesFileName;
                resourceFIS = new FileInputStream(resourceFilename);
            }
        } catch (FileNotFoundException e) {
        }

        if (resourceFIS == null) {
            resourceFIS = this.getClass().getClassLoader().
                getResourceAsStream(propertiesFileName);
        }

        return resourceFIS;
    }
    private Properties loadProperties(InputStream resourceInputStream)
    {
        Properties properties = new Properties();

        try {
            properties.load(resourceInputStream);
            resourceInputStream.close();
        }
        catch (IOException e) {
            System.out.println("Error loading properties file: " + e.getMessage());
        }

        return properties;
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "OncoKB service is disabled")
    public class OncoKBServiceIsDisabledException extends RuntimeException {
    }

    @ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "No OncoKB access token is provided")
    public class NoOncoKBTokenProvidedException extends RuntimeException {
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Fair Usage Agreement is missing")
    public class OncoKBProxyUserAgreementException extends RuntimeException {
    }

}