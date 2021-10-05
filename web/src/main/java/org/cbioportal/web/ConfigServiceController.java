package org.cbioportal.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.cbioportal.service.PropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigServiceController {

    @Autowired
    private PropertiesService propertiesService;

    private ObjectMapper mapper = new ObjectMapper();

    @GetMapping({"/config_service"})
    public String getFrontedProperties(HttpServletRequest request, Authentication authentication)
        throws JsonProcessingException {
    
        Map<String, String> properties = propertiesService.getFrontendProperties();
        
        // TODO
        //  obj.put("enable_darwin", CheckDarwinAccessServlet.CheckDarwinAccess.existsDarwinProperties()); 
        
        String currentUrl = request.getRequestURL().toString();
        String baseURL = currentUrl.substring(0, currentUrl.length() - request.getRequestURI().length()) + request.getContextPath();
        baseURL = baseURL.replace("https://", "").replace("http://", "");
        properties.put("base_url", baseURL);

        properties.put("user_email_address", authentication != null ? authentication.getName() : "anonymousUser");

        return mapper.writeValueAsString(properties);
//        return new ResponseEntity<Map<String, String>>(properties, HttpStatus.OK);
    }

}
