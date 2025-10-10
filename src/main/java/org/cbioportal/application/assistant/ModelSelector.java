package org.cbioportal.application.assistant;

import java.util.Optional;
import org.cbioportal.application.assistant.openai.OpenAIAssistantService;
import org.cbioportal.legacy.service.GeneAssistantService;
import org.springframework.stereotype.Service;

@Service
public class ModelSelector implements GeneAssistantService {

  private final Optional<OpenAIAssistantService> openAIAssistantService;

  public ModelSelector(Optional<OpenAIAssistantService> openAIAssistantService) {
    this.openAIAssistantService = openAIAssistantService;
  }

  @Override
  public String generateResponse(String message) {
    return openAIAssistantService
        .map(service -> service.ask(message))
        .orElse("No assistant configured");
  }
}
