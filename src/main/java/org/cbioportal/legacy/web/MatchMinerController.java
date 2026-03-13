package org.cbioportal.legacy.web;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/matchminer")
public class MatchMinerController {

  private static final Logger LOG = LoggerFactory.getLogger(MatchMinerController.class);

  @Value("${matchminer.url:}")
  private String url;

  @Value("${matchminer.token:}")
  private String token;

  @RequestMapping(value = "/api/trial_match", produces = "application/json")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = Object.class)))
  public ResponseEntity<Object> proxy(
      @RequestBody(required = false) JSONObject body,
      HttpMethod method,
      HttpServletRequest request) {
    try {
      // TODO when reimplemeting different dispatcherservlets with different context roots
      // reset this to  'String requestPathInfo = request.getPathInfo();'
      String path = request.getRequestURI();
      String querystring = request.getQueryString();
      int mmindex = path.indexOf("/matchminer");
      String requestpath = path.substring(mmindex + 11);
      URI uri = new URI(this.url + requestpath + "?" + URLEncoder.encode(querystring, "UTF-8"));
      uri = new URI(this.url + requestpath + "?" + querystring);

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
      if (this.url != "") {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate
            .getMessageConverters()
            .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> responseEntity =
            restTemplate.exchange(uri, method, new HttpEntity<>(body, httpHeaders), String.class);
        HttpStatusCode responseStatus = responseEntity.getStatusCode();
        if (responseStatus.is2xxSuccessful()) {
          Object response = new JSONParser().parse(responseEntity.getBody());
          return new ResponseEntity<>(response, responseEntity.getStatusCode());
        }
      }
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (RestClientException e) {
      LOG.error("Error occurred", e);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (UnsupportedEncodingException e) {
      LOG.error("Error occurred", e);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (URISyntaxException e) {
      LOG.error("Error occurred", e);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (ParseException e) {
      LOG.error("Error occurred", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
