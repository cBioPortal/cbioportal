package org.cbioportal.infrastructure.requestlog;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the MongoDB HTTP request logger.
 *
 * <p>The feature is disabled by default. When {@code request-logging.enabled=true} every incoming
 * HTTP request that matches {@link #getPathPatterns()} is captured (method, url, headers and body)
 * and upserted into MongoDB, deduplicated by a hash of method + path + query + body so the
 * collection stays bounded regardless of traffic. The stored documents contain everything needed to
 * replay the request with {@code curl}, and can be searched by endpoint, making them a convenient
 * corpus for QC of new endpoint implementations.
 */
@ConfigurationProperties(prefix = "request-logging")
public class RequestLoggingProperties {

  /** Master switch. When false (the default) no Mongo connection is opened and no filter is run. */
  private boolean enabled = false;

  /** MongoDB connection string for the database that stores the captured requests. */
  private String mongoUri = "mongodb://localhost:27017";

  /** Database name used to store captured requests. */
  private String database = "cbioportal_qc";

  /** Collection name used to store captured requests. */
  private String collection = "logged_requests";

  /** Ant-style path patterns to capture. Defaults to the REST API only. */
  private List<String> pathPatterns = List.of("/api/**");

  /**
   * Maximum number of request body bytes to capture (and to keep buffered in memory per request).
   * Bodies larger than this are truncated; the document is flagged with {@code bodyTruncated=true}.
   */
  private int maxBodyBytes = 5 * 1024 * 1024;

  /**
   * Header names (case-insensitive) whose values are replaced with {@code "REDACTED"} before being
   * stored, so credentials don't land in the database. Set to an empty list to capture everything
   * verbatim (e.g. when the captured requests need to be replayed with their original auth).
   */
  private List<String> redactHeaders = List.of("authorization", "cookie", "set-cookie");

  /** Number of background threads used to write captured requests to MongoDB. */
  private int writerThreads = 2;

  /**
   * Maximum number of captured requests queued for writing. When the queue is full new captures are
   * dropped (and logged at debug level) so request handling is never blocked by Mongo.
   */
  private int queueCapacity = 10_000;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getMongoUri() {
    return mongoUri;
  }

  public void setMongoUri(String mongoUri) {
    this.mongoUri = mongoUri;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
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
