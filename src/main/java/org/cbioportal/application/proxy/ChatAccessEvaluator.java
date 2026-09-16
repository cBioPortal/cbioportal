package org.cbioportal.application.proxy;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * <p>A name qualifies either by being listed individually or by sitting at a listed domain. Both
 * are necessary-but-not-sufficient: the name must also have been reported by Google, which {@link
 * #isAllowed(Authentication)} establishes before consulting either list.
 *
 * <p>Both lists are empty when unset, and an empty list matches nobody, so a missing configuration
 * closes the feature rather than opening it.
 */
@Component("chatAccess")
@ConditionalOnProperty(name = "chat.sidebar.url")
public class ChatAccessEvaluator {

  private static final Logger log = LoggerFactory.getLogger(ChatAccessEvaluator.class);

  /** Both spellings appear as the issuer on Google's id tokens. */
  private static final Set<String> GOOGLE_ISSUERS =
      Set.of("https://accounts.google.com", "accounts.google.com");

  private final Set<String> allowedUsers;
  private final Set<String> allowedDomains;

  public ChatAccessEvaluator(
      @Value("${chat.sidebar.allowed_users:}") String allowedUsers,
      @Value("${chat.sidebar.allowed_domains:}") String allowedDomains) {
    this.allowedUsers = parseList(allowedUsers, UnaryOperator.identity());
    this.allowedDomains = parseList(allowedDomains, ChatAccessEvaluator::stripWildcard);
    warnAboutEntriesThatCannotMatch();
    // A wildcard list no longer says who has access, so record at least how much was granted.
    log.info(
        "chat sidebar allowlist loaded: {} address(es), {} domain(s)",
        this.allowedUsers.size(),
        this.allowedDomains.size());
  }

  public boolean isAllowed(Authentication authentication) {
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return false;
    }
    // Established up front rather than alongside the lists: it guards both of them, and an && sat
    // next to an || is one precedence slip away from silently skipping it for one branch.
    if (!reportedByGoogle(authentication)) {
      return false;
    }
    String name = authentication.getName();
    if (name == null) {
      return false;
    }
    String email = normalize(name);
    return allowedUsers.contains(email) || allowedDomains.contains(domainOf(email));
  }

  /**
   * The allowlists match on the email claim, and only Google's is worth anything: Google issues an
   * address to whoever demonstrated control of it, whereas Entra lets a tenant set a user's email
   * to a domain the tenant does not own — so anyone could register a free tenant, claim an address
   * and arrive here named by it. Matching a list therefore proves nothing unless Google is what
   * said so.
   *
   * <p>Load-bearing, and more so for the domain list than the address list: forging a listed
   * address at least requires knowing one, whereas a listed domain is public and yields an endless
   * supply of plausible names. Do not drop this as redundant with whatever the portal's identity
   * providers happen to be configured as today.
   */
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

  /**
   * The last "@" separates the address, not the first: "alice@mskcc.org@evil.com" is an address at
   * evil.com, and reading it as one at mskcc.org would hand a listed domain to anyone able to
   * register a Google account. Do not simplify this to indexOf or to split("@")[1].
   *
   * <p>A name with no "@" has no domain and matches nothing, since the empty string never survives
   * into {@code allowedDomains}.
   */
  private static String domainOf(String email) {
    int separator = email.lastIndexOf('@');
    return separator < 0 ? "" : email.substring(separator + 1);
  }

  /** Operators reach for "@mskcc.org" and "*@mskcc.org"; both mean the bare domain. */
  private static String stripWildcard(String domain) {
    String stripped = domain.startsWith("*") ? domain.substring(1) : domain;
    return stripped.startsWith("@") ? stripped.substring(1) : stripped;
  }

  /**
   * Normalizing before the mapper, and dropping empties after it, is deliberate: an entry of "@"
   * strips to the empty string, and letting that into the domain set would match every name that
   * has no domain at all.
   */
  private static Set<String> parseList(String value, UnaryOperator<String> mapper) {
    return Arrays.stream(value.split(","))
        .map(ChatAccessEvaluator::normalize)
        .map(mapper)
        .filter(entry -> !entry.isEmpty())
        .collect(Collectors.toUnmodifiableSet());
  }

  /** An entry in the wrong property fails closed, but silently; say so while anyone is looking. */
  private void warnAboutEntriesThatCannotMatch() {
    allowedUsers.stream()
        .filter(user -> user.indexOf('@') < 0)
        .forEach(
            user ->
                log.warn(
                    "chat.sidebar.allowed_users entry \"{}\" is not an email address and will never"
                        + " match; a whole domain belongs in chat.sidebar.allowed_domains",
                    user));
    allowedDomains.stream()
        .filter(domain -> domain.indexOf('@') >= 0)
        .forEach(
            domain ->
                log.warn(
                    "chat.sidebar.allowed_domains entry \"{}\" is an address rather than a domain"
                        + " and will never match; one person belongs in chat.sidebar.allowed_users",
                    domain));
  }

  private static String normalize(String user) {
    return user.trim().toLowerCase(Locale.ROOT);
  }
}
