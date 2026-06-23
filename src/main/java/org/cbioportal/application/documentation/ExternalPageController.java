package org.cbioportal.application.documentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

// Retrieve the content of an external page
@Controller
public class ExternalPageController {

  // Only these schemes may be fetched. file://, ftp://, gopher:// etc. are rejected so the
  // endpoint cannot be used to read local files or reach non-HTTP internal services.
  private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

  // Optional strict allowlist of hostnames that may be fetched (comma-separated). When empty, any
  // public host is allowed but internal/private destinations are always blocked (see below).
  private final Set<String> allowedHosts;

  public ExternalPageController(
      @Value("${external_page.allowed_hosts:}") String allowedHostsProperty) {
    this.allowedHosts =
        Arrays.stream(allowedHostsProperty.split(","))
            .map(String::trim)
            .filter(host -> !host.isEmpty())
            .map(host -> host.toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
  }

  // service name: getexternalpage.json
  // available via GET method
  // sourceURL is required
  @Transactional
  @RequestMapping(
      value = "/api/getexternalpage.json",
      method = {RequestMethod.GET})
  public @ResponseBody Map<String, String> getExternalPage(
      @RequestParam(required = true) String sourceURL) throws IOException {

    sourceURL = URLDecoder.decode(sourceURL, StandardCharsets.UTF_8);
    URL url = validateAndParse(sourceURL);

    URLConnection connection = url.openConnection();

    StringBuilder pageText = new StringBuilder();
    try (BufferedReader in =
        new BufferedReader(
            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = in.readLine()) != null) {
        pageText.append(line).append("\n");
      }
    }

    return Collections.singletonMap("response", pageText.toString());
  }

  /**
   * Validates that {@code sourceURL} is safe to fetch and returns the parsed {@link URL}. Rejects
   * (with HTTP 400) any URL that uses a non-HTTP(S) scheme, is not on the configured host allowlist,
   * or resolves to a loopback, link-local, private, or otherwise internal address. This prevents
   * SSRF and local file disclosure (e.g. {@code file:///etc/passwd},
   * {@code http://169.254.169.254/...}, {@code http://localhost:8080/...}).
   */
  private URL validateAndParse(String sourceURL) {
    URI uri;
    try {
      uri = new URI(sourceURL);
    } catch (URISyntaxException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed sourceURL");
    }

    String scheme = uri.getScheme();
    if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Only http and https URLs are allowed");
    }

    String host = uri.getHost();
    if (host == null || host.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sourceURL must specify a host");
    }
    String normalizedHost = host.toLowerCase(Locale.ROOT);

    if (!allowedHosts.isEmpty() && !allowedHosts.contains(normalizedHost)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Host is not allowed");
    }

    InetAddress[] addresses;
    try {
      addresses = InetAddress.getAllByName(host);
    } catch (UnknownHostException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Host could not be resolved");
    }
    // Reject if the host resolves to any internal address. Checking every resolved address
    // (rather than only the first) closes off names that mix public and internal records.
    for (InetAddress address : addresses) {
      if (isInternalAddress(address)) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Host resolves to a disallowed address");
      }
    }

    try {
      return uri.toURL();
    } catch (MalformedURLException | IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed sourceURL");
    }
  }

  private boolean isInternalAddress(InetAddress address) {
    return address.isLoopbackAddress()
        || address.isAnyLocalAddress()
        || address.isLinkLocalAddress()
        || address.isSiteLocalAddress()
        || address.isMulticastAddress();
  }
}
