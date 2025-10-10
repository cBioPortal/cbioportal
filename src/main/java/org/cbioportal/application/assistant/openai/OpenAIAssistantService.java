package org.cbioportal.application.assistant.openai;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "assistant", havingValue = "gpt")
public class OpenAIAssistantService {

  @Value("${assistant.openai.model}")
  private String model;

  private final OpenAIClient openAIClient;

  public OpenAIAssistantService(OpenAIClient openAIClient) {
    this.openAIClient = openAIClient;
  }

  public String ask(String message) {
    Response response =
        openAIClient
            .responses()
            .create(ResponseCreateParams.builder().model(model).input(message).build());
    System.out.println(response);
    String text =
        response.output().stream()
            .map(ResponseOutputItem::toString)
            .collect(Collectors.joining("\n"));

    return text;
  }
}
