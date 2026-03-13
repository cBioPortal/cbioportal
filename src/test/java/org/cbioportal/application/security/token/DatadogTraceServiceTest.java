/*
 * Copyright (c) 2024 Memorial Sloan-Kettering Cancer Center.
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;

public class DatadogTraceServiceTest {

  private Tracer mockTracer;
  private Span mockSpan;

  @Before
  public void setUp() throws Exception {
    mockTracer = mock(Tracer.class);
    mockSpan = mock(Span.class);
    when(mockTracer.activeSpan()).thenReturn(mockSpan);

    // Use reflection to register the mock tracer if GlobalTracer is already
    // registered
    // This is necessary because GlobalTracer.register() can only be called once.
    try {
      Field tracerField = GlobalTracer.class.getDeclaredField("tracer");
      tracerField.setAccessible(true);
      tracerField.set(null, mockTracer);

      Field isRegisteredField = GlobalTracer.class.getDeclaredField("isRegistered");
      isRegisteredField.setAccessible(true);
      isRegisteredField.set(null, true);
    } catch (Exception e) {
      // Fallback to standard registration if reflection fails
      GlobalTracer.registerIfAbsent(mockTracer);
    }
  }

  @Test
  public void tagCurrentSpan_setsTagsWhenTracerAndSpanPresent() {
    DatadogTraceService.tagCurrentSpan("user1", "token");

    verify(mockSpan).setTag("usr.id", "user1");
    verify(mockSpan).setTag("auth_method", "token");
  }

  @Test
  public void tagCurrentSpan_doesNotThrowWhenSpanNull() {
    when(mockTracer.activeSpan()).thenReturn(null);

    DatadogTraceService.tagCurrentSpan("user1", "token");
    // Should return early and not crash
    verify(mockSpan, never()).setTag(anyString(), anyString());
  }

  @Test
  public void tagCurrentSpan_handlesNullOrEmptyUser() {
    DatadogTraceService.tagCurrentSpan(null, "token");
    DatadogTraceService.tagCurrentSpan("", "token");

    verify(mockSpan, never()).setTag(anyString(), anyString());
  }
}
