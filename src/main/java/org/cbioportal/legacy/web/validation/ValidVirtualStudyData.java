package org.cbioportal.legacy.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = VirtualStudyDataValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ValidVirtualStudyData {

  String message() default "Virtual study data is invalid";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
