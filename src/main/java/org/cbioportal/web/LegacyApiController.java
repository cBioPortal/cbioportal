package org.cbioportal.web;


import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@RestController
public class LegacyApiController {
    @GetMapping(value = "/api/v2/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getLegacyAPI() throws IOException {
       
        Resource resource = new ClassPathResource("legacy-api.json");
        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
        
        String jsonData = FileCopyUtils.copyToString(reader);
        return ResponseEntity.ok(jsonData);
    }
}
