package org.cbioportal.application.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ApiSecurityConditionTest {

  private final ApiSecurityCondition condition = new ApiSecurityCondition();
  private final ConditionContext context = mock(ConditionContext.class);
  private final AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
  private final Environment environment = mock(Environment.class);

  public ApiSecurityConditionTest() {
    when(context.getEnvironment()).thenReturn(environment);
  }

  @Test
  public void matchesWhenAuthenticateTrue() {
    when(environment.getProperty("authenticate", "false")).thenReturn("true");
    when(environment.getProperty("dat.require_token", "false")).thenReturn("false");

    assertThat(condition.matches(context, metadata)).isTrue();
  }

  @Test
  public void doesNotMatchWhenAuthenticateFalseAndTokenNotRequired() {
    when(environment.getProperty("authenticate", "false")).thenReturn("false");
    when(environment.getProperty("dat.require_token", "false")).thenReturn("false");

    assertThat(condition.matches(context, metadata)).isFalse();
  }

  @Test
  public void matchesWhenAuthenticateFalseButTokenRequired() {
    when(environment.getProperty("authenticate", "false")).thenReturn("false");
    when(environment.getProperty("dat.require_token", "false")).thenReturn("true");

    assertThat(condition.matches(context, metadata)).isTrue();
  }

  @Test
  public void doesNotMatchWhenOptionalOAuth2AndTokenNotRequired() {
    when(environment.getProperty("authenticate", "false")).thenReturn("optional_oauth2");
    when(environment.getProperty("dat.require_token", "false")).thenReturn("false");

    assertThat(condition.matches(context, metadata)).isFalse();
  }

  @Test
  public void matchesWhenOptionalOAuth2AndTokenRequired() {
    when(environment.getProperty("authenticate", "false")).thenReturn("optional_oauth2");
    when(environment.getProperty("dat.require_token", "false")).thenReturn("true");

    assertThat(condition.matches(context, metadata)).isTrue();
  }
}
