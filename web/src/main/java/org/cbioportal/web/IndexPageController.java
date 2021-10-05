package org.cbioportal.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.cbioportal.service.PropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexPageController {

    @Autowired
    private PropertiesService propertiesService;

    private ObjectMapper mapper = new ObjectMapper();

    @GetMapping({"/", "/index", "/index.html"})
    public String showIndexPage(HttpServletRequest request, Authentication authentication, Model model)
        throws JsonProcessingException {

        String currentUrl = request.getRequestURL().toString();
        String contextPath = request.getContextPath();
        String baseURL = currentUrl.substring(0, currentUrl.length() - request.getRequestURI().length()) + contextPath;
        baseURL = baseURL.replace("https://", "").replace("http://", "");
        String username = authentication != null ? authentication.getName() : "anonymousUser";
        
        Map<String, String> properties = propertiesService.getFrontendProperties(baseURL, username);
        model.addAttribute("propertiesJson", mapper.writeValueAsString(properties));
        model.addAttribute("frontendUrl", propertiesService.getFrontendUrl());
        model.addAttribute("baseUrl", baseURL);
        model.addAttribute("contextPath", contextPath);
        model.addAttribute("appVersion", propertiesService.getFrontendProperty("app.version"));
        return "index";
    }

}
