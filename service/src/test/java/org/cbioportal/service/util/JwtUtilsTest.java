/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
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
 * Copyright 2002-2018 the original author or authors.
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

package org.cbioportal.service.util;
//TODO package org.cbioportal.security.spring.authentication.token;

import java.util.*;
import org.cbioportal.service.exception.InvalidDataAccessTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@TestPropertySource(
    properties = { "dat.jwt.secret_key = +NbopXzb/AIQNrVEGzxzP5CF42e5drvrXTQot3gfW/s=",
                    "dat.ttl_seconds = 2"
    },
    inheritLocations = false
)
@ContextConfiguration(classes=JwtUtilsTestConfiguration.class)
@RunWith(SpringRunner.class)
public class JwtUtilsTest {

    /* Several tests in this class rely on proper singing of tokens using the key embedded in the TestPropertySource annotation above. If that value is changed, the expected values should also be recomputed.
     */
    @Autowired
    private JwtUtils jwtUtils;

    private static final String TEST_SUBJECT = "testSubject";
    private static final long TEST_TOKEN_EXPIRATION_MILLISECONDS = 2000L;

    @Test
    public void createTokenTest() {
        String token = jwtUtils.createToken(TEST_SUBJECT).getToken();
        if (token.isEmpty()) {
            Assert.fail("token was empty");
        }
        if (!token.matches("^\\S+\\.\\S+\\.\\S+$")) {
            Assert.fail("generated token does not have proper format");
        }
    }

    @Test(expected = IllegalArgumentException.class) 
    public void createInvalidTokenTest() {
        String token = jwtUtils.createToken("").getToken();
    }

    @Test
    public void validateValidTokenTest() throws InvalidDataAccessTokenException {
        String token = jwtUtils.createToken(TEST_SUBJECT).getToken();
        jwtUtils.validate(token); // when token is valid, there will be no exception thrown
    }

    @Test(expected = InvalidDataAccessTokenException.class)
    public void validateBadSignatureTokenTest() throws InvalidDataAccessTokenException {
        String token = jwtUtils.createToken(TEST_SUBJECT).getToken();
        int finalDividerIndex = token.lastIndexOf(".");
        String badSignature = "";
        for (int i = 0; i < token.length() - finalDividerIndex - 1; i++) {
            badSignature = badSignature + "A";
        }
        String badSignatureToken = token.substring(0, finalDividerIndex + 1) + badSignature;
        jwtUtils.validate(badSignatureToken);
    }

    @Test(expected = InvalidDataAccessTokenException.class)
    public void validateExpiredTokenTest() throws InvalidDataAccessTokenException, InterruptedException {
        String token = jwtUtils.createToken(TEST_SUBJECT).getToken();
        Thread.sleep(TEST_TOKEN_EXPIRATION_MILLISECONDS + 10L); // NOTE: sleep time must be adequate to allow created token to expire
        jwtUtils.validate(token);
    }

    @Test
    public void extractSubjectTest() throws InvalidDataAccessTokenException {
        String token = jwtUtils.createToken(TEST_SUBJECT).getToken();
        String extractedSubject = jwtUtils.extractSubject(token);
        if (extractedSubject.isEmpty() || !extractedSubject.equals(TEST_SUBJECT)) {
            Assert.fail("extracted subject does not match expected value");
        }
    }

    @Test
    public void extractExpirationDateTest() throws InvalidDataAccessTokenException {
        Date now = new Date();
        long nowTime = now.getTime();
        String token = jwtUtils.createToken(TEST_SUBJECT).getToken();
        Date extractedExpirationDate = jwtUtils.extractExpirationDate(token);
        long expirationTime = extractedExpirationDate.getTime();
        long timeDifference = expirationTime - nowTime;
        if (extractedExpirationDate == null || timeDifference > TEST_TOKEN_EXPIRATION_MILLISECONDS + 10L || timeDifference < 0) {
            Assert.fail("extracted expiration date is not in the expected range");
        }
    }

    @Test
    public void extractPropertiesTest() throws InvalidDataAccessTokenException {
        String token = jwtUtils.createToken(TEST_SUBJECT).getToken();
        Map<String, Object> extractedProperties = jwtUtils.extractProperties(token);
        if (extractedProperties == null || extractedProperties.keySet().size() < 3) {
            Assert.fail("extracted properties is not large enough (at least 3 keys were expected)");
        }
    }

}
