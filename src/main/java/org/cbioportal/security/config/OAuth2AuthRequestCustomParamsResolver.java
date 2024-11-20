package org.cbioportal.security.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OAuth2AuthRequestCustomParamsResolver implements OAuth2AuthorizationRequestResolver {
    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthRequestCustomParamsResolver.class);

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    // @Autowired
    // private OAuthCustomRequestParams authParams;
    //TODO Try making it dynamic by using the above (auto-expanded config map key:value)
    
    @Value("${security.custom.oauth.request.acr:}")
    private String acr_value;

    public OAuth2AuthRequestCustomParamsResolver(ClientRegistrationRepository repo) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return authorizationRequest != null ? customizeAuthorizationRequest(authorizationRequest) : null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return authorizationRequest != null ? customizeAuthorizationRequest(authorizationRequest) : null;
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
        OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.from(authorizationRequest);

        // TODO: more flexible way
        // Map<String, String> params = authParams.getParams();
        // // Add custom parameters from authParams
        // if (params != null) {
        //     Map<String, Object> additionalParams = new HashMap<>(authorizationRequest.getAdditionalParameters());
        //     additionalParams.putAll(params);
        //     builder.additionalParameters(additionalParams);
        // } else 
        
        if (acr_value != null && !acr_value.equals("")) {
            Map<String, Object> additionalParams = new HashMap<>(authorizationRequest.getAdditionalParameters());
            additionalParams.put("acr_value", acr_value);
            builder.additionalParameters(additionalParams);
        }

        return builder.build();
    }
}