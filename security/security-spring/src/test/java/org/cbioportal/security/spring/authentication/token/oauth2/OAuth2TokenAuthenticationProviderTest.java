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

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@ContextConfiguration(classes = OAuth2TokenAuthenticationProviderTestConfiguration.class)
@RunWith(SpringRunner.class)
public class OAuth2TokenAuthenticationProviderTest {

    public static String USER_ROLE_1 = "dummy_role_1";
    public static String USER_ROLE_2 = "dummy_role_2";
    public static String OFFLINE_TOKEN_VALID = "my_valid_offline_token";
    public static String OFFLINE_TOKEN_MALFORMED_JSON = "dummy_malformed_json_response";

    @Autowired
    private OAuth2TokenAuthenticationProvider provider;

    @Test
    public void testAuthenticateSuccess() {

        Authentication authRequest = new OAuth2BearerAuthenticationToken("my_principal", OFFLINE_TOKEN_VALID);
        Authentication authGranted = provider.authenticate(authRequest);

        String[] authorities = authGranted.getAuthorities().stream()
            .map(a -> a.toString())
            .toArray(String[]::new);

        assertArrayEquals(authorities, new String[] {USER_ROLE_1, USER_ROLE_2});
    }

    @Test(expected = BadCredentialsException.class)
    public void testAuthenticateFailureMalformedJson() {
        Authentication authRequest = new OAuth2BearerAuthenticationToken("my_principal", OFFLINE_TOKEN_MALFORMED_JSON);
        provider.authenticate(authRequest);
    }

}