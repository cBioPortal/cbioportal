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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Manda Wilson
 */
public class SessionServiceRequestWrapper extends HttpServletRequestWrapper {
    public static final String SESSION_ERROR = "session_error";
    public static final String SESSION_ID_PARAM = "session_id";

    private static Log LOG = LogFactory.getLog(
        SessionServiceRequestWrapper.class
    );
    private Map<String, String[]> storedParameters;
    private String sessionId;

    /**
     * If session_id is a request parameter, calls session-service API to retrieve
     * stored session.  Stores SESSION_ERROR as a request attribute if the session is
     * not found, or the session service returns an error.  Stored session parameters
     * override current request parameters.  If session_id is not a request parameter,
     * request behaves as normal.
     *
     * @param request wrapped HttpServletRequest
     */
    public SessionServiceRequestWrapper(final HttpServletRequest request) {
        super(request);
        LOG.debug("new SessionServiceRequestWrapper()");
        sessionId = super.getParameter(SESSION_ID_PARAM);
        LOG.debug(
            "new SessionServiceRequestWrapper(): request parameter '" +
            SESSION_ID_PARAM +
            "' = '" +
            sessionId +
            "'"
        );
        if (!StringUtils.isBlank(sessionId)) {
            LOG.debug(
                "new SessionServiceRequestWrapper(): retrieved parameters = '" +
                storedParameters +
                "'"
            );
            try {
                storedParameters = SessionServiceUtil.getSession(sessionId);
                if (storedParameters == null || storedParameters.size() == 0) {
                    request.setAttribute(
                        SESSION_ERROR,
                        "Session with id '" + sessionId + "' not found."
                    );
                }
            } catch (Exception e) {
                request.setAttribute(
                    SESSION_ERROR,
                    "Session service error. Session with id '" +
                    sessionId +
                    "' not loaded. Try again later.  If problem persists contact site administrator."
                );
            }
        }
    }

    @Override
    public String getParameter(final String name) {
        if (storedParameters != null && !SESSION_ID_PARAM.equals(name)) {
            LOG.debug(
                "SessionServiceRequestWrapper.getParameter(" +
                name +
                "): accessing parameters from stored session with id '" +
                sessionId +
                "'"
            );
            if (storedParameters.containsKey(name)) {
                String value = storedParameters.get(name)[0];
                LOG.debug(
                    "SessionServiceRequestWrapper.getParameter(" +
                    name +
                    "): returning '" +
                    value +
                    "'"
                );
                return value;
            }
            LOG.debug(
                "SessionServiceRequestWrapper.getParameter(" +
                name +
                "): returning null - parameter name not found"
            );
            return null;
        }
        LOG.debug(
            "SessionServiceRequestWrapper.getParameter(" +
            name +
            "): accessing current request parameters"
        );
        return super.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (storedParameters != null) {
            LOG.debug(
                "SessionServiceRequestWrapper.getParameterMap(): accessing parameters from stored session with id '" +
                sessionId +
                "'"
            );
            return Collections.unmodifiableMap(storedParameters);
        }
        LOG.debug(
            "SessionServiceRequestWrapper.getParameterMap(): accessing current request parameters"
        );
        return super.getParameterMap();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (storedParameters != null) {
            LOG.debug(
                "SessionServiceRequestWrapper.getParameterNames(): accessing parameters from stored session with id '" +
                sessionId +
                "'"
            );
            return Collections.enumeration(storedParameters.keySet());
        }
        LOG.debug(
            "SessionServiceRequestWrapper.getParameterNames(): accessing current request parameters"
        );
        return super.getParameterNames();
    }

    @Override
    public String[] getParameterValues(final String name) {
        if (storedParameters != null) {
            LOG.debug(
                "SessionServiceRequestWrapper.getParameterValues(): accessing parameters from stored session with id '" +
                sessionId +
                "'"
            );
            return storedParameters.get(name);
        }
        LOG.debug(
            "SessionServiceRequestWrapper.getParameterValues(): accessing current request parameters"
        );
        return super.getParameterValues(name);
    }
}
