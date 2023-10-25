package org.cbioportal.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/hello")
    public String index() {
        return "Greetings from Spring Boot!";
    }

}
