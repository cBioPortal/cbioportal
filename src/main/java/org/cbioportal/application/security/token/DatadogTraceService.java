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

package org.cbioportal.application.security.token;

import datadog.trace.api.interceptor.MutableSpan;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool-agnostic service that maps MDC context fields (user, auth_method) to the active Datadog
 * trace span for API usage telemetry.
 */
public class DatadogTraceService {

  private static final Logger LOG = LoggerFactory.getLogger(DatadogTraceService.class);

  private DatadogTraceService() {
    // Utility class - not instantiable.
  }

  /**
   * Tags the currently active Datadog span with user identity and authentication method.
   *
   * @param user the authenticated username
   * @param authMethod the authentication method used
   */
  public static void tagCurrentSpan(String user, String authMethod) {
    if (user == null || user.isEmpty()) {
      return;
    }
    try {
      Tracer tracer = GlobalTracer.get();
      if (tracer == null) {
        return;
      }
      Span span = tracer.activeSpan();
      if (span == null) {
        return;
      }
      if (span instanceof MutableSpan mutableSpan) {
        mutableSpan.setTag("usr.id", user);
        mutableSpan.setTag("auth_method", authMethod);
      } else {
        span.setTag("usr.id", user);
        span.setTag("auth_method", authMethod);
      }
      LOG.debug("Tagged active Datadog span with usr.id='{}' auth_method='{}'", user, authMethod);
    } catch (Exception e) {
      LOG.warn("Failed to tag Datadog span with user identity - continuing without telemetry", e);
    }
  }
}
