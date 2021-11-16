package org.cbioportal.security.spring.authentication.oauth2;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;


import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;

// TODO add tests

/**
 * Spring Security filter that checks the expiration of the OAuth2 access token. When expired,
 * this filter will renew the access token using the refresh token. When token exchange is successful,
 * the user permissions/authorities are retrieved from the user-info endpoint (using the accces token).
 * Finally, the Authentication object for the respective user is updated with possible changes in permissions.
 * This mechanism ensures that update of user permissions is controlled by the lifespan of the access
 * token.
 */
public class AccessTokenRefreshFilter extends GenericFilterBean {

    private static final Logger log = LoggerFactory.getLogger(AccessTokenRefreshFilter.class);
    private static final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService =
        new DefaultOAuth2UserService();

    @Value("#{T(java.time.Duration).ofMinutes(${spring.security.oauth2.allowed-clock-skew:1})}")
    private Duration accessTokenExpiresSkew;

    @Value("${spring.security.oauth2.client.jwt-roles-path:resource_access::cbioportal::roles}")
    private String jwtRolesPath;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain)
        throws IOException, ServletException, OAuth2AuthorizationException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            authentication instanceof OAuth2AuthenticationToken) {

            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            OAuth2AuthorizedClient currentClient = authorizedClientService.loadAuthorizedClient(
                token.getAuthorizedClientRegistrationId(),
                token.getName());

            OAuth2AccessToken currentAccessToken = currentClient.getAccessToken();
            if (currentAccessToken != null && isExpired(currentAccessToken)) {
                log.debug("OAuth2 access token has expired. Refreshing the token using the refresh token");
                OAuth2AccessTokenResponse accessTokenResponse = refreshAccessToken(currentClient);
                if (accessTokenResponse != null && accessTokenResponse.getAccessToken() != null) {

                    log.debug("OAuth2 access token was refreshed.");

                    OAuth2AuthorizedClient updatedClient =
                        getUpdatedClient(currentClient, accessTokenResponse);

                    // Get token with up-to-date user permissions.
                    OAuth2AuthenticationToken authenticationToken =
                        createAuthenticationToken(accessTokenResponse, updatedClient);

                    // Register client with updated access token and user information.
                    updateSecurityContext(updatedClient, authenticationToken);

                } else {
                    log.error("Failed to refresh token for {}", token.getPrincipal().getName());
                }
            }
        }
        // Always pass processing of the request to the next filter.
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private OAuth2AuthorizedClient getUpdatedClient(OAuth2AuthorizedClient currentClient,
                                                    OAuth2AccessTokenResponse accessTokenResponse) {
        OAuth2RefreshToken refreshToken = accessTokenResponse.getRefreshToken() != null ?
            accessTokenResponse.getRefreshToken() :
            currentClient.getRefreshToken();
        return new OAuth2AuthorizedClient(
            currentClient.getClientRegistration(),
            currentClient.getPrincipalName(),
            accessTokenResponse.getAccessToken(),
            refreshToken
        );
    }

    private OAuth2AuthenticationToken createAuthenticationToken(
        OAuth2AccessTokenResponse accessTokenResponse, OAuth2AuthorizedClient updatedClient) {
        OAuth2User newPrincipal = getUserInfo(updatedClient.getClientRegistration(),
            accessTokenResponse.getAccessToken());
        Collection<? extends GrantedAuthority> newStudyPermissions =
            new CBioAuthoritiesMapper(jwtRolesPath).mapAuthorities(newPrincipal.getAuthorities());
        return new OAuth2AuthenticationToken(newPrincipal, newStudyPermissions,
            updatedClient.getClientRegistration().getRegistrationId());
    }

    private void updateSecurityContext(OAuth2AuthorizedClient updatedClient,
                                       OAuth2AuthenticationToken authenticationToken) {
        log.debug("Resetting user Authorization object.");
        authorizedClientService.saveAuthorizedClient(updatedClient, authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private OAuth2AccessTokenResponse refreshAccessToken(OAuth2AuthorizedClient client)
        throws OAuth2AuthorizationException {

        LinkedMultiValueMap<String, String> formParameters =
            new LinkedMultiValueMap<>();
        formParameters.add(OAuth2ParameterNames.GRANT_TYPE,
            AuthorizationGrantType.REFRESH_TOKEN.getValue());
        formParameters.add(OAuth2ParameterNames.REFRESH_TOKEN,
            client.getRefreshToken().getTokenValue());
        formParameters.add(OAuth2ParameterNames.REDIRECT_URI,
            client.getClientRegistration().getRedirectUri());

        RequestEntity<LinkedMultiValueMap<String, String>> requestEntity = RequestEntity
            .post(URI.create(client.getClientRegistration().getProviderDetails().getTokenUri()))
            .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
            .body(formParameters);

        RestTemplate restTemplate =
            tokenExchangeRestTemplate(client.getClientRegistration().getClientId(),
                client.getClientRegistration().getClientSecret());
        ResponseEntity<OAuth2AccessTokenResponse> responseEntity =
            restTemplate.exchange(requestEntity, OAuth2AccessTokenResponse.class);
        return responseEntity.getBody();

    }

    private boolean isExpired(OAuth2AccessToken accessToken) {
        return accessToken.getExpiresAt().isBefore(Instant.now().minus(accessTokenExpiresSkew));
    }

    private RestTemplate tokenExchangeRestTemplate(String clientId, String clientSecret) {
        return new RestTemplateBuilder()
            .additionalMessageConverters(
                new FormHttpMessageConverter(),
                new OAuth2AccessTokenResponseHttpMessageConverter())
            .errorHandler(new OAuth2ErrorResponseErrorHandler())
            .basicAuthentication(clientId, clientSecret)
            .build();
    }

    private OAuth2User getUserInfo(ClientRegistration clientRegistration,
                                   OAuth2AccessToken accessToken) {
        return oAuth2UserService.loadUser(
            new OAuth2UserRequest(clientRegistration, accessToken)
        );
    }

}
