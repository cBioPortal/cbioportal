package org.cbioportal.application.proxy;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * Decides who may use the chat sidebar, matching on the name the identity provider reports — an
 * email address wherever user-name-attribute is configured as email. Referenced from
 * ChatProxyController's @PreAuthorize expressions as {@code @chatAccess}.
 *
 * <p>Being on the list is necessary but not sufficient: the name must also have been reported by
 * Google. See {@link #reportedByGoogle(Authentication)}.
 *
 * <p>An unset or empty list denies everyone, so a missing configuration closes the feature rather
 * than opening it.
 */
@Component("chatAccess")
@ConditionalOnProperty(name = "chat.sidebar.url")
public class ChatAccessEvaluator {

  /** Both spellings appear as the issuer on Google's id tokens. */
  private static final Set<String> GOOGLE_ISSUERS =
      Set.of("https://accounts.google.com", "accounts.google.com");

  private final Set<String> allowedUsers;

  public ChatAccessEvaluator(@Value("${chat.sidebar.allowed_users:}") String allowedUsers) {
    this.allowedUsers =
        Arrays.stream(allowedUsers.split(","))
            .map(ChatAccessEvaluator::normalize)
            .filter(user -> !user.isEmpty())
            .collect(Collectors.toUnmodifiableSet());
  }

  public boolean isAllowed(Authentication authentication) {
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return false;
    }
    String name = authentication.getName();
    return name != null
        && allowedUsers.contains(normalize(name))
        && reportedByGoogle(authentication);
  }

  private boolean reportedByGoogle(Authentication authentication) {
    if (!(authentication.getPrincipal() instanceof OAuth2User user)) {
      return false;
    }
    Object issuer = user.getAttributes().get("iss");
    if (issuer == null || !GOOGLE_ISSUERS.contains(normalize(issuer.toString()))) {
      return false;
    }
    Object verified = user.getAttributes().get("email_verified");
    return verified != null && Boolean.parseBoolean(verified.toString());
  }

  private static String normalize(String user) {
    return user.trim().toLowerCase(Locale.ROOT);
  }
}
