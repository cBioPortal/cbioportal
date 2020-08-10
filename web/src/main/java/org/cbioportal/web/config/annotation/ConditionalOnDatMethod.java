package org.cbioportal.web.config.annotation;

import org.cbioportal.web.config.DatMethodCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(value = DatMethodCondition.class)
public @interface ConditionalOnDatMethod {
    String value();
    boolean isNot() default false;
}
