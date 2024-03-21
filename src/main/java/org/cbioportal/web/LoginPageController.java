package org.cbioportal.web;

import jakarta.servlet.http.HttpServletRequest;
import org.cbioportal.service.FrontendPropertiesService;
import org.cbioportal.service.FrontendPropertiesServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
@ConditionalOnExpression("{'oauth2','saml','optional_oauth2'}.contains('${authenticate}')")
public class LoginPageController {
    private static final Logger log = LoggerFactory.getLogger(LoginPageController.class);

    @Autowired
    private FrontendPropertiesService frontendPropertiesService;

    @Autowired(required = false)
    private InMemoryClientRegistrationRepository clientRegistrationRepository;

    @Value("${authenticate}")
    private String authenticate;
    
    @Value("${security.force_redirect_on_one_idp:true}")
    private boolean forceRedirectOnOneIdentityProvider;

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public String showLoginPagePost(HttpServletRequest request, Authentication authentication, Model model) {
        populateModel(request, model, new HashMap<>());
        return "login";
    }
    
    @GetMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public String showLoginPage(HttpServletRequest request, Authentication authentication, Model model) {
        Map<String, String> oauth2AuthenticationUrls = getOauth2AuthenticationUrls();
        boolean logoutFailure = request.getParameterMap().containsKey("logout_failure");
        if(oauth2AuthenticationUrls.size() == 1 && !logoutFailure && this.forceRedirectOnOneIdentityProvider) {
            log.debug("Redirecting to Identity Provider");
            return "redirect:" + oauth2AuthenticationUrls.values().iterator().next();
        }
        populateModel(request, model, oauth2AuthenticationUrls);

        return "login";
    }
    private void populateModel(HttpServletRequest request, Model model, Map<String, String> oauth2AuthenticationUrls) {
        model.addAttribute("oauth_urls", oauth2AuthenticationUrls);
        model.addAttribute("skin_title", frontendPropertiesService.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.skin_title));
        model.addAttribute("skin_authorization_message", frontendPropertiesService.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.skin_authorization_message));
        model.addAttribute("skin_login_contact_html", frontendPropertiesService.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.skin_login_contact_html));
        model.addAttribute("skin_login_saml_registration_html", frontendPropertiesService.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.skin_login_saml_registration_html));
        model.addAttribute("saml_idp_metadata_entityid", frontendPropertiesService.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.saml_idp_metadata_entityid));
        model.addAttribute("logout_success", request.getParameterMap().containsKey("logout_success"));
        model.addAttribute("login_error", request.getParameterMap().containsKey("logout_failure"));
        model.addAttribute("show_saml", frontendPropertiesService.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.authenticationMethod).equals("saml"));
    }
    
    private Map<String, String> getOauth2AuthenticationUrls() {
        Map<String, String> oauth2AuthenticationUrls = new HashMap<>();
        if(!Objects.isNull(clientRegistrationRepository) && !Objects.equals(authenticate, "saml")) {
            for (ClientRegistration clientRegistration : clientRegistrationRepository) {
                oauth2AuthenticationUrls.put(clientRegistration.getRegistrationId(),
                    OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/" + clientRegistration.getRegistrationId());
            }
        }
        return oauth2AuthenticationUrls;
    }
}
