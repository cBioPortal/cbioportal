package org.cbioportal.application.chat;

import com.anthropic.backends.Backend;
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.vertex.backends.VertexBackend;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnthropicChatConfig {

  @Value("${chat.vertex.project}")
  private String vertexProject;

  @Value("${chat.vertex.region:global}")
  private String vertexRegion;

  @Value("${chat.vertex.credentials-file}")
  private String credentialsFile;

  @Bean
  public AnthropicClient anthropicClient() throws IOException {
    GoogleCredentials credentials;
    try (FileInputStream credentialsStream = new FileInputStream(credentialsFile)) {
      // Service-account credentials carry no scopes; without this the token request 400s.
      credentials =
          GoogleCredentials.fromStream(credentialsStream)
              .createScoped("https://www.googleapis.com/auth/cloud-platform");
    }
    Backend backend =
        VertexBackend.builder()
            .googleCredentials(credentials)
            .project(vertexProject)
            .region(vertexRegion)
            .build();
    return AnthropicOkHttpClient.builder().backend(backend).build();
  }
}
