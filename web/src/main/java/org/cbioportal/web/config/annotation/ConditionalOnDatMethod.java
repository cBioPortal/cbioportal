package org.cbioportal.web.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(value = DatMethodCondition.class)
public @interface ConditionalOnDatMethod {
    String value();
    boolean isNot() default false;
}
