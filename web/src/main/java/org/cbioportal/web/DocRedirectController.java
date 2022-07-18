package org.cbioportal.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

/**
 * Springfox 3.0 doesn't completely respect the path we specify for the swagger UI
 * This redirect puts the api-doc json in the specified path so that the swagger UI works
 * and the frontend can pull the json.
 */
@Controller
public class DocRedirectController {
    @GetMapping("/api-docs")
    public RedirectView redirectWithUsingRedirectView(
        @RequestParam(value = "group", defaultValue = "default")
        String group,
        final RedirectAttributes redirectAttributes,
        HttpServletRequest request
    ) {
        redirectAttributes.addAttribute("group", group);
        return new RedirectView("/api/v2/api-docs");
    }
}
