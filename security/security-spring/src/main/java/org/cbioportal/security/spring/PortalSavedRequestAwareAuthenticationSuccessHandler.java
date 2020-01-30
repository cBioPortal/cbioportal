/*
 * Copyright (c) 2017 Memorial Sloan-Kettering Cancer Center.
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
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

package org.cbioportal.security.spring;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A reimplementation of SavedRequestAwareAuthenticationSuccessHandler that checks for
 * targetUrlParameter on the SavedRequest rather than on the current request.
 *
 * @author Manda Wilson
 * @see <a href="https://github.com/spring-projects/spring-security/blob/master/web/src/main/java/org/springframework/security/web/authentication/SavedRequestAwareAuthenticationSuccessHandler.java">SavedRequestAwareAuthenticationSuccessHandler</a>
 */
class PortalSavedRequestAwareAuthenticationSuccessHandler
    extends SimpleUrlAuthenticationSuccessHandler {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    )
        throws ServletException, IOException {
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (savedRequest == null) {
            super.onAuthenticationSuccess(request, response, authentication);

            return;
        }

        if (isAlwaysUseDefaultTargetUrl()) {
            requestCache.removeRequest(request, response);
            super.onAuthenticationSuccess(request, response, authentication);

            return;
        }

        String targetUrlParameter = getTargetUrlParameter();
        String targetUrl = null;
        String[] targetUrlParameterValues = savedRequest.getParameterValues(
            targetUrlParameter
        );

        if (
            targetUrlParameter != null &&
            targetUrlParameterValues != null &&
            targetUrlParameterValues.length > 0 &&
            StringUtils.hasText(targetUrlParameterValues[0])
        ) {
            requestCache.removeRequest(request, response);
            // force this to be relative
            targetUrl =
                getRelativeURI(
                    request,
                    savedRequest.getParameterValues(targetUrlParameter)[0]
                );
            Assert.notNull(
                targetUrl,
                "'" +
                savedRequest.getParameterValues(targetUrlParameter)[0] +
                "' could not be turned into a valid relative URI"
            );
            Assert.isTrue(
                StringUtils.hasText(targetUrl),
                "'" +
                savedRequest.getParameterValues(targetUrlParameter)[0] +
                "' could not be turned into a valid relative URI"
            );
        } else {
            clearAuthenticationAttributes(request);
            // Use the DefaultSavedRequest URL
            targetUrl = savedRequest.getRedirectUrl();
        }

        logger.debug("Redirecting to Url: " + targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }

    private String getRelativeURI(
        HttpServletRequest request,
        String targetURI
    ) {
        String relativeURI = null;
        try {
            URI originalURI = new URI(targetURI);
            logger.debug(
                "getRelativeURI(): request.getServletContext() = '" +
                request.getServletContext() +
                "'"
            );
            logger.debug(
                "getRelativeURI(): testing '" +
                new URI(request.getContextPath()) +
                "'"
            );
            // URI(String scheme, String authority, String path, String query, String fragment)
            // use relativize so we do not include context path e.g. /cbioportal/
            // use resolve to make sure we have a "/" at the front
            relativeURI =
                new URI("/")
                    .resolve(
                        new URI(request.getContextPath())
                        .relativize(
                                new URI(
                                    null,
                                    null,
                                    originalURI.getRawPath(),
                                    originalURI.getRawQuery(),
                                    originalURI.getRawFragment()
                                )
                            )
                    )
                    .toString();
            logger.debug(
                "getRelativeURI(): changing '" +
                targetURI +
                "' to '" +
                relativeURI +
                "'"
            );
        } catch (URISyntaxException e) {
            return null;
        }
        return relativeURI;
    }
}
