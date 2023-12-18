package org.cbioportal.web;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/matchminer")
public class MatchMinerController {

    private static final Logger LOG = LoggerFactory.getLogger( MatchMinerController.class);

    @Value("${matchminer.url:}")
    private String url;

    @Value("${matchminer.token:}")
    private String token;

    @RequestMapping(value = "/api/**", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = Object.class)))
    public ResponseEntity<Object> proxy(@RequestBody(required = false) JSONObject body, HttpMethod method, HttpServletRequest request) {
        try {
            // TODO when reimplemeting different dispatcherservlets with different context roots
            // reset this to  'String requestPathInfo = request.getPathInfo();'
            String requestPathInfo = request.getPathInfo() == null? request.getServletPath() : request.getPathInfo();
            String path = requestPathInfo.replace("/matchminer", "");
            URI uri = new URI(this.url + path);

            HttpHeaders httpHeaders = new HttpHeaders();
            String contentType = request.getHeader("Content-Type");
            if (contentType != null) {
                httpHeaders.setContentType(MediaType.valueOf(contentType));
            }
            if (!this.token.equals("")) {
                String auth = this.token + ":";
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
                String authHeader = "Basic " + new String(encodedAuth);
                httpHeaders.set("Authorization", authHeader);
            }

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, method, new HttpEntity<>(body, httpHeaders), String.class);
            // The response might be a json object or a json array, so I return Object to cover both cases.
            Object response = new JSONParser().parse(responseEntity.getBody());
            return new ResponseEntity<>(response, responseEntity.getStatusCode());
        } catch (URISyntaxException e) {
            LOG.error("Error occurred", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (ParseException e) {
            LOG.error("Error occurred", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}