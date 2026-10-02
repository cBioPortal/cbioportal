package org.cbioportal.assistant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.stream.Collectors;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@ConditionalOnProperty(name = "spring.ai.enabled", havingValue = "true")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private final AssistantService assistantService;

    @Autowired
    public Application(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @Operation(description = "Send query to AI model for gene assistance")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(schema = @Schema(implementation = ChatResponse.class)))
    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> fetchGeneAssistantResponse(@RequestBody String message) {
        ChatResponse chatResponse = assistantService.generateResponse(message);
        // Each content block (e.g. thinking, text) is a separate generation; keep only the text.
        String text =
                chatResponse.getResults().stream()
                        .map(generation -> generation.getOutput().getText())
                        .filter(StringUtils::hasText)
                        .collect(Collectors.joining("\n"));
        return ResponseEntity.ok(text);
    }
}
