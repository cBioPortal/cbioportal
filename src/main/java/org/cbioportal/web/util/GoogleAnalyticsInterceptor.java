package org.cbioportal.web.util;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

// TODO: Double check, needed to convert HandlerInterceptorAdapter to HandlerInterceptor
@Component
@ConditionalOnExpression("!'${google.analytics.tracking.code.api:}'.isEmpty() || !'${google.analytics.application.client.id:}'.isEmpty()")
public class GoogleAnalyticsInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleAnalyticsInterceptor.class);
    
    @Value("${google.analytics.tracking.code.api:}")
    private String trackingId;

    @Value("${google.analytics.application.client.id:}")
    private String clientId;

    private static HttpHeaders defaultHeaders;
    private static LinkedMultiValueMap<String, String> globalURIVariables;
    private boolean missingGoogleAnalyticsCredentials;

    @PostConstruct
    private void initializeDefaultParams() {

        missingGoogleAnalyticsCredentials = trackingId.isEmpty() || clientId.isEmpty();
        if (missingGoogleAnalyticsCredentials) {
            if (LOG.isInfoEnabled()) {
                LOG.info("@PostContruct:");
                LOG.info("Google Analytics tracking id: {}", trackingId);
                LOG.info("Google Analytics client id: {}", clientId);
            }
            return;
        }

        defaultHeaders = new HttpHeaders();
        defaultHeaders.setAccept(Arrays.asList(MediaType.ALL));
        defaultHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        defaultHeaders.set(HttpHeaders.USER_AGENT, "cBioPortal API Reporting/1.0 via org.cbioportal.web.util.GoogleAnalyticsInterceptor");

        globalURIVariables = new LinkedMultiValueMap<>();
        globalURIVariables.add("v", "1");
        globalURIVariables.add("dt", "request logged by GoogleAnalyticsInterceptor");
        globalURIVariables.add("t", "pageview");
        globalURIVariables.add("tid", trackingId);
        globalURIVariables.add("cid", clientId);
        globalURIVariables.add("dh", "cbioportal.org");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        if (invalidAfterCompletionArgs(response)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
                LinkedMultiValueMap<String, String> thisTasksURIVariables = new LinkedMultiValueMap<>();
                thisTasksURIVariables.putAll(globalURIVariables);
                thisTasksURIVariables.add("dp", request.getRequestURI());
                HttpEntity<LinkedMultiValueMap<String, String>> requestEntity =
                    new HttpEntity<>(thisTasksURIVariables, defaultHeaders);
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    ResponseEntity<String> responseEntity =
                        restTemplate.exchange("https://www.google-analytics.com/collect", HttpMethod.POST, requestEntity, String.class);
                    HttpStatusCode responseStatus = responseEntity.getStatusCode();
                    if (responseStatus.is2xxSuccessful()) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("CompletableFuture.runAsync(): POST request successfully sent to Google Analytics: ");
                            LOG.info(requestEntity.toString());
                        }
                    }
                    else {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("CompletableFuture.runAsync(): POST request to Google Analytics failed.  HTTP status code: "
                                     + Integer.toString(responseStatus.value()));
                        }
                    }
                }
                catch(RestClientException e) {
                    e.printStackTrace();
                }
        });
    }

    // TODO I think this bean should not be created when no Google analytics are configured.    
    private boolean invalidAfterCompletionArgs(HttpServletResponse response) {
        if (missingGoogleAnalyticsCredentials || response.getHeader("referer") != null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("afterCompletion() cannot be completed:");
                if (missingGoogleAnalyticsCredentials) {
                    LOG.info("Invalid Google Analytics credentials (see @PostConstruct log entry)");
                }
                else {
                    LOG.info("Response referer is not null");
                }
            }
            return true;
        }
        return false;
    }
}
