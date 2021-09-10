package org.cbioportal.utils.validation;

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AllowedValuesValidatorImpl implements ConstraintValidator<AllowedValues, String> {

    String[] valueList;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean isValid = Arrays.stream(valueList)
            .anyMatch(allowedValue -> allowedValue.equals(value));
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Allowed values are: " +
                    Arrays.stream(valueList).collect(Collectors.joining(", ")))
                .addConstraintViolation();
        }
        return isValid;
    }

    @Override
    public void initialize(AllowedValues constraintAnnotation) {
        valueList = constraintAnnotation.values();
    }

}