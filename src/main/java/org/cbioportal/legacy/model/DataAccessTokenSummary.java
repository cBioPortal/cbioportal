package org.cbioportal.legacy.model;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;

/**
 * A data access token without its secret value. The id is a SHA-256 hash of the token so that a
 * token can be referenced (e.g. for revocation) without exposing it.
 */
public class DataAccessTokenSummary implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final int PREVIEW_CHARS = 4;

  private String id;
  private String tokenPreview;
  private String username;
  private Date expiration;
  private Date creation;

  public DataAccessTokenSummary() {}

  public static DataAccessTokenSummary fromToken(DataAccessToken dataAccessToken) {
    DataAccessTokenSummary summary = new DataAccessTokenSummary();
    summary.setId(computeId(dataAccessToken.getToken()));
    summary.setTokenPreview(maskToken(dataAccessToken.getToken()));
    summary.setUsername(dataAccessToken.getUsername());
    summary.setExpiration(dataAccessToken.getExpiration());
    summary.setCreation(dataAccessToken.getCreation());
    return summary;
  }

  public static String computeId(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  public static String maskToken(String token) {
    if (token == null || token.length() <= PREVIEW_CHARS * 2) {
      return "…";
    }
    return token.substring(0, PREVIEW_CHARS)
        + "…"
        + token.substring(token.length() - PREVIEW_CHARS);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTokenPreview() {
    return tokenPreview;
  }

  public void setTokenPreview(String tokenPreview) {
    this.tokenPreview = tokenPreview;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Date getExpiration() {
    return expiration;
  }

  public void setExpiration(Date expiration) {
    this.expiration = expiration;
  }

  public Date getCreation() {
    return creation;
  }

  public void setCreation(Date creation) {
    this.creation = creation;
  }
}
