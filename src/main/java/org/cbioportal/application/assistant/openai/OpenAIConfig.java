package org.cbioportal.application.assistant.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

  @Value("${assistant.openai.api.key}")
  private String apiKey;

  @Value("${assistant.openai.base.url}")
  private String baseUrl;

  @Bean
  @ConditionalOnProperty(name = "assistant", havingValue = "gpt")
  public OpenAIClient openAIClient() {
    return OpenAIOkHttpClient.builder().apiKey(apiKey).baseUrl(baseUrl).maxRetries(1).build();
  }
}
