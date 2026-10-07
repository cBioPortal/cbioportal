package org.cbioportal.assistant;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

/**
 * Builds the Anthropic chat model with only model and max-tokens as defaults. The Spring AI
 * autoconfigured model always sends a default temperature, which current Claude models reject.
 */
@Configuration
public class AnthropicConfig {

    @Bean
    public AnthropicChatModel anthropicChatModel(
            AnthropicApi anthropicApi,
            RetryTemplate retryTemplate,
            @Value("${spring.ai.anthropic.chat.options.model}") String model,
            @Value("${spring.ai.anthropic.chat.options.max-tokens:4096}") Integer maxTokens) {
        AnthropicChatOptions options =
                AnthropicChatOptions.builder().model(model).maxTokens(maxTokens).build();
        return AnthropicChatModel.builder()
                .anthropicApi(anthropicApi)
                .defaultOptions(options)
                .retryTemplate(retryTemplate)
                .build();
    }
}
