package org.cbioportal.application.assistant;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "azure-openai")
public class OpenAIGeneAssistantService implements GeneAssistantService {

  private static final String OQL_CONTEXT_FILE = "oql-context.st";

  private final AzureOpenAiChatModel chatModel;

  @Autowired
  public OpenAIGeneAssistantService(AzureOpenAiChatModel chatModel) {
    this.chatModel = chatModel;
  }

  @Override
  public String generateResponse(String message) {
    try {
      Resource oqlContextResource = new ClassPathResource(OQL_CONTEXT_FILE);
      String oqlContext =
          new String(oqlContextResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      Message systemMessage = new SystemMessage(oqlContext);
      Message userMessage = new UserMessage(message);

      Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
      ChatResponse response = this.chatModel.call(prompt);
      return response.getResult().getOutput().getText().toString();

    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read oql context prompt resource", e);
    }
  }
}
