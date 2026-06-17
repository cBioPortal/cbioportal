package org.cbioportal.infrastructure.requestlog;

import java.time.Instant;
import java.util.List;

/**
 * A single observation of an HTTP request captured for QC purposes.
 *
 * <p>The {@link #id} is a deterministic hash of the request's method, path, query string and body.
 * Every observation is inserted as its own row; the backing ClickHouse table is a {@code
 * ReplacingMergeTree} ordered by {@code id}, so background merges eventually collapse repeated
 * observations of the same logical request to a single row. Per-request statistics such as how many
 * times a request was seen, and the first/last time it was seen, are derived at query time ({@code
 * count()}, {@code min(seen)}, {@code max(seen)} grouped by {@code id}) rather than being
 * maintained on write.
 */
public class LoggedRequest {

  /** SHA-256 of {@code method\npath\nquery\nbody} (post-redaction); the table's sort/dedup key. */
  private String id;

  private String method;

  /** Request path without the query string, e.g. {@code /api/studies/acc_tcga/clinical-data}. */
  private String path;

  /**
   * The matched controller route pattern, e.g. {@code /api/studies/{studyId}/clinical-data}. This
   * is the primary field to search on when collecting all calls to a given endpoint.
   */
  private String endpoint;

  private String queryString;

  /** The server host the request was addressed to, e.g. {@code www.cbioportal.org}. */
  private String serverName;

  /** Fully reconstructed request URL including scheme, host and query string. */
  private String url;

  private List<HttpHeader> headers;

  private String contentType;

  private String body;

  private boolean bodyTruncated;

  /** HTTP status returned for this observation. */
  private int responseStatus;

  /** When this observation was captured. */
  private Instant seen;

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

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public List<HttpHeader> getHeaders() {
    return headers;
  }

  public void setHeaders(List<HttpHeader> headers) {
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

  public Instant getSeen() {
    return seen;
  }

  public void setSeen(Instant seen) {
    this.seen = seen;
  }
}
