package org.cbioportal.web;

import static org.cbioportal.service.FrontendPropertiesServiceImpl.FrontendProperty;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.servlet.http.HttpServletRequest;
import org.cbioportal.service.FrontendPropertiesService;
import org.cbioportal.web.util.HttpRequestUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    private final ObjectMapper mapper = new ObjectMapper();

    private Map<String, String> getFrontendProperties(HttpServletRequest request, Authentication authentication) {
        String baseUrl = requestUtils.getBaseUrl(request);
        Map<String, String> properties = frontendPropertiesService.getFrontendProperties();
        properties.put("base_url", baseUrl);
        properties.put("user_email_address", authentication != null ? authentication.getName(): "anonymousUser");
        properties.put("user_display_name", authentication != null ? authentication.getName(): "anonymousUser");
        return properties;
    }
    
    @GetMapping({"/", "/index", "/index.html"})
    public String showIndexPage(HttpServletRequest request, Authentication authentication, Model model)
        throws JsonProcessingException {

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(String.class, new CustomFrontendPropertiesSerializer());
        mapper.registerModule(simpleModule);

        String baseUrl = requestUtils.getBaseUrl(request); 
        JSONObject postData = requestUtils.getPostData(request);
        
        model.addAttribute("propertiesJson", mapper.writeValueAsString(getFrontendProperties(request, authentication)));
        model.addAttribute("frontendUrl", frontendPropertiesService.getFrontendProperty(FrontendProperty.frontendUrl));
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("contextPath", request.getContextPath());
        model.addAttribute("appVersion", frontendPropertiesService.getFrontendProperty(FrontendProperty.app_version));
        model.addAttribute("postData", postData.isEmpty() ? "undefined" : postData);

        return "index";
    }

    @GetMapping(value = "/login.jsp", produces = MediaType.APPLICATION_JSON_VALUE)
    public String showLoginPage(HttpServletRequest request, Authentication authentication, Model model) {
    
        model.addAttribute("skin_title", frontendPropertiesService.getFrontendProperty(FrontendProperty.skin_title));
        model.addAttribute("skin_authorization_message", frontendPropertiesService.getFrontendProperty(FrontendProperty.skin_authorization_message));
        model.addAttribute("skin_login_contact_html", frontendPropertiesService.getFrontendProperty(FrontendProperty.skin_login_contact_html));
        model.addAttribute("skin_login_saml_registration_html", frontendPropertiesService.getFrontendProperty(FrontendProperty.skin_login_saml_registration_html));
        model.addAttribute("saml_idp_metadata_entityid", frontendPropertiesService.getFrontendProperty(FrontendProperty.saml_idp_metadata_entityid));
        model.addAttribute("logout_success", Boolean.parseBoolean(request.getParameter("logout_success")));
        model.addAttribute("login_error", Boolean.parseBoolean(request.getParameter("login_error")));
        model.addAttribute("show_saml", frontendPropertiesService.getFrontendProperty(FrontendProperty.authenticationMethod).equals("saml"));
        model.addAttribute("show_google", Arrays.asList(authenticate).contains("social_auth") || Arrays.asList(authenticate).contains("social_auth_google") );
        model.addAttribute("show_microsoft", Arrays.asList(authenticate).contains("social_auth_microsoft"));
        
        return "login";
    }
    
    @GetMapping("/config_service")
    public ResponseEntity<?> getConfigService(HttpServletRequest request, Authentication authentication) {
        return ResponseEntity.ok(getFrontendProperties(request, authentication));
    }

    public FrontendPropertiesService getFrontendPropertiesService() {
        return frontendPropertiesService;
    }

    public static class CustomFrontendPropertiesSerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase((value))) {
                jsonGenerator.writeBoolean("true".equalsIgnoreCase(value));
            } else if (value != null) {
                jsonGenerator.writeString(value);
            } else {
                jsonGenerator.writeNull();
            }
        }
    }
}
