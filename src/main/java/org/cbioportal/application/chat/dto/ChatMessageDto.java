package org.cbioportal.application.chat.dto;

import com.fasterxml.jackson.databind.JsonNode;

/** Matches the frontend's ChatMessage: content is either a string or an array of content blocks. */
public class ChatMessageDto {

  private String role;
  private JsonNode content;

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public JsonNode getContent() {
    return content;
  }

  public void setContent(JsonNode content) {
    this.content = content;
  }
}
