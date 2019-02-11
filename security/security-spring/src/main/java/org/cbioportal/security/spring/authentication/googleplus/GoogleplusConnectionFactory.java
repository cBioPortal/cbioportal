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

package org.cbioportal.security.spring.authentication.googleplus;

import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.connect.GoogleAdapter;
import org.springframework.social.google.connect.GoogleServiceProvider;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.google.api.oauth2.OAuth2Operations;

/**
 * @author criscuof
 *
 */
public class GoogleplusConnectionFactory extends OAuth2ConnectionFactory<Google> {

    public GoogleplusConnectionFactory(String clientId, String clientSecret) {
            super("google", new GoogleServiceProvider(clientId, clientSecret),
                            new GoogleAdapter());
    }

    /**
     * modification of original factory class to support using the user's email address as his/her id
     * original method utilized the google id, a numeric string
    */
    @Override
    protected String extractProviderUserId(AccessGrant accessGrant) {
        Google api = ((GoogleServiceProvider)getServiceProvider()).getApi(accessGrant.getAccessToken());
        OAuth2Operations op = api.oauth2Operations();
        return op.getUserinfo().getEmail();
    }

}
