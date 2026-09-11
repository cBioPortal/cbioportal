package org.cbioportal.application.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * Custom SAML authentication success handler that supports the spring-security-redirect parameter.
 * This handler checks for a redirect parameter in the request and validates it before redirecting.
 * If no valid redirect parameter is found, it falls back to the default Spring Security behavior
 * (using the saved request).
 */
public class SamlAuthenticationSuccessHandler
    extends SavedRequestAwareAuthenticationSuccessHandler {

  private static final Logger log = LoggerFactory.getLogger(SamlAuthenticationSuccessHandler.class);
  private static final String REDIRECT_PARAM = "spring-security-redirect";

  private final String allowedDomain;

  /**
   * Constructor with default target URL and optional allowed domain.
   *
   * @param allowedDomain The allowed domain for redirects (e.g., "https://cbioportal.mskcc.org").
   *     If null, only relative URLs (starting with "/") will be allowed.
   */
  public SamlAuthenticationSuccessHandler(String allowedDomain) {
    super();
    this.allowedDomain = allowedDomain;
    setDefaultTargetUrl("/");
  }

  /** Constructor with default settings (only allows relative URLs). */
  public SamlAuthenticationSuccessHandler() {
    this(null);
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, jakarta.servlet.ServletException {

    // Check for redirect parameters (support both 'spring-security-redirect' and
    // 'continue')
    String redirectParam = request.getParameter(REDIRECT_PARAM);
    if (redirectParam == null || redirectParam.isEmpty()) {
      redirectParam = request.getParameter("continue");
    }

    if (redirectParam != null && !redirectParam.isEmpty()) {
      log.debug("Found redirect parameter: {}", redirectParam);

      if (isValidRedirectUrl(redirectParam)) {
        log.info("Redirecting to: {}", redirectParam);
        getRedirectStrategy().sendRedirect(request, response, redirectParam);
        return;
      } else {
        log.warn("Invalid redirect URL rejected: {}", redirectParam);
      }
    }

    // Fall back to default behavior (uses saved request)
    log.debug("Using default authentication success behavior");
    super.onAuthenticationSuccess(request, response, authentication);
  }

  /**
   * Validates that the redirect URL is safe to use. Only allows: - Relative URLs starting with "/"
   * - Absolute URLs matching the configured allowed domain (if set)
   *
   * <p>Blocks: - Logout URLs to prevent redirect loops - Protocol-relative URLs (//example.com) -
   * URLs containing authentication endpoints
   *
   * @param url The URL to validate
   * @return true if the URL is safe to redirect to, false otherwise
   */
  private boolean isValidRedirectUrl(String url) {
    if (url == null || url.isEmpty()) {
      return false;
    }

    // Normalize the URL for checking
    String normalizedUrl = url.toLowerCase().trim();

    // Block logout URLs to prevent redirect loops
    if (normalizedUrl.contains("/logout")
        || normalizedUrl.contains("/saml/logout")
        || normalizedUrl.contains("j_spring_security_logout")) {
      log.warn("Blocked redirect to logout URL: {}", url);
      return false;
    }

    // Block login URLs to prevent redirect loops
    if (normalizedUrl.contains("/login") || normalizedUrl.contains("/saml2/authenticate")) {
      log.warn("Blocked redirect to login URL: {}", url);
      return false;
    }

    // Allow relative URLs
    if (url.startsWith("/")) {
      // Prevent open redirect via protocol-relative URLs (//example.com)
      if (url.startsWith("//")) {
        return false;
      }
      return true;
    }

    // If an allowed domain is configured, check absolute URLs
    if (allowedDomain != null && !allowedDomain.isEmpty()) {
      return url.startsWith(allowedDomain + "/");
    }

    // Reject all absolute URLs if no allowed domain is configured
    return false;
  }
}
