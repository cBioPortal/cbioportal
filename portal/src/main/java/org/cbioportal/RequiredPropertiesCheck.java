package org.cbioportal;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RequiredPropertiesCheck {

    // --- Add all required portal.properties here.

    @Value("${authenticate:not defined}")
    private String authenticate;

    // ---

    @PostConstruct
    public void checkProperties() {

        Field[] requiredProperties = RequiredPropertiesCheck.class.getDeclaredFields();
        String missingFieldsString = Arrays.stream(requiredProperties)
            .filter(field -> {
                try {
                    return "not defined".equals((String) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return false;
            })
            .map(Field::getName)
            .collect(Collectors.joining("', '"));
        if (!missingFieldsString.isEmpty()) {
            throw new IllegalStateException("TERMINATING -> define the following cBioPortal properties: '" + missingFieldsString + "'");
        }
    }

}
