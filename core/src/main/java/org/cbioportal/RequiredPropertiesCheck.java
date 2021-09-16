package org.cbioportal;

import org.springframework.beans.factory.annotation.Value;

public class RequiredPropertiesCheck {

    // Add all required portal.properties here. When no properties are found in Application context
    // Spring will throw an error message stating the property that needs to be defined.

    @Value("${authenticate}")
    private String authenticate;

}
