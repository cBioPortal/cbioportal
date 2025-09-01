package org.cbioportal.application.assistant.openai;

import com.openai.springboot.OpenAIClient;
import com.openai.springboot.OpenAIClientCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

  @Bean
  @ConditionalOnExpression("'${assistant}' == 'gpt'")
  public OpenAIClientCustomizer openAIClientCustomizer() {
    return builder -> builder.maxRetries(3);
  }

  @Bean
  @ConditionalOnExpression("'${assistant}' == 'gpt'")
  public OpenAIClient openAIClient(OpenAIClientCustomizer customizer) {
    return OpenAIClient.builder().apply(customizer).build();
  }
}
