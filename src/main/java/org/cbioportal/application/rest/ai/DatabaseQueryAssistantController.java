package org.cbioportal.application.rest.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * REST controller for AI-powered database query assistance. Provides a streamChat interface for
 * querying and analyzing cBioPortal cancer genomics data.
 */
@RestController
@RequestMapping("/api/ai")
public class DatabaseQueryAssistantController {

  private static final Logger logger =
      LoggerFactory.getLogger(DatabaseQueryAssistantController.class);
  private final ChatClient chatClient;

  public DatabaseQueryAssistantController(ChatClient chatClient) {
    this.chatClient = chatClient;
    logger.info("DatabaseQueryAssistantController initialized");
  }

  /**
   * Chat endpoint for natural language database queries.
   *
   * @param userInput The user's question or query in natural language
   * @param conversationId Conversation ID for maintaining streamChat history (required)
   * @return AI response with query results and analysis
   */
  @GetMapping("/chat")
  public String chat(
      @RequestParam(value = "userInput") String userInput,
      @RequestParam(value = "conversationId") String conversationId) {

    logger.info(
        "Received streamChat request - conversationId: {}, userInput: {}",
        conversationId,
        userInput);

    try {
      // Use ChatClient with conversation ID for memory management
      String response =
          chatClient
              .prompt()
              .user(userInput)
              .advisors(
                  advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
              .call()
              .content();

      logger.info("Chat response generated successfully for conversationId: {}", conversationId);
      return response;

    } catch (Exception e) {
      logger.error("Error processing streamChat request for conversationId: {}", conversationId, e);
      return "I encountered an error processing your request: "
          + e.getMessage()
          + ". Please try rephrasing your question or contact support if the issue persists.";
    }
  }

  /**
   * Streaming chat endpoint for natural language database queries. Returns AI response as
   * Server-Sent Events (SSE) stream, showing tokens as they are generated.
   *
   * <p>This provides a real-time, ChatGPT-like experience where users see the response being
   * "typed" out.
   *
   * @param userInput The user's question or query in natural language
   * @param conversationId Conversation ID for maintaining chat history (required)
   * @return Flux stream of response tokens (each token/chunk as it's generated)
   *     <p>Usage example with curl:
   *     <pre>
   * curl -N "http://localhost:8080/api/ai/streamchat?userInput=How%20many%20studies?&conversationId=test-123"
   * </pre>
   */
  @GetMapping(value = "/streamchat", produces = "text/event-stream")
  public Flux<String> streamChat(
      @RequestParam(value = "userInput") String userInput,
      @RequestParam(value = "conversationId") String conversationId) {

    logger.info(
        "Received streamChat request - conversationId: {}, userInput: {}",
        conversationId,
        userInput);

    try {
      // Use ChatClient with conversation ID for memory management
      // The stream() method returns tokens as they arrive from Claude
      Flux<String> responseStream =
          chatClient
              .prompt()
              .user(userInput)
              .advisors(
                  advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
              .stream()
              .content();

      // Log when stream starts
      logger.info("Starting stream response for conversationId: {}", conversationId);

      // Add logging when stream completes
      return responseStream
          .doOnComplete(
              () -> logger.info("Stream completed for conversationId: {}", conversationId))
          .doOnError(
              error -> logger.error("Stream error for conversationId: {}", conversationId, error));

    } catch (Exception e) {
      logger.error("Error processing streamChat request for conversationId: {}", conversationId, e);
      // Return error as a single event in the stream
      return Flux.just(
          "Error: "
              + e.getMessage()
              + ". Please try rephrasing your question or contact support if the issue persists.");
    }
  }
}
