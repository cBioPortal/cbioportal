package org.cbioportal.utils.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PropertyConditionTest {

    @Mock
    private AnnotatedTypeMetadata metadata;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConditionContext context;

    private HashMap<String, Object> attributes;
    private PropertyCondition propertyCondition;

    @Before
    public void init() {
        attributes = new HashMap();
        attributes.put("name", "requested_prop");
        attributes.put("havingValue", "requested_value");
        attributes.put("matchIfMissing", false);
        attributes.put("isNot", false);
        propertyCondition = new PropertyCondition();
    }

    @Test
    public void matchesSuccess() {
        attributes.put("name", "requested_prop");
        attributes.put("havingValue", "requested_value");
        attributes.put("matchIfMissing", false);
        when(metadata.getAnnotationAttributes(any())).thenReturn(attributes);
        when(context.getEnvironment().getProperty(eq("requested_prop"))).thenReturn("requested_value");
        Assert.assertTrue(propertyCondition.matches(context, metadata));
    }

    @Test
    public void matchesFailWrongValue() {
        attributes.put("name", "requested_prop");
        attributes.put("havingValue", "requested_value");
        attributes.put("matchIfMissing", false);
        when(metadata.getAnnotationAttributes(any())).thenReturn(attributes);
        when(context.getEnvironment().getProperty(eq("requested_prop"))).thenReturn("different_value");
        Assert.assertFalse(propertyCondition.matches(context, metadata));
    }

    @Test
    public void matchesFailNullValue() {
        attributes.put("name", "requested_prop");
        attributes.put("havingValue", "requested_value");
        attributes.put("matchIfMissing", false);
        when(metadata.getAnnotationAttributes(any())).thenReturn(attributes);
        when(context.getEnvironment().getProperty(eq("requested_prop"))).thenReturn(null);
        Assert.assertFalse(propertyCondition.matches(context, metadata));
    }

    @Test
    public void matchesSuccessNullValueAndMatchIfMissing() {
        attributes.put("name", "requested_prop");
        attributes.put("havingValue", "requested_value");
        attributes.put("matchIfMissing", true);
        when(metadata.getAnnotationAttributes(any())).thenReturn(attributes);
        when(context.getEnvironment().getProperty(eq("requested_prop"))).thenReturn(null);
        Assert.assertTrue(propertyCondition.matches(context, metadata));
    }

    @Test
    public void matchesSuccessIsNot() {
        attributes.put("name", "requested_prop");
        attributes.put("havingValue", "requested_value");
        attributes.put("matchIfMissing", false);
        attributes.put("isNot", true);
        when(metadata.getAnnotationAttributes(any())).thenReturn(attributes);
        when(context.getEnvironment().getProperty(eq("requested_prop"))).thenReturn("different_value");
        Assert.assertTrue(propertyCondition.matches(context, metadata));
    }

    @Test
    public void matchesSuccessMultipleValues() {
        attributes.put("name", "requested_prop");
        attributes.put("havingValue", new String[] {"requested_value_1", "requested_value_2"});
        attributes.put("matchIfMissing", false);
        when(metadata.getAnnotationAttributes(any())).thenReturn(attributes);
        when(context.getEnvironment().getProperty(eq("requested_prop"))).thenReturn("requested_value_2");
        Assert.assertTrue(propertyCondition.matches(context, metadata));
    }

}