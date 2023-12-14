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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Controller
@ConditionalOnExpression("{'oauth2','saml','optional_oauth2'}.contains('${authenticate}')")
public class LoginPageController {
    private static final Logger log = LoggerFactory.getLogger(LoginPageController.class);

    @Autowired
    private FrontendPropertiesService frontendPropertiesService;

    @Autowired
    InMemoryClientRegistrationRepository clientRegistrationRepository;

    @Value("${authenticate}")
    private String[] authenticate;

    @GetMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public String showLoginPage(HttpServletRequest request, Authentication authentication, Model model) {
        Map<String, String> oauth2AuthenticationUrls = new HashMap<>();
        for (ClientRegistration clientRegistration : clientRegistrationRepository) {
            oauth2AuthenticationUrls.put(clientRegistration.getClientName(),
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/" + clientRegistration.getRegistrationId());
        }


        model.addAttribute("oauth_urls", oauth2AuthenticationUrls);

        model.addAttribute("skin_title", frontendPropertiesService.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.skin_title));
        model.addAttribute("skin_authorization_message", frontendPropertiesService.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.skin_authorization_message));
        model.addAttribute("skin_login_contact_html", frontendPropertiesService.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.skin_login_contact_html));
        model.addAttribute("skin_login_saml_registration_html", frontendPropertiesService.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.skin_login_saml_registration_html));
        model.addAttribute("saml_idp_metadata_entityid", frontendPropertiesService.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.saml_idp_metadata_entityid));
        model.addAttribute("logout_success", Boolean.parseBoolean(request.getParameter("logout_success")));
        model.addAttribute("login_error", Boolean.parseBoolean(request.getParameter("login_error")));
        model.addAttribute("show_saml", frontendPropertiesService.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.authenticationMethod).equals("saml"));
        model.addAttribute("show_google", Arrays.asList(authenticate).contains("social_auth") || Arrays.asList(authenticate).contains("social_auth_google") );
        model.addAttribute("show_microsoft", Arrays.asList(authenticate).contains("social_auth_microsoft"));

        return "login";
    }
}
