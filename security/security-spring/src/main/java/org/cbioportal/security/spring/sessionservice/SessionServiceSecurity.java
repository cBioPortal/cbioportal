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
package org.cbioportal.security.spring.sessionservice;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SessionServiceSecurity {

  private static String NUM_SESSION_SERVICE_REQ = "count_session_requests";
  private static int MAX_SESSION_SERVICE_REQ = 1000;

  private static Log LOG = LogFactory.getLog(SessionServiceSecurity.class);

  public boolean checkRead(HttpServletRequest request) {
    return request.getSession(false) != null;
  }

  public boolean checkWrite(HttpServletRequest request) {
    if (request.getSession(false) == null)  {
      return false;
    }
    
    Integer countRequests = (Integer) request.getSession().getAttribute(NUM_SESSION_SERVICE_REQ);
    int count = 0;
    if (countRequests == null) {
      count = 1; 
    } else {
      count = countRequests.intValue();   
      count += 1;
    }
    LOG.debug("SessionServiceSecurity.check() -- " + count + 
      " session service API requests made by this session (MAX_SESSION_SERVICE_REQ = " + 
      MAX_SESSION_SERVICE_REQ + ")");
    request.getSession().setAttribute(NUM_SESSION_SERVICE_REQ, Integer.valueOf(count));
    if (MAX_SESSION_SERVICE_REQ < count) {
      LOG.warn("SessionServiceSecurity.check() -- too many requests (" + count + 
        ") made by this session to the session service API (MAX_SESSION_SERVICE_REQ = " + 
        MAX_SESSION_SERVICE_REQ + ")");
      return false;
    }
    return true;
  }
}
