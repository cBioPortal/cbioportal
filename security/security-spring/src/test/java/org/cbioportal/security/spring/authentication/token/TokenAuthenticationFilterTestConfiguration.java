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

package org.cbioportal.security.spring.authentication.token;

import org.cbioportal.security.spring.authentication.social.PortalUserDetailsService;
import org.cbioportal.service.DataAccessTokenService;
import org.cbioportal.service.DataAccessTokenServiceFactory;
import org.cbioportal.service.impl.JwtDataAccessTokenServiceImpl;
import org.cbioportal.service.util.JwtUtils;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetails;

@Configuration
public class TokenAuthenticationFilterTestConfiguration {

    public static final String TEST_SUBJECT = "testSubject";

    @Bean ProviderManager authenticationManager() {
        List<AuthenticationProvider> authenticationProviderList = new ArrayList<AuthenticationProvider>();
        PortalUserDetailsService userDetailsService = Mockito.mock(PortalUserDetailsService.class);
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        // See: https://github.com/spring-projects/spring-security/blob/master/core/src/main/java/org/springframework/security/authentication/dao/AbstractUserDetailsAuthenticationProvider.java#L350
        Mockito.when(userDetails.isAccountNonLocked()).thenReturn(true);
        Mockito.when(userDetails.isEnabled()).thenReturn(true);
        Mockito.when(userDetails.isAccountNonExpired()).thenReturn(true);
        Mockito.when(userDetails.isCredentialsNonExpired()).thenReturn(true);
        Mockito.when(userDetails.getUsername()).thenReturn(TEST_SUBJECT);
        Mockito.when(userDetailsService.loadUserByUsername(ArgumentMatchers.anyString())).thenReturn(userDetails);
        TokenUserDetailsAuthenticationProvider authenticationProvider = new TokenUserDetailsAuthenticationProvider(userDetailsService);
        authenticationProviderList.add(authenticationProvider);
        return new ProviderManager(authenticationProviderList);
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }

    @Bean
    public ServiceLocatorFactoryBean tokenServiceFactory() {
        ServiceLocatorFactoryBean factoryBean = new ServiceLocatorFactoryBean();
        factoryBean.setServiceLocatorInterface(DataAccessTokenServiceFactory.class);
        return factoryBean;
    }

    @Bean
    public DataAccessTokenServiceFactory dataAccessTokenServiceFactory() {
        DataAccessTokenServiceFactory factory = Mockito.mock(DataAccessTokenServiceFactory.class);
        Mockito.when(factory.getDataAccessTokenService(ArgumentMatchers.anyString())).thenReturn(tokenService());
        return factory;
    }

    @Bean
    public DataAccessTokenService tokenService() {
        return new JwtDataAccessTokenServiceImpl();
    }

    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils();
    }

    @Bean
    public HttpServletRequest request() {
        return Mockito.mock(HttpServletRequest.class);
    }

    @Bean
    public HttpServletResponse response() {
        return Mockito.mock(HttpServletResponse.class);
    }
}
