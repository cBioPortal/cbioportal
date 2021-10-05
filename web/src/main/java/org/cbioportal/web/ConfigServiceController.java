package org.cbioportal.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.MalformedURLException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.cbioportal.service.PropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConfigServiceController {

    @Autowired
    private PropertiesService propertiesService;

    private ObjectMapper mapper = new ObjectMapper();

    @GetMapping({"/config_service"})
    public String getFrontedProperties(HttpServletRequest request, Authentication authentication, Model model)
        throws JsonProcessingException, MalformedURLException {
    
        Map<String, String> properties = propertiesService.getFrontendProperties(request.getRequestURI(), authentication.getName());
        
        // TODO
        //  obj.put("enable_darwin", CheckDarwinAccessServlet.CheckDarwinAccess.existsDarwinProperties()); 
        
        String currentUrl = request.getRequestURL().toString();
        String baseURL = currentUrl.substring(0, currentUrl.length() - request.getRequestURI().length()) + request.getContextPath();
        baseURL = baseURL.replace("https://", "").replace("http://", "");
        properties.put("base_url", baseURL);

        properties.put("user_email_address", authentication != null ? authentication.getName() : "anonymousUser");

        model.addAttribute("propertiesJson", mapper.writeValueAsString(properties));
        return "config_service";
//        return new ResponseEntity<Map<String, String>>(properties, HttpStatus.OK);
    }

}
