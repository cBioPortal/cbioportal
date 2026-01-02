package org.cbioportal.legacy.web.util;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
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

/**
 * Interceptor for sending HTTP request data to Google Analytics.
 * 
 * Implementation Details:
 * - Only implements afterCompletion() from HandlerInterceptor interface, which is sufficient
 *   for logging completed requests asynchronously
 * - preHandle() and postHandle() methods are not overridden as they are not needed
 * - Bean creation is conditional on both tracking ID and client ID being configured
 * - Uses CompletableFuture for async reporting to avoid blocking request processing
 */
@Component
@ConditionalOnExpression(
    "!'${google.analytics.tracking.code.api:}'.isEmpty() && !'${google.analytics.application.client.id:}'.isEmpty()")
public class GoogleAnalyticsInterceptor implements HandlerInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(GoogleAnalyticsInterceptor.class);
  private static final String GOOGLE_ANALYTICS_ENDPOINT = "https://www.google-analytics.com/collect";

  @Value("${google.analytics.tracking.code.api:}")
  private String trackingId;

  @Value("${google.analytics.application.client.id:}")
  private String clientId;

  private static HttpHeaders defaultHeaders;
  private static LinkedMultiValueMap<String, String> globalURIVariables;
  private RestTemplate restTemplate;
  private boolean credentialsConfigured;

  @PostConstruct
  private void initializeDefaultParams() {
    // Validate that both tracking ID and client ID are configured
    credentialsConfigured = hasValidCredentials(trackingId, clientId);
    
    if (!credentialsConfigured) {
      if (LOG.isInfoEnabled()) {
        LOG.info("Google Analytics interceptor disabled: missing credentials");
        if (trackingId == null || trackingId.isEmpty()) {
          LOG.info("  - Tracking ID not configured (google.analytics.tracking.code.api)");
        }
        if (clientId == null || clientId.isEmpty()) {
          LOG.info("  - Client ID not configured (google.analytics.application.client.id)");
        }
      }
      return;
    }
    
    // Initialize RestTemplate once instead of creating new instances for each request
    this.restTemplate = new RestTemplate();

    // Initialize HTTP headers for Google Analytics requests
    defaultHeaders = new HttpHeaders();
    defaultHeaders.setAccept(Arrays.asList(MediaType.ALL));
    defaultHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    defaultHeaders.set(
        HttpHeaders.USER_AGENT,
        "cBioPortal API Reporting/1.0 via org.cbioportal.legacy.web.util.GoogleAnalyticsInterceptor");

    // Initialize global URI variables for all Google Analytics requests
    globalURIVariables = new LinkedMultiValueMap<>();
    globalURIVariables.add("v", "1");  // Google Analytics API version
    globalURIVariables.add("dt", "request logged by GoogleAnalyticsInterceptor");  // page title
    globalURIVariables.add("t", "pageview");  // hit type
    globalURIVariables.add("tid", trackingId);  // tracking ID
    globalURIVariables.add("cid", clientId);  // client ID
    globalURIVariables.add("dh", "cbioportal.org");  // document hostname
    
    if (LOG.isInfoEnabled()) {
      LOG.info("Google Analytics interceptor initialized successfully");
    }
  }

  /**
   * Validates if Google Analytics credentials are properly configured.
   * Both tracking ID and client ID must be non-null and non-empty.
   *
   * @param trackingId the Google Analytics tracking ID
   * @param clientId the Google Analytics client ID
   * @return true if both credentials are valid, false otherwise
   */
  private boolean hasValidCredentials(String trackingId, String clientId) {
    return trackingId != null && !trackingId.isEmpty() && 
           clientId != null && !clientId.isEmpty();
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

    // Skip analytics reporting if credentials are not configured or response is invalid
    if (!credentialsConfigured || shouldSkipAnalyticsReporting(response)) {
      return;
    }

    // Submit analytics reporting asynchronously to avoid blocking request processing
    CompletableFuture.runAsync(
        () -> reportToGoogleAnalytics(request),
        CompletableFuture.delayedExecutor(0, java.util.concurrent.TimeUnit.MILLISECONDS));
  }

  /**
   * Determines if analytics reporting should be skipped for this response.
   *
   * @param response the HTTP response
   * @return true if reporting should be skipped, false otherwise
   */
  private boolean shouldSkipAnalyticsReporting(HttpServletResponse response) {
    return response == null || response.getHeader("referer") != null;
  }

  /**
   * Reports the completed request to Google Analytics.
   * Handles all errors gracefully without blocking the main request.
   *
   * @param request the HTTP request
   */
  private void reportToGoogleAnalytics(HttpServletRequest request) {
    try {
      LinkedMultiValueMap<String, String> analyticsVariables = new LinkedMultiValueMap<>();
      analyticsVariables.putAll(globalURIVariables);
      analyticsVariables.add("dp", request.getRequestURI());  // document path
      
      HttpEntity<LinkedMultiValueMap<String, String>> requestEntity =
          new HttpEntity<>(analyticsVariables, defaultHeaders);
      
      ResponseEntity<String> responseEntity =
          restTemplate.exchange(
              GOOGLE_ANALYTICS_ENDPOINT,
              HttpMethod.POST,
              requestEntity,
              String.class);
      
      if (responseEntity.getStatusCode().is2xxSuccessful()) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Successfully reported to Google Analytics for: {}", request.getRequestURI());
        }
      } else {
        LOG.warn(
            "Google Analytics reporting failed with HTTP status: {} for URI: {}",
            responseEntity.getStatusCode().value(),
            request.getRequestURI());
      }
    } catch (RestClientException e) {
      LOG.warn(
          "Failed to report request to Google Analytics for URI: {}",
          request.getRequestURI(),
          e);
    } catch (Exception e) {
      LOG.error(
          "Unexpected error while reporting to Google Analytics for URI: {}",
          request.getRequestURI(),
          e);
    }
  }
}