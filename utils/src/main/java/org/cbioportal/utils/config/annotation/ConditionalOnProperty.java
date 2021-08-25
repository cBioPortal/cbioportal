package org.cbioportal.utils.config.annotation;

import org.cbioportal.utils.config.PropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
Examples:

Only instantiate when property my_prop has value correct_value:
- @ConditionalOnProperty(name = "my_prop", havingValue = "correct_value")

Only instantiate when property my_prop does not have value correct_value:
- @ConditionalOnProperty(name = "my_prop", havingValue = "correct_value", isNot = true)

Only instantiate when property my_prop has value correct_value_1 or correct_value_2:
- @ConditionalOnProperty(name = "my_prop", havingValue = { "correct_value_1", "correct_value_2"} )

Only instantiate when property my_prop does not have value correct_value_1 and not value correct_value_2:
- @ConditionalOnProperty(name = "my_prop", havingValue = { "correct_value_1", "correct_value_2"}, isNot = true)

Instantiate by default (when property is not defined in portal properties:
- @ConditionalOnProperty(name = "not_existing_prop", havingValue = "correct_value", matchIfMissing = true)
*/

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(value = PropertyCondition.class)
public @interface ConditionalOnProperty {
    /**
     * Name string. Name of the application property.
     */
    String name() default "";

    /**
     * havingValue string [ ]. Value or values compared with application
     *    property (can be string or string[]).
     */
    String[] havingValue() default {};

    /**
     * isNot boolean. When true a bean will be created when the application property is not
     *    equal to any of the values passed by havingValues.
     */
    boolean isNot() default false;

    /**
     * matchIfMissing boolean. Default behavior when application property is not defined.
     *    Default is false, which means the bean is not created when property is undefined.
     */
    boolean matchIfMissing() default false;
}
