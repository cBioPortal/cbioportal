package org.cbioportal.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexPageController {

    @GetMapping({"/", "/index", "/index.html"})
    public String showIndexPage(Model model) {
        return "index";
    }

}
