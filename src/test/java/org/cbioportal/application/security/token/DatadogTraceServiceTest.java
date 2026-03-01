package org.cbioportal.application.security.token;

import static org.mockito.Mockito.*;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;

public class DatadogTraceServiceTest {

    @Before
    public void setUp() throws Exception {
        // Reset GlobalTracer if needed (difficult in some environments, but we can try)
        // For unit testing, we mostly want to ensure it doesn't crash even if tracer is
        // absent
    }

    @Test
    public void tagCurrentSpan_doesNotThrowWhenTracerAbsent() {
        DatadogTraceService.tagCurrentSpan("user1", "token");
        // Should pass silently
    }

    @Test
    public void tagCurrentSpan_handlesNullUser() {
        DatadogTraceService.tagCurrentSpan(null, "token");
        // Should return early
    }
}
