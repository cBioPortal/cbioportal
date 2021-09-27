package org.cbioportal.utils.config.annotation;

import org.cbioportal.utils.config.OnExpressionCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(value = OnExpressionCondition.class)
public @interface ConditionalOnExpression {
    
    String value() default "true";
    
}
