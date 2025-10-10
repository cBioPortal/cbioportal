package org.cbioportal.application.assistant;

import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "azure-openai")
public class OpenAIGeneAssistantService implements GeneAssistantService {

  private final AzureOpenAiChatModel chatModel;

  // @Value("classpath:/prompts/system-message.st")
  // private Resource systemResource;

  @Autowired
  public OpenAIGeneAssistantService(AzureOpenAiChatModel chatModel) {
    this.chatModel = chatModel;
  }

  @Override
  public String generateResponse(String message) {
    // SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
    // Message systemMessage = systemPromptTemplate.createMessage();
    // Prompt prompt = new Prompt(List.of(userMessage, systemMessage));
    return this.chatModel.call(message);
  }
}
