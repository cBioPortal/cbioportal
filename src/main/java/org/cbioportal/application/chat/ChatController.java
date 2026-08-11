package org.cbioportal.application.chat;

import org.cbioportal.application.chat.dto.ChatRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/chat")
public class ChatController {

  private final LibreChatProxyService libreChatProxyService;

  public ChatController(LibreChatProxyService libreChatProxyService) {
    this.libreChatProxyService = libreChatProxyService;
  }

  @PostMapping(value = "/stream", consumes = MediaType.APPLICATION_JSON_VALUE)
  public SseEmitter stream(@RequestBody ChatRequest request) {
    // No timeout: a turn with several tool calls can take well over a minute.
    SseEmitter emitter = new SseEmitter(0L);
    libreChatProxyService.runTurn(request.getMessages(), emitter);
    return emitter;
  }
}
