/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
