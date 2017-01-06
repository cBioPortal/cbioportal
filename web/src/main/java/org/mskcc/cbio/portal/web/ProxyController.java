/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.web;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.*;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/proxy")
public class ProxyController
{
  private String bitlyURL;
  @Value("${bitly.url}")
  public void setBitlyURL(String property) { this.bitlyURL = property; }

  private String sessionServiceURL;
  @Value("${session.service.url:''}") // default is empty string
  public void setSessionServiceURL(String property) { this.sessionServiceURL = property; }

  private String pdbDatabaseURL;
  @Value("${pdb.database.url}")
  public void setPDBDatabaseURL(String property) { this.pdbDatabaseURL = property; }

  private String oncokbApiURL;
  @Value("${oncokb.api.url:http://oncokb.org/legacy-api/}")
  public void setOncoKBURL(String property) {
      // The annotation above can only prevent oncokb.api.url is not present in the property file.
      // If user set the  oncokb.api.url to empty, we should also use the default OncoKB URL.
      if (property.isEmpty()) {
          property = "http://oncokb.org/legacy-api/";
      }
      this.oncokbApiURL = property;
  }
    
    private Boolean enableOncokb;

    @Value("${show.oncokb:true}")
    public void setEnableOncokb(Boolean property) {
        if(property == null) {
            property = true;
        }
        this.enableOncokb = property;
    }
  // This is a general proxy for future use.
  // Please modify and improve it as needed with your best expertise. The author does not have fully understanding
  // of JAVA proxy when creating this proxy.
  // Created by Hongxin
  @RequestMapping(value="/{path}")
  public @ResponseBody String getProxyURL(@PathVariable String path,
                                          @RequestBody String body, HttpMethod method,
                                          HttpServletRequest request, HttpServletResponse response) throws URISyntaxException, IOException {
      String URL = null;
    
        //Switch could be replaced by a filter function
        switch (path){
          case "bitly":
            URL = bitlyURL;
            break;
        case "cancerHotSpots":
            URL = "http://cancerhotspots.org/api/hotspots/single/";
            break;
        case "oncokbAccess":
            URL = oncokbApiURL + "access";
           break;
        case "oncokbSummary":
            URL = oncokbApiURL + "summary.json";
            break;
        default:
            URL = "";
            break;
        }
        
        if(path != null && StringUtils.startsWithIgnoreCase(path, "oncokb") && !enableOncokb) {
            response.sendError(403, "OncoKB service is disabled.");
            return "";
        }
        
        //If request method is GET, include query string
        if (method.equals(HttpMethod.GET) && request.getQueryString() != null){
          URL += "?" + request.getQueryString();
        }
        
        return respProxy(URL, method, body, response);
  }

    @RequestMapping(value="/oncokbSummary", method = RequestMethod.POST)
    public @ResponseBody String getOncoKBSummary(@RequestBody String body, HttpMethod method,
                                                  HttpServletRequest request, HttpServletResponse response) throws URISyntaxException, IOException {
        if(!enableOncokb) {
            response.sendError(403, "OncoKB service is disabled.");
            return "";
        }
        return respProxy(oncokbApiURL + "summary.json", method, body, response);
    }

    @RequestMapping(value="/oncokbEvidence", method = RequestMethod.POST)
    public @ResponseBody String getOncoKBEvidence(@RequestBody JSONObject body, HttpMethod method,
                                          HttpServletRequest request, HttpServletResponse response) throws URISyntaxException, IOException {
        if(!enableOncokb) {
            response.sendError(403, "OncoKB service is disabled.");
            return "";
        }
        return respProxy(oncokbApiURL + "evidence.json", method, body, response);
    }

  @RequestMapping(value="/oncokb", method = RequestMethod.POST)
  public @ResponseBody String getOncoKB(@RequestBody JSONObject body, HttpMethod method,
                                          HttpServletRequest request, HttpServletResponse response) throws URISyntaxException, IOException {
      if(!enableOncokb) {
          response.sendError(403, "OncoKB service is disabled.");
          return "";
      }
      return respProxy(oncokbApiURL + "indicator.json", method, body, response);
  }
  
     private String respProxy(String url, HttpMethod method, Object body, HttpServletResponse response) throws IOException {
        try {
            RestTemplate restTemplate = new RestTemplate();
            URI uri = new URI(url);

            ResponseEntity<String> responseEntity =
                restTemplate.exchange(uri, method, new HttpEntity<>(body), String.class);

            return responseEntity.getBody();
        }catch (Exception exception) {
            String errorMessage = "Unexpected error: " + exception.getLocalizedMessage();
            response.sendError(503, errorMessage);
            return errorMessage;
        }
     }

