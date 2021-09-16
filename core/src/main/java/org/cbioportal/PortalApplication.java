package org.cbioportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PortalApplication extends RequiredPropertiesCheck {
    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}
