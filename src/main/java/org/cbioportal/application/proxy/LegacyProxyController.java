/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.application.proxy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/api/proxy")
public class LegacyProxyController {

  private String bitlyURL;
  private String sessionServiceURL;
  private Boolean enableOncokb;

  @Value("${bitly.url:''}")
  public void setBitlyURL(String property) {
    this.bitlyURL = property;
  }

  @Value("${session.service.url:''}") // default is empty string
  public void setSessionServiceURL(String property) {
    this.sessionServiceURL = property;
  }

  @Value("${show.oncokb:true}")
  public void setEnableOncokb(Boolean property) {
    if (property == null) {
      property = true;
    }
    this.enableOncokb = property;
  }

  // This is a general proxy for future use.
  // Please modify and improve it as needed with your best expertise. The author does not have fully
  // understanding
  // of JAVA proxy when creating this proxy.
  // Created by Hongxin
  @RequestMapping(value = "/{path}")
  public @ResponseBody String getProxyURL(
      @PathVariable String path,
      @RequestBody(required = false) String body,
      HttpMethod method,
      HttpServletRequest request,
      HttpServletResponse response)
      throws URISyntaxException, IOException {
    Map<String, String> pathToUrl = new HashMap<>();

    pathToUrl.put("bitly", bitlyURL);
    pathToUrl.put("3dHotspots", "https://www.3dhotspots.org/api/hotspots/3d/");

    String URL = pathToUrl.get(path) == null ? "" : pathToUrl.get(path);

    if (path != null && StringUtils.startsWithIgnoreCase(path, "oncokb") && !enableOncokb) {
      response.sendError(403, "OncoKB service is disabled.");
      return "";
    }

    // If request method is GET, include query string
    if (method.equals(HttpMethod.GET) && request.getQueryString() != null) {
      URL += "?" + request.getQueryString();
    }
    return respProxy(URL, method, body, response);
  }

  private String respProxy(String url, HttpMethod method, Object body, HttpServletResponse response)
      throws IOException {
    try {
      RestTemplate restTemplate = new RestTemplate();
      URI uri = new URI(url);
      ResponseEntity<String> responseEntity =
          restTemplate.exchange(uri, method, new HttpEntity<>(body), String.class);
      return responseEntity.getBody();
    } catch (Exception exception) {
      String errorMessage = "Unexpected error: " + exception.getLocalizedMessage();
      response.sendError(503, errorMessage);
      return errorMessage;
    }
  }

  @RequestMapping(value = "/bitly", method = RequestMethod.GET)
  public @ResponseBody String getBitlyURL(
      HttpMethod method, HttpServletRequest request, HttpServletResponse response)
      throws URISyntaxException, IOException {
    return respProxy(bitlyURL + request.getQueryString(), method, null, response);
  }
}
