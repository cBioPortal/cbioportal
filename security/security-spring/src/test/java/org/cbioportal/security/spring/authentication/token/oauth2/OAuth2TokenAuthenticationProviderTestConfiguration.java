/*
 * Copyright (c) 2020 The Hyve B.V.
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

package org.cbioportal.security.spring.authentication.token.oauth2;

import static org.cbioportal.security.spring.authentication.token.oauth2.OAuth2TokenTestUtils.createJwt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OAuth2TokenAuthenticationProviderTestConfiguration {

    @Bean
    public OAuth2TokenAuthenticationProvider provider() {
        return new OAuth2TokenAuthenticationProvider();
    }

    @Bean
    public OAuth2TokenRefreshRestTemplate tokenRefreshRestTemplate() {

        String jsonSuccess = new StringBuilder()
            .append("{")
            .append("\"resource_access\":")
                .append("{")
                    .append("\"cbioportal\":")
                    .append("{")
                        .append("\"roles\": [")
                            .append("\"").append(OAuth2TokenAuthenticationProviderTest.USER_ROLE_1).append("\",")
                            .append("\"").append(OAuth2TokenAuthenticationProviderTest.USER_ROLE_2).append("\"")
                        .append("]")
                    .append("}")
                .append("}")
            .append("}")
            .toString();

            
        String accessTokenSuccess = createJwt(jsonSuccess);
        
        String jsonMalformedJson = new StringBuilder()
            .append("{")
            .append("\"resource_access\":")
                .append("{}")
            .append("}")
            .toString();

        String accessTokenMalformedJson = createJwt(jsonMalformedJson);

        OAuth2TokenRefreshRestTemplate template = mock(OAuth2TokenRefreshRestTemplate.class);
        Mockito.doAnswer(invocation -> {
            String offlineToken = (String) invocation.getArguments()[0];
            if (offlineToken.equals(OAuth2TokenAuthenticationProviderTest.OFFLINE_TOKEN_MALFORMED_JSON)) {
                return accessTokenMalformedJson;
            }
            return accessTokenSuccess;
        }).when(template).getAccessToken(anyString());

        return template;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}