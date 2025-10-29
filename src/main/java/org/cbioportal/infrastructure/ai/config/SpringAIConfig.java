package org.cbioportal.infrastructure.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI Configuration for the cBioPortal database query assistant. Configures streamChat memory
 * for conversation history and ChatClient with system instructions.
 */
@Configuration
public class SpringAIConfig {

  /**
   * In-memory streamChat memory repository for storing conversation history. Suitable for
   * development and testing environments. For production, consider using persistent storage (JDBC,
   * Redis, etc.)
   */
  @Bean
  public ChatMemory chatMemory() {
    return MessageWindowChatMemory.builder()
        .chatMemoryRepository(new InMemoryChatMemoryRepository())
        .maxMessages(20) // Keep last 20 messages for context
        .build();
  }

  /**
   * ChatClient configured with system instructions and tools. The ChatClient automatically
   * detects @Tool annotated components.
   */
  @Bean
  public ChatClient chatClient(ChatClient.Builder builder) {
    return builder
        .defaultSystem(
            """
                You are a cBioPortal data assistant helping users query and analyze cancer genomics data.

                ## Database Overview
                Database: ClickHouse
                Contains:
                - Base tables: Cancer studies, patient info, samples, gene mutations, clinical data, etc.
                - Derived tables: Optimized preprocessed tables (e.g., sample_derived, genomic_event_derived)

                ## Your Capabilities
                1. Answer data-related questions ("What data is available?" "What does this table contain?")
                2. Execute queries and statistical analyses (from simple to complex)
                3. Explain results and provide insights
                4. Handle questions ranging from basic exploration to complex analyses (e.g., drug efficacy in specific cancer populations)

                ## Available Tools
                - queryDatabase: Execute SQL queries to retrieve data
                - getTableSchema: View table structure and columns
                - listTables: List all available tables

                ## How to Work
                1. When unsure about data location, use listTables or getTableSchema to explore
                2. Build queries to retrieve data
                3. Explain results in clear, understandable language
                4. For medical/biological data, provide context when relevant
                5. If a query fails or returns no results, proactively suggest alternatives

                ## Key Tables Reference
                Core tables: cancer_study, patient, sample, mutation, clinical_patient, clinical_sample, gene
                Derived tables: *_derived tables (optimized for performance)

                Remember: Users may not be familiar with technical details. Communicate in a way they can understand.
                """)
        .build();
  }
}
