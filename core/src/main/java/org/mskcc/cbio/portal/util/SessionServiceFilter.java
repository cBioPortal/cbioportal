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

package org.mskcc.cbio.portal.util;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Manda Wilson
 */
public class SessionServiceFilter<W extends HttpServletRequestWrapper>
    implements Filter {
    private static Log LOG = LogFactory.getLog(SessionServiceFilter.class);
    // this is used to see if we have already redirected
    // to same URL with the URL fragment/anchor (#blah)
    private static String RETRIEVED_SESSION_PARAM = "found_session";

    @Override
    public void init(FilterConfig aConfig) throws ServletException {
        //do nothing
    }

    @Override
    public void destroy() {
        //do nothing
    }

    @Override
    public void doFilter(
        ServletRequest aRequest,
        ServletResponse aResponse,
        FilterChain aChain
    )
        throws IOException, ServletException {
        LOG.debug("SessionServiceFilter.doFilter()");
        LOG.info(
            "SessionServiceFilter.doFilter() -- calling request.getSession() to create a valid session which is a requirement for the session service API proxy"
        );
        HttpServletRequest request = (HttpServletRequest) aRequest;
        request.getSession();
        String sessionServiceURL = GlobalProperties.getSessionServiceUrl();
        if (!StringUtils.isBlank(sessionServiceURL)) {
            SessionServiceRequestWrapper wrapper = new SessionServiceRequestWrapper(
                request
            );
            // do not get this parameter from the SessionServiceRequestWrapper -- it was not stored as part of the session
            String foundSession = request.getParameter(RETRIEVED_SESSION_PARAM);
            String sessionId = wrapper.getParameter(
                SessionServiceRequestWrapper.SESSION_ID_PARAM
            );
            String urlHashData = wrapper.getParameter("url_hash_data");
            // check if we already added #fragment to URL,
            // if not try to pull the fragment (url_hash_data) from session service
            if (
                StringUtils.isBlank(foundSession) &&
                !StringUtils.isBlank(sessionId) &&
                !StringUtils.isBlank(urlHashData)
            ) {
                String requestURI = request.getRequestURI();
                String newURI =
                    requestURI +
                    "?" +
                    SessionServiceRequestWrapper.SESSION_ID_PARAM +
                    "=" +
                    sessionId +
                    "&" +
                    RETRIEVED_SESSION_PARAM +
                    "=true" +
                    "#" +
                    urlHashData;
                LOG.debug(
                    "SessionServiceFilter.doFilter(): need to include URL anchor, redirecting to '" +
                    newURI +
                    "'"
                );
                HttpServletResponse response = (HttpServletResponse) aResponse;
                // we have to do a client side redirect so this is reflected in browser location bar for javascript
                response.sendRedirect(newURI);
                return;
            }
            aChain.doFilter(wrapper, aResponse);
        } else {
            aChain.doFilter(aRequest, aResponse);
        }
    }
}
