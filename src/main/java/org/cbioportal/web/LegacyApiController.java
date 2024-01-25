package org.cbioportal.web;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@RestController
public class LegacyApiController {
    
    private static final String JSON_HOST_KEY = "host";
    private static final String REQUEST_HEADER_HOST = "Host";
    private static final String LEGACY_API_FILE_JSON = "legacy-api.json";
    
    @Nullable private String legacySwaggerJson;
    
    @GetMapping(value = "/api/v2/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getLegacyAPI(HttpServletRequest request) throws IOException {
        
        if(Objects.isNull(legacySwaggerJson)) {
            legacySwaggerJson = getLegacySwagger(request.getHeader(REQUEST_HEADER_HOST));
        }
        return ResponseEntity.ok(legacySwaggerJson);
    }
    
    private String getLegacySwagger(String host) throws IOException {
        Resource resource = new ClassPathResource(LEGACY_API_FILE_JSON);
        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);

        String jsonData = FileCopyUtils.copyToString(reader);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonData); 
       
        // Update key host
        if(jsonNode.has(JSON_HOST_KEY)) {
            ((ObjectNode) jsonNode).put(JSON_HOST_KEY, host);
        }
        
        // Convert back to string
        return objectMapper.writeValueAsString(jsonNode);
    }
}
