package org.cbioportal.web;

import jakarta.servlet.http.HttpServletRequest;
import org.cbioportal.service.FrontendPropertiesService;
import org.cbioportal.service.FrontendPropertiesServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@ConditionalOnProperty(value = "authenticate", havingValue = "saml_plus_basic")
public class SamlAndBasicLoginController {
    
    private final FrontendPropertiesService frontendPropertiesService;
    
    @Autowired
    SamlAndBasicLoginController(FrontendPropertiesService frontendPropertiesService) {
        this.frontendPropertiesService = frontendPropertiesService;
    }
    
    @GetMapping(value = "/restful_login", produces = MediaType.APPLICATION_JSON_VALUE)
    public String showRestfulLoginPage(HttpServletRequest request, Model model) {
        model.addAttribute("skin_title", frontendPropertiesService.getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty.skin_title));
        return "restful_login";
    }
}
