package org.cbioportal.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

@RestController
public class ProxyController {
    private static final String DEFAULT_ONCOKB_URL = "https://www.oncokb.org/api/v1";
    
    @Value("${show.oncokb:true}")
    private Boolean showOncokb;
    @Value("${oncokb.token:}")
    private String oncokbToken;
    @Value("${oncokb.public_api.url:}")
    private String oncokbApiUrl;

    private Logger LOG = LoggerFactory.getLogger(ProxyController.class);

    @RequestMapping("/**")
    public String proxy(@RequestBody(required = false) String body, HttpMethod method, HttpServletRequest request)
        throws URISyntaxException {
        HttpHeaders httpHeaders = initHeaders(request);
        
        return exchangeData(body,
            buildUri(request.getPathInfo(), request.getQueryString(), false),
            method,
            httpHeaders,
            String.class
        ).getBody();
    }

    @RequestMapping("/oncokb/**")
    public ResponseEntity<Object> proxyOncokb(@RequestBody(required = false) String body, HttpMethod method, HttpServletRequest request)
        throws URISyntaxException {
        if (!this.showOncokb) {
            return new ResponseEntity<>("OncoKB service is disabled.", HttpStatus.NOT_FOUND);
        }
        else if (StringUtils.isEmpty(oncokbToken)) {
            return new ResponseEntity<>("No OncoKB access token is provided.", HttpStatus.NOT_FOUND);
        }

        HttpHeaders httpHeaders = initHeaders(request);
        
        if (!StringUtils.isEmpty(this.oncokbToken)) {
            httpHeaders.add("Authorization", "Bearer " + this.oncokbToken);
        }

        String oncokbApiUrl = StringUtils.isEmpty(this.oncokbApiUrl) ? DEFAULT_ONCOKB_URL: this.oncokbApiUrl;
        
        return exchangeData(body, 
            buildUri(oncokbApiUrl + request.getPathInfo().replaceFirst("/oncokb", ""), request.getQueryString()), 
            method,
            httpHeaders,
            Object.class);
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
}