  @RequestMapping(value="/bitly")
  public @ResponseBody String getBitlyURL(@RequestBody String body, HttpMethod method,
                                          HttpServletRequest request, HttpServletResponse response) throws URISyntaxException, IOException
  {
      return respProxy(bitlyURL + request.getQueryString(), method, body, response);
  }

  @RequestMapping(value="/session-service/{type}", method = RequestMethod.POST)
  public @ResponseBody Map addSessionService(@PathVariable String type, @RequestBody JSONObject body, HttpMethod method,
                                                HttpServletRequest request, HttpServletResponse response) throws URISyntaxException
  {
    RestTemplate restTemplate = new RestTemplate();
    URI uri = new URI(sessionServiceURL + type);

    // returns {"id":"5799648eef86c0e807a2e965"}
    // using HashMap because converter is MappingJackson2HttpMessageConverter (Jackson 2 is on classpath)
    // was String when default converter StringHttpMessageConverter was used
    ResponseEntity<HashMap> responseEntity =
      restTemplate.exchange(uri, method, new HttpEntity<JSONObject>(body), HashMap.class);

    return responseEntity.getBody();
  }
  
	@RequestMapping(value = "/virtual-cohort", method = RequestMethod.POST)
	public ResponseEntity<JSONObject> addVirtualCohort(@RequestBody JSONObject body) throws URISyntaxException {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new CustomResponseErrorHandler());
		URI uri = new URI(sessionServiceURL + "virtual_cohort");

		return restTemplate.exchange(uri, HttpMethod.POST,
				new HttpEntity<JSONObject>(body), JSONObject.class);
	}
	
	@RequestMapping(value = "/virtual-cohort/{id}")
	public ResponseEntity<JSONObject> getVirtualCohortById(@PathVariable String id, @RequestBody(required = false)  JSONObject body, HttpMethod method) throws URISyntaxException {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new CustomResponseErrorHandler());
		String urlString = sessionServiceURL + "virtual_cohort";
			urlString = urlString + "/" + id;
			URI uri = new URI(urlString);
			JSONObject requestObject = new JSONObject();
			switch (method){
	          case PUT:
	        	  requestObject = body;
	        	  break;
	          case DELETE:
	          case GET:
	        	  break;
	          default:
	        	  return new ResponseEntity<>(new JSONObject(), HttpStatus.METHOD_NOT_ALLOWED);
			}
			if(method.equals(HttpMethod.PUT)) {
				requestObject = body;
			}
			return restTemplate.exchange(uri, method,
					new HttpEntity<JSONObject>(requestObject), JSONObject.class);
	}
	
	@RequestMapping(value = "/virtual-cohort/get-user-cohorts", method = RequestMethod.GET)
	public @ResponseBody JSONArray getUserVirtualCohorts(@RequestParam String email) throws URISyntaxException {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new CustomResponseErrorHandler());
		String urlString = sessionServiceURL + "virtual_cohort";
		JSONArray responseObject = new JSONArray();
		if(email != null) {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(urlString+"/query")
			        .queryParam("field","data.userID")
			        .queryParam("value", email);
			ResponseEntity<JSONArray> responseEntity = restTemplate.exchange(builder.build().toUri(), HttpMethod.GET,
					new HttpEntity<JSONObject>(new JSONObject()), JSONArray.class);
			responseObject =  responseEntity.getBody();
		}
		return responseObject;
	}
	
}

class CustomResponseErrorHandler implements ResponseErrorHandler {

	private static final Log logger = LogFactory.getLog(CustomResponseErrorHandler.class);
	
	@Override
	public void handleError(ClientHttpResponse clienthttpresponse) throws IOException {
		 logger.debug(clienthttpresponse);
	}

	@Override
	public boolean hasError(ClientHttpResponse clienthttpresponse) throws IOException {

		if (clienthttpresponse.getStatusCode() != HttpStatus.OK) {
			 logger.debug("Status code: " + clienthttpresponse.getStatusCode());
			 logger.debug("Response" + clienthttpresponse.getStatusText());
			 logger.debug(clienthttpresponse.getBody());
			return false;
		}
		return true;
	}
}
