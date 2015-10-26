package org.mskcc.cbio.portal.documentation;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Map;

@Controller
public class MarkdownPageController {
    @Transactional
//    @RequestMapping(value = "/getmarkdownpage.json", method = {RequestMethod.GET}, produces={"application/xml", "application/json"})
    @RequestMapping(value = "/getmarkdownpage.json", method = {RequestMethod.GET})
    public @ResponseBody
    Map<String, String> getCancerTypes(@RequestParam(required = true) String sourceURL) throws IOException {
        sourceURL = URLDecoder.decode(sourceURL, "UTF-8");

        URL url = new URL(sourceURL);
        URLConnection connection = url.openConnection();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String decodedString, markdownText = "";
        while ((decodedString = in.readLine()) != null) {
            markdownText += decodedString + "\n";
        }
        in.close();

        return Collections.singletonMap("response", markdownText);
//        return markdownText;
    }
}