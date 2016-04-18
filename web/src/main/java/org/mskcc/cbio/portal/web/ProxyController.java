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

import org.json.simple.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.*;

import java.net.*;
import javax.servlet.http.*;
import java.util.Arrays;
import java.util.Map;

@Controller
@RequestMapping("/proxy")
public class ProxyController
{
  private String bitlyURL;
  @Value("${bitly.url}")
  public void setBitlyURL(String property) { this.bitlyURL = property; }

  private String pdbDatabaseURL;
  @Value("${pdb.database.url}")
  public void setPDBDatabaseURL(String property) { this.pdbDatabaseURL = property; }

  private String oncokbURL;
  @Value("${oncokb.url}")
  public void setOncoKBURL(String property) { this.oncokbURL = property; }

  // This is a general proxy for future use.
  // Please modify and improve it as needed with your best expertise. The author does not have fully understanding
  // of JAVA proxy when creating this proxy.
  // Created by Hongxin
  @RequestMapping(value="/{path}")
  public @ResponseBody String getProxyURL(@PathVariable String path,
                                          @RequestBody String body, HttpMethod method,
                                          HttpServletRequest request, HttpServletResponse response) throws URISyntaxException
  {

    RestTemplate restTemplate = new RestTemplate();
    String URL = null;

    //Switch could be replaced by a filter function
    switch (path){
      case "bitly":
        URL = bitlyURL;
        break;
      case "oncokbAccess":
        URL = oncokbURL + "access";
        break;
        case "oncokbSummary":
            URL = oncokbURL + "summary.json";
            break;
      default:
        URL = "";
        break;
    }

    //If request method is GET, include query string
    if (method.equals(HttpMethod.GET) && request.getQueryString() != null){
      URL += "?" + request.getQueryString();
    }

    URI uri = new URI(URL);

    ResponseEntity<String> responseEntity =
            restTemplate.exchange(uri, method, new HttpEntity<String>(body), String.class);

    return responseEntity.getBody();
  }

    @RequestMapping(value="/oncokbSummary", method = RequestMethod.POST)
    public @ResponseBody String getOncoKBSummary(@RequestBody JSONObject body, HttpMethod method,
                                                  HttpServletRequest request, HttpServletResponse response) throws URISyntaxException
    {

        RestTemplate restTemplate = new RestTemplate();
        URI uri = new URI(oncokbURL + "summary.json");

        ResponseEntity<String> responseEntity =
            restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<JSONObject>(body), String.class);

        return responseEntity.getBody();
    }

    @RequestMapping(value="/oncokbEvidence", method = RequestMethod.POST)
    public @ResponseBody String getOncoKBEvidence(@RequestBody JSONObject body, HttpMethod method,
                                          HttpServletRequest request, HttpServletResponse response) throws URISyntaxException
    {

        RestTemplate restTemplate = new RestTemplate();
        URI uri = new URI(oncokbURL + "evidence.json");

        ResponseEntity<String> responseEntity =
            restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<JSONObject>(body), String.class);

        return responseEntity.getBody();
    }

  @RequestMapping(value="/oncokb", method = RequestMethod.POST)
  public @ResponseBody String getOncoKB(@RequestBody JSONObject body, HttpMethod method,
                                          HttpServletRequest request, HttpServletResponse response) throws URISyntaxException
  {

    RestTemplate restTemplate = new RestTemplate();
    URI uri = new URI(oncokbURL + "indicator.json");

    ResponseEntity<String> responseEntity =
            restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<JSONObject>(body), String.class);

    return responseEntity.getBody();
  }


  private JSONObject requestParamsToJSON(HttpServletRequest req) {
    JSONObject jsonObj = new JSONObject();
    Map<String, String[]> params = req.getParameterMap();
    for (Map.Entry<String, String[]> entry : params.entrySet()) {
      String v[] = entry.getValue();
      Object o = (v.length == 1) ? v[0] : v;
      jsonObj.put(entry.getKey(), o);
    }
    return jsonObj;
  }

  @RequestMapping(value="/bitly")
  public @ResponseBody String getBitlyURL(@RequestBody String body, HttpMethod method,
                                          HttpServletRequest request, HttpServletResponse response) throws URISyntaxException
  {
    RestTemplate restTemplate = new RestTemplate();
    URI uri = new URI(bitlyURL + request.getQueryString());

    ResponseEntity<String> responseEntity =
      restTemplate.exchange(uri, method, new HttpEntity<String>(body), String.class);

    return responseEntity.getBody();
  }

  @RequestMapping(value="/jsmol/{pdbFile}")
  public @ResponseBody String getJSMolURL(@PathVariable String pdbFile,
                                          @RequestBody String body, HttpMethod method,
                                          HttpServletRequest request, HttpServletResponse response) throws URISyntaxException
  {
    RestTemplate restTemplate = new RestTemplate();
    URI uri = new URI(pdbDatabaseURL + pdbFile + ".pdb");

    ResponseEntity<String> responseEntity =
      restTemplate.exchange(uri, method, new HttpEntity<String>(body), String.class);

    return responseEntity.getBody();
  }
}