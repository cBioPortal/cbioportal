package org.cbioportal.web;

import static org.cbioportal.service.FrontendPropertiesServiceImpl.FrontendProperty;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.cbioportal.service.FrontendPropertiesService;
import org.cbioportal.web.util.HttpRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

}
