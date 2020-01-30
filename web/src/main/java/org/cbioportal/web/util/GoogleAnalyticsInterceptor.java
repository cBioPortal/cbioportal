package org.cbioportal.web.util;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import javax.servlet.http.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.*;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class GoogleAnalyticsInterceptor extends HandlerInterceptorAdapter {
    @Value("${google.analytics.tracking.code.api}")
    private String trackingId;

    @Value("${google.analytics.application.client.id}")
    private String clientId;

    private static HttpHeaders defaultHeaders;
    private static LinkedMultiValueMap<String, String> globalURIVariables;
    private static final Logger LOG = LoggerFactory.getLogger(
        GoogleAnalyticsInterceptor.class
    );
    private static boolean missingGoogleAnalyticsCredentials;

    @PostConstruct
    private void initializeDefaultParams() {
        if (trackingId == null || trackingId.isEmpty()) {
            missingGoogleAnalyticsCredentials = true;
        }
        if (clientId == null || clientId.isEmpty()) {
            missingGoogleAnalyticsCredentials = true;
        }

        if (missingGoogleAnalyticsCredentials) {
            if (LOG.isInfoEnabled()) {
                LOG.info("@PostContruct:");
                LOG.info(
                    "Google Analytics tracking id: " +
                    ((trackingId == null) ? "null" : trackingId)
                );
                LOG.info(
                    "Google Analytics client id: " +
                    ((clientId == null) ? "null" : clientId)
                );
            }
            return;
        }

        defaultHeaders = new HttpHeaders();
        defaultHeaders.setAccept(Arrays.asList(MediaType.ALL));
        defaultHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        defaultHeaders.set(
            HttpHeaders.USER_AGENT,
            "cBioPortal API Reporting/1.0 via org.cbioportal.web.util.GoogleAnalyticsInterceptor"
        );

        globalURIVariables = new LinkedMultiValueMap<String, String>();
        globalURIVariables.add("v", "1");
        globalURIVariables.add(
            "dt",
            "request logged by GoogleAnalyticsInterceptor"
        );
        globalURIVariables.add("t", "pageview");
        globalURIVariables.add("tid", trackingId);
        globalURIVariables.add("cid", clientId);
        globalURIVariables.add("dh", "cbioportal.org");
    }

    @Override
    public void afterCompletion(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        Exception ex
    )
        throws Exception {
        if (invalidAfterCompletionArgs(response)) {
            return;
        }

        CompletableFuture.runAsync(
            () -> {
                LinkedMultiValueMap<String, String> thisTasksURIVariables = new LinkedMultiValueMap<>();
                thisTasksURIVariables.putAll(globalURIVariables);
                thisTasksURIVariables.add("dp", request.getRequestURI());
                HttpEntity<LinkedMultiValueMap<String, String>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, String>>(
                    thisTasksURIVariables,
                    defaultHeaders
                );
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    ResponseEntity<String> responseEntity = restTemplate.exchange(
                        "https://www.google-analytics.com/collect",
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                    );
                    HttpStatus responseStatus = responseEntity.getStatusCode();
                    if (responseStatus.is2xxSuccessful()) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info(
                                "CompletableFuture.runAsync(): POST request successfully sent to Google Analytics: "
                            );
                            LOG.info(requestEntity.toString());
                        }
                    } else {
                        if (LOG.isInfoEnabled()) {
                            LOG.info(
                                "CompletableFuture.runAsync(): POST request to Google Analytics failed.  HTTP status code: " +
                                Integer.toString(responseStatus.value())
                            );
                        }
                    }
                } catch (RestClientException e) {
                    e.printStackTrace();
                }
            }
        );
    }

    private boolean invalidAfterCompletionArgs(HttpServletResponse response) {
        if (
            missingGoogleAnalyticsCredentials ||
            response.getHeader("referer") != null
        ) {
            if (LOG.isInfoEnabled()) {
                LOG.info("afterCompletion() cannot be completed:");
                if (missingGoogleAnalyticsCredentials) {
                    LOG.info(
                        "Invalid Google Analytics credentials (see @PostConstruct log entry)"
                    );
                } else {
                    LOG.info("Response referer is not null");
                }
            }
            return true;
        }
        return false;
    }
}
