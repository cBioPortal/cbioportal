package org.cbioportal.application.chat;

import com.langfuse.client.LangfuseClient;
import com.langfuse.client.resources.prompts.requests.GetPromptRequest;
import com.langfuse.client.resources.prompts.types.Prompt;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Fetches the system prompt from Langfuse, cached, falling back to a hardcoded one on failure. */
@Service
public class SystemPromptService {

  private static final Logger LOG = LoggerFactory.getLogger(SystemPromptService.class);

  private static final String PROMPT_NAME = "cBioChatAgent System Prompt";
  private static final Duration CACHE_TTL = Duration.ofMinutes(5);

  private static final String FALLBACK_SYSTEM_PROMPT =
      "You are the cBioPortal AI assistant, embedded as a chat sidebar on the cBioPortal cancer "
          + "genomics website. Use the cbioportal-navigator tools to find the right page for the "
          + "user's research question, and the ClickHouse data-query tools to answer questions "
          + "about the underlying data itself.";

  private final LangfuseClient langfuseClient;

  private volatile String cachedPrompt;
  private volatile Instant cachedAt = Instant.EPOCH;

  public SystemPromptService(
      @Value("${LANGFUSE_HOST:${LANGFUSE_BASE_URL:}}") String baseUrl,
      @Value("${LANGFUSE_PUBLIC_KEY:}") String publicKey,
      @Value("${LANGFUSE_SECRET_KEY:}") String secretKey) {
    this.langfuseClient =
        LangfuseClient.builder().url(baseUrl).credentials(publicKey, secretKey).build();
  }

  public synchronized String getSystemPrompt() {
    if (cachedPrompt != null
        && Duration.between(cachedAt, Instant.now()).compareTo(CACHE_TTL) < 0) {
      return cachedPrompt;
    }
    try {
      Prompt prompt =
          langfuseClient
              .prompts()
              .get(PROMPT_NAME, GetPromptRequest.builder().label("production").build());
      cachedPrompt = prompt.getText().map(t -> t.getPrompt()).orElse(FALLBACK_SYSTEM_PROMPT);
    } catch (Exception e) {
      LOG.warn(
          "Could not fetch system prompt '{}' from Langfuse, using {}",
          PROMPT_NAME,
          cachedPrompt != null ? "last cached copy" : "hardcoded fallback",
          e);
      cachedPrompt = cachedPrompt != null ? cachedPrompt : FALLBACK_SYSTEM_PROMPT;
    }
    cachedAt = Instant.now();
    return cachedPrompt;
  }
}
