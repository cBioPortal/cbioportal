package org.cbioportal.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Springfox 3.0 doesn't completely respect the path we specify for the swagger UI
 * This redirect puts the api-doc json in the specified path so that the swagger UI works
 * and the frontend can pull the json.
 */
//@Controller
public class DocRedirectController {
    @GetMapping("/api-docs")
    public RedirectView docRedirect(
        @RequestParam(value = "group", defaultValue = "default")
        String group,
        final RedirectAttributes redirectAttributes,
        HttpServletRequest request
    ) {
        redirectAttributes.addAttribute("group", group);
        String baseUrl = getBaseUrl(request, "/api/api-docs");
        return new RedirectView(baseUrl + "/api/v2/api-docs");
    }

    @GetMapping({"/", "/swagger-ui.html"})
    public RedirectView swaggerUIRedirect(
        @RequestParam(value = "group", defaultValue = "default")
        String group,
        final RedirectAttributes redirectAttributes,
        HttpServletRequest request
    ) {
        redirectAttributes.addAttribute("group", group);
        String baseUrl = getBaseUrl(request, "/api/(|swagger-ui.html)");
        return new RedirectView(baseUrl + "/api/swagger-ui/index.html");
    }
    
    private String getBaseUrl(HttpServletRequest request, String suffix) {
        Pattern p = Pattern.compile("^(.*)(" + suffix + ")$");
        Matcher m = p.matcher(request.getRequestURI());
        return m.find() ? m.group(1) : "";
    }
}
