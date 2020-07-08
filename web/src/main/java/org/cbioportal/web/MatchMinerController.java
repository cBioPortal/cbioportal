package org.cbioportal.web;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/matchminer")
public class MatchMinerController {

    private static final Log LOG = LogFactory.getLog( MatchMinerController.class);

    @Value("${matchminer.url:}")
    private String url;

    @Value("${matchminer.token:}")
    private String token;

    @RequestMapping(value = "/**", produces = "application/json")
    public ResponseEntity<Object> proxy(@RequestBody(required = false) JSONObject body, HttpMethod method, HttpServletRequest request) {
        try {
            String path = request.getPathInfo().replace("/matchminer", "");
            URI uri = new URI(this.url + path);

            HttpHeaders httpHeaders = new HttpHeaders();
            String contentType = request.getHeader("Content-Type");
            if (contentType != null) {
                httpHeaders.setContentType(MediaType.valueOf(contentType));
            }
            if (this.token != "") {
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
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (ParseException e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}