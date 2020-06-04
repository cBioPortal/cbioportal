package org.cbioportal.service.config.annotation;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(value = DatMethodCondition.class)
public @interface ConditionalOnDatMethod {
    String value();
    boolean isNot() default false;
}
