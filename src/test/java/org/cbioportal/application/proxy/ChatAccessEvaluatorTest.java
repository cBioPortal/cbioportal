package org.cbioportal.application.proxy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

public class ChatAccessEvaluatorTest {

  private static Authentication user(String name) {
    return new UsernamePasswordAuthenticationToken(
        name, "n/a", AuthorityUtils.createAuthorityList("ROLE_USER"));
  }

  @Test
  public void allowsAListedUser() {
    ChatAccessEvaluator evaluator = new ChatAccessEvaluator("alice@example.org,bob@example.org");

    assertTrue(evaluator.isAllowed(user("alice@example.org")));
    assertTrue(evaluator.isAllowed(user("bob@example.org")));
  }

  @Test
  public void ignoresCaseAndSurroundingWhitespace() {
    // Identity providers are inconsistent about the case they report, and the property is
    // hand-edited.
    ChatAccessEvaluator evaluator = new ChatAccessEvaluator("  Alice@Example.org , bob@x.org ");

    assertTrue(evaluator.isAllowed(user("alice@example.ORG")));
    assertTrue(evaluator.isAllowed(user("bob@x.org")));
  }

  @Test
  public void deniesAnUnlistedUser() {
    ChatAccessEvaluator evaluator = new ChatAccessEvaluator("alice@example.org");

    assertFalse(evaluator.isAllowed(user("mallory@example.org")));
  }

  @Test
  public void deniesEveryoneWhenTheListIsUnset() {
    // A missing or blank property must close the feature, never open it.
    assertFalse(new ChatAccessEvaluator("").isAllowed(user("alice@example.org")));
    assertFalse(new ChatAccessEvaluator("  , ,").isAllowed(user("alice@example.org")));
  }

  @Test
  public void deniesAnonymousAndMissingAuthentication() {
    ChatAccessEvaluator evaluator = new ChatAccessEvaluator("anonymousUser,alice@example.org");

    assertFalse(evaluator.isAllowed(null));
    // Named "anonymousUser" by Spring, so the listed name must still not let it through.
    assertFalse(
        evaluator.isAllowed(
            new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.copyOf(AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")))));
  }
}
