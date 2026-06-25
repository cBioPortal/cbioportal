package org.cbioportal.infrastructure.requestlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persists captured requests to ClickHouse on a small background thread pool so request handling is
 * never blocked by the database write. Each observation is appended as a single row; the table is a
 * {@code ReplacingMergeTree} ordered by {@code id}, so duplicate observations of the same logical
 * request are collapsed by background merges rather than at write time. With {@code async_insert}
 * enabled (the default) ClickHouse buffers the many small per-request inserts server-side and
 * flushes them in batches, which is what keeps the write impact of this columnar store low.
 */
public class RequestLogService {

  private static final Logger LOG = LoggerFactory.getLogger(RequestLogService.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final DataSource dataSource;
  private final String insertSql;
  private final String gitCommit;
  private final ThreadPoolExecutor executor;

  public RequestLogService(
      DataSource dataSource,
      String table,
      boolean asyncInsert,
      String gitCommit,
      int writerThreads,
      int queueCapacity) {
    this.dataSource = dataSource;
    this.insertSql = buildInsertSql(table, asyncInsert);
    this.gitCommit = gitCommit;
    this.executor =
        new ThreadPoolExecutor(
            writerThreads,
            writerThreads,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(queueCapacity),
            runnable -> {
              Thread thread = new Thread(runnable, "request-log-writer");
              thread.setDaemon(true);
              return thread;
            });
  }

  private static String buildInsertSql(String table, boolean asyncInsert) {
    // The SETTINGS clause sits between the column list and VALUES in ClickHouse's INSERT grammar.
    String settings = asyncInsert ? " SETTINGS async_insert=1, wait_for_async_insert=0" : "";
    return "INSERT INTO "
        + table
        + " (id, method, path, endpoint, query_string, server_name, url, headers, content_type,"
        + " body, body_truncated, response_status, seen, git_commit)"
        + settings
        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  }

  /** Hand a captured request off to be written asynchronously. Never blocks the caller. */
  public void save(LoggedRequest request) {
    try {
      executor.execute(() -> insert(request));
    } catch (RejectedExecutionException ex) {
      // Queue is full: drop the capture rather than slow down request handling.
      LOG.debug("Request log queue full; dropping capture for {}", request.getPath());
    }
  }

  private void insert(LoggedRequest request) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insertSql)) {
      statement.setString(1, request.getId());
      statement.setString(2, request.getMethod());
      statement.setString(3, request.getPath());
      statement.setString(4, request.getEndpoint());
      statement.setString(5, nullToEmpty(request.getQueryString()));
      statement.setString(6, nullToEmpty(request.getServerName()));
      statement.setString(7, nullToEmpty(request.getUrl()));
      statement.setString(8, writeHeaders(request.getHeaders()));
      statement.setString(9, nullToEmpty(request.getContentType()));
      statement.setString(10, nullToEmpty(request.getBody()));
      statement.setInt(11, request.isBodyTruncated() ? 1 : 0);
      statement.setInt(12, request.getResponseStatus());
      statement.setTimestamp(13, Timestamp.from(request.getSeen()));
      statement.setString(14, gitCommit);
      statement.executeUpdate();
    } catch (SQLException | RuntimeException ex) {
      LOG.warn("Failed to log request {} to ClickHouse: {}", request.getPath(), ex.getMessage());
      LOG.debug("Request logging failure", ex);
    }
  }

  private static String writeHeaders(List<HttpHeader> headers) {
    try {
      return OBJECT_MAPPER.writeValueAsString(headers == null ? List.of() : headers);
    } catch (RuntimeException | com.fasterxml.jackson.core.JsonProcessingException ex) {
      return "[]";
    }
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  @PreDestroy
  public void shutdown() {
    executor.shutdown();
    try {
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      executor.shutdownNow();
    }
  }
}
