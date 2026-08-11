package org.cbioportal.application.chat.dto;

/** Matches the frontend's ChatMessage. Only plain-text content is used on this path. */
public class ChatMessageDto {

  private String role;
  private String content;

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
