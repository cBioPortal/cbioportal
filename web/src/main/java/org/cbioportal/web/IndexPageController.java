package org.cbioportal.web;

import static org.cbioportal.service.FrontendPropertiesServiceImpl.FrontendProperty;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.cbioportal.service.FrontendPropertiesService;
import org.cbioportal.web.util.HttpRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexPageController {

    @Autowired
    private FrontendPropertiesService frontendPropertiesService;
    
    @Autowired
    private HttpRequestUtils requestUtils;
    
    @Value("${authenticate}")
    private String[] authenticate;
    
    @Value("${saml.idp.metadata.entityid:not_defined_in_portalproperties}")
    private String samlIdpEntityId;

    private ObjectMapper mapper = new ObjectMapper();

    @GetMapping({"/", "/index", "/index.html"})
    public String showIndexPage(HttpServletRequest request, Authentication authentication, Model model)
        throws JsonProcessingException {

        String baseUrl = requestUtils.getBaseUrl(request);
        
        Map<String, String> properties = frontendPropertiesService.getFrontendProperties();
        properties.put("base_url", baseUrl);
        properties.put("user_email_address", authentication != null ? authentication.getName() : "anonymousUser");
        
        model.addAttribute("propertiesJson", mapper.writeValueAsString(properties));
        model.addAttribute("frontendUrl", frontendPropertiesService.getFrontendProperty(FrontendProperty.frontendUrl));
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("contextPath", request.getContextPath());
        model.addAttribute("appVersion", frontendPropertiesService.getFrontendProperty(FrontendProperty.app_version));
        model.addAttribute("postData", requestUtils.getPostData(request));

        return "index";
    }

    @GetMapping("/login")
    public String showLoginPage(HttpServletRequest request, Authentication authentication, Model model) {
    
        model.addAttribute("skin_title", frontendPropertiesService.getFrontendProperty(FrontendProperty.skin_title));
        model.addAttribute("skin_authorization_message", frontendPropertiesService.getFrontendProperty(FrontendProperty.skin_authorization_message));
        model.addAttribute("skin_login_contact_html", frontendPropertiesService.getFrontendProperty(FrontendProperty.skin_login_contact_html));
        model.addAttribute("skin_login_saml_registration_html", frontendPropertiesService.getFrontendProperty(FrontendProperty.skin_login_saml_registration_html));
        model.addAttribute("saml_idp_metadata_entityid", frontendPropertiesService.getFrontendProperty(FrontendProperty.saml_idp_metadata_entityid));
        model.addAttribute("logout_success", Boolean.parseBoolean(request.getParameter("logout_success")));
        model.addAttribute("login_error", Boolean.parseBoolean(request.getParameter("login_error")));
        model.addAttribute("authentication_method", frontendPropertiesService.getFrontendProperty(FrontendProperty.authenticationMethod));
        model.addAttribute("show_google", Arrays.asList(authenticate).contains("social_auth") || Arrays.asList(authenticate).contains("social_auth_google") );
        model.addAttribute("show_microsoft", Arrays.asList(authenticate).contains("social_auth_microsoft"));

        switch (authenticate[0]) {
            case "openid":
                return "login_openid";
            case "ad":
            case "ldap":
                return "login_ad";
            case "saml":
                model.addAttribute("skin_login_saml_registration_html", frontendPropertiesService.getFrontendProperty(FrontendProperty.skin_login_saml_registration_html));
                model.addAttribute("saml_idp_entity_id", samlIdpEntityId);
                return "login_saml"; 
            default:
                return "login_new";
        }

        
    }

    public FrontendPropertiesService getFrontendPropertiesService() {
        return frontendPropertiesService;
    }
}
