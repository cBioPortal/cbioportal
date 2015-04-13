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

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.*;

import java.net.*;
import javax.servlet.http.*;

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