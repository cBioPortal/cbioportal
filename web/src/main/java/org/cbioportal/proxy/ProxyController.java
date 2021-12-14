package org.cbioportal.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

// TODO Consider creating separate DispatcherServlets as in the original web.xml
// See: https://stackoverflow.com/a/30686733/11651683
@RestController
public class ProxyController {
    private static final String DEFAULT_ONCOKB_URL = "https://public.api.oncokb.org/api/v1";
    private Properties properties;

    private Logger LOG = LoggerFactory.getLogger(ProxyController.class);

    @RequestMapping("/proxy/**")
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
    public class NOOncoKBTokenProvidedException extends RuntimeException {
    }

}