package org.cbioportal.web;

import static org.cbioportal.service.FrontendPropertiesServiceImpl.FrontendProperty;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.cbioportal.service.FrontendPropertiesService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexPageController {

    @Autowired
    private FrontendPropertiesService frontendPropertiesService;

    private ObjectMapper mapper = new ObjectMapper();

    @GetMapping({"/", "/index", "/index.html"})
    public String showIndexPage(HttpServletRequest request, Authentication authentication, Model model)
        throws JsonProcessingException {

        Map<String, String> properties = frontendPropertiesService.getFrontendProperties();
        
        String currentUrl = request.getRequestURL().toString();
        String contextPath = request.getContextPath();
        String baseURL = currentUrl.substring(0, currentUrl.length() - request.getRequestURI().length()) + contextPath;
        baseURL = baseURL.replace("https://", "").replace("http://", "");
        properties.put("base_url", baseURL);
        
        String useremail = authentication != null ? authentication.getName() : "anonymousUser";
        properties.put("user_email_address", useremail);
        
        model.addAttribute("propertiesJson", mapper.writeValueAsString(properties));
        model.addAttribute("frontendUrl", frontendPropertiesService.getFrontendProperty(FrontendProperty.frontendUrl));
        model.addAttribute("baseUrl", baseURL);
        model.addAttribute("contextPath", contextPath);
        model.addAttribute("appVersion", frontendPropertiesService.getFrontendProperty(FrontendProperty.app_version));
        model.addAttribute("postData", getPostData(request));
        return "index";
    }
    
    private String getPostData(HttpServletRequest request) {
        // To support posted query data (when data would exceed URL length),
        // write all post params to json on page where it can be consumed.
        if (request.getMethod().equals("POST")) {
            Enumeration<String> parameterNames = request.getParameterNames();
            JSONObject paramsJson = new JSONObject();
            while (parameterNames.hasMoreElements()) {
                String parameterName = parameterNames.nextElement();
                paramsJson.put(parameterName, request.getParameter(parameterName));
            }
            return paramsJson.toString();
        }
        return "{}";
    }

}
