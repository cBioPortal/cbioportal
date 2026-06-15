package org.cbioportal.infrastructure.requestlog;

import java.time.Instant;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A single, unique HTTP request captured for QC purposes.
 *
 * <p>The {@link #id} is a deterministic hash of the request's method, path, query string and body,
 * so re-issuing the same request simply increments {@link #count} and refreshes {@link #lastSeen}
 * rather than creating a new document. This keeps the collection bounded by the number of
 * <em>distinct</em> requests, not by traffic volume.
 */
@Document(collection = "logged_requests")
public class LoggedRequest {

  /** SHA-256 of {@code method\npath\nsortedQuery\nbody}; used as the Mongo {@code _id}. */
  @Id private String id;

  private String method;

  /** Request path without the query string, e.g. {@code /api/studies/acc_tcga/clinical-data}. */
  @Indexed private String path;

  /**
   * The matched controller route pattern, e.g. {@code /api/studies/{studyId}/clinical-data}. This
   * is the primary field to search on when collecting all calls to a given endpoint.
   */
  @Indexed private String endpoint;

  private String queryString;

  /** Fully reconstructed request URL including scheme, host and query string. */
  private String url;

  private Map<String, String> headers;

  private String contentType;

  private String body;

  private boolean bodyTruncated;

  /** HTTP status returned the last time this request was seen. */
  private int responseStatus;

  /** Number of times this exact request has been observed. */
  private long count;

  private Instant firstSeen;

  private Instant lastSeen;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getQueryString() {
    return queryString;
  }

  public void setQueryString(String queryString) {
    this.queryString = queryString;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public boolean isBodyTruncated() {
    return bodyTruncated;
  }

  public void setBodyTruncated(boolean bodyTruncated) {
    this.bodyTruncated = bodyTruncated;
  }

  public int getResponseStatus() {
    return responseStatus;
  }

  public void setResponseStatus(int responseStatus) {
    this.responseStatus = responseStatus;
  }

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }

  public Instant getFirstSeen() {
    return firstSeen;
  }

  public void setFirstSeen(Instant firstSeen) {
    this.firstSeen = firstSeen;
  }

  public Instant getLastSeen() {
    return lastSeen;
  }

  public void setLastSeen(Instant lastSeen) {
    this.lastSeen = lastSeen;
  }
}
