package com.example.assistant.openai;

import com.example.assistant.AssistantService;
import com.openai.springboot.OpenAIClient;
import org.springframework.stereotype.Service;

@Service("gpt")
public class OpenAIAssistantService implements AssistantService {

  private final OpenAIClient client;

  public OpenAIAssistantService(OpenAIClient client) {
    this.client = client;
  }

  @Override
  public String generateResponse(String prompt) {
    return client.chatCompletion(prompt);
  }
}
