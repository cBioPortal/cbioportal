package org.cbioportal.infrastructure.requestlog;

import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * Persists captured requests to MongoDB on a small background thread pool so request handling is
 * never blocked by the database write. Writes are idempotent upserts keyed by {@link
 * LoggedRequest#getId()}: the first time a distinct request is seen the full document is inserted;
 * subsequent identical requests only bump {@code count}, {@code lastSeen} and {@code
 * responseStatus}.
 */
public class RequestLogService {

  private static final Logger LOG = LoggerFactory.getLogger(RequestLogService.class);

  private final MongoOperations mongoOperations;
  private final String collection;
  private final ThreadPoolExecutor executor;

  public RequestLogService(
      MongoOperations mongoOperations, String collection, int writerThreads, int queueCapacity) {
    this.mongoOperations = mongoOperations;
    this.collection = collection;
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

  /** Hand a captured request off to be written asynchronously. Never blocks the caller. */
  public void save(LoggedRequest request) {
    try {
      executor.execute(() -> upsert(request));
    } catch (RejectedExecutionException ex) {
      // Queue is full: drop the capture rather than slow down request handling.
      LOG.debug("Request log queue full; dropping capture for {}", request.getPath());
    }
  }

  private void upsert(LoggedRequest request) {
    try {
      Query query = new Query(Criteria.where("_id").is(request.getId()));
      Update update =
          new Update()
              // Static, request-defining fields are only written on first insert.
              .setOnInsert("method", request.getMethod())
              .setOnInsert("path", request.getPath())
              .setOnInsert("endpoint", request.getEndpoint())
              .setOnInsert("queryString", request.getQueryString())
              .setOnInsert("url", request.getUrl())
              .setOnInsert("headers", request.getHeaders())
              .setOnInsert("contentType", request.getContentType())
              .setOnInsert("body", request.getBody())
              .setOnInsert("bodyTruncated", request.isBodyTruncated())
              .setOnInsert("firstSeen", request.getFirstSeen())
              // Volatile fields are refreshed on every observation.
              .set("lastSeen", request.getLastSeen())
              .set("responseStatus", request.getResponseStatus())
              .inc("count", 1);
      mongoOperations.upsert(query, update, LoggedRequest.class, collection);
    } catch (RuntimeException ex) {
      LOG.warn("Failed to log request {} to MongoDB: {}", request.getPath(), ex.getMessage());
      LOG.debug("Request logging failure", ex);
    }
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

  /** Exposed for tests. */
  Instant now() {
    return Instant.now();
  }
}
