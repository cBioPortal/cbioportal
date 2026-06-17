package org.cbioportal.infrastructure.requestlog;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the ClickHouse HTTP request logger.
 *
 * <p>The feature is disabled by default. When {@code request-logging.enabled=true} every incoming
 * HTTP request that matches {@link #getPathPatterns()} is captured (method, url, headers and body)
 * and inserted into a dedicated ClickHouse table, reusing the application's primary datasource. The
 * table is expected to be a {@code ReplacingMergeTree} ordered by {@code id} (a hash of method +
 * path + query + body), so repeated observations of the same logical request eventually collapse to
 * a single row during background merges. The stored rows contain everything needed to replay the
 * request with {@code curl} and can be searched by endpoint, making them a convenient corpus for QC
 * of new endpoint implementations.
 */
@ConfigurationProperties(prefix = "request-logging")
public class RequestLoggingProperties {

  /** Master switch. When false (the default) no filter is registered and nothing is captured. */
  private boolean enabled = false;

  /**
   * Target table for captured requests. Defaults to the database-qualified {@code
   * cbioportal_qc.logged_requests} created by {@code db-scripts/clickhouse/request_log.sql}; an
   * unqualified value resolves against the database in the primary datasource URL. The table must
   * already exist (the application does not create it).
   */
  private String table = "cbioportal_qc.logged_requests";

  /**
   * Use ClickHouse asynchronous inserts ({@code async_insert=1, wait_for_async_insert=0}) so the
   * server batches the many small per-request inserts before flushing to disk, keeping write impact
   * low. Leave enabled unless you specifically want synchronous inserts.
   */
  private boolean asyncInsert = true;

  /** Ant-style path patterns to capture. Defaults to the REST API only. */
  private List<String> pathPatterns = List.of("/api/**");

  /**
   * Maximum number of request body bytes to capture (and to keep buffered in memory per request).
   * Bodies larger than this are truncated; the row is flagged with {@code bodyTruncated=true}.
   */
  private int maxBodyBytes = 5 * 1024 * 1024;

  /**
   * Header names (case-insensitive) whose values are replaced with {@code "REDACTED"} before being
   * stored, so credentials don't land in the database. Set to an empty list to capture everything
   * verbatim (e.g. when the captured requests need to be replayed with their original auth).
   */
  private List<String> redactHeaders = List.of("authorization", "cookie", "set-cookie");

  /**
   * Query-string and body parameter/field names (case-insensitive) whose values are replaced with
   * {@code "REDACTED"} before being stored, so secrets passed outside headers (e.g. {@code ?token=}
   * or a JSON {@code "password"} field) don't land in the database. Applied to query strings,
   * form-encoded bodies and JSON bodies. Empty by default; populating it adds a small parsing cost
   * to capturing matching requests.
   */
  private List<String> redactParams = List.of();

  /** Number of background threads used to write captured requests to ClickHouse. */
  private int writerThreads = 2;

  /**
   * Maximum number of captured requests queued for writing. When the queue is full new captures are
   * dropped (and logged at debug level) so request handling is never blocked by the database.
   */
  private int queueCapacity = 10_000;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public boolean isAsyncInsert() {
    return asyncInsert;
  }

  public void setAsyncInsert(boolean asyncInsert) {
    this.asyncInsert = asyncInsert;
  }

  public List<String> getPathPatterns() {
    return pathPatterns;
  }

  public void setPathPatterns(List<String> pathPatterns) {
    this.pathPatterns = pathPatterns;
  }

  public int getMaxBodyBytes() {
    return maxBodyBytes;
  }

  public void setMaxBodyBytes(int maxBodyBytes) {
    this.maxBodyBytes = maxBodyBytes;
  }

  public List<String> getRedactHeaders() {
    return redactHeaders;
  }

  public void setRedactHeaders(List<String> redactHeaders) {
    this.redactHeaders = redactHeaders;
  }

  public List<String> getRedactParams() {
    return redactParams;
  }

  public void setRedactParams(List<String> redactParams) {
    this.redactParams = redactParams;
  }

  public int getWriterThreads() {
    return writerThreads;
  }

  public void setWriterThreads(int writerThreads) {
    this.writerThreads = writerThreads;
  }

  public int getQueueCapacity() {
    return queueCapacity;
  }

  public void setQueueCapacity(int queueCapacity) {
    this.queueCapacity = queueCapacity;
  }
}
