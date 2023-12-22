package org.cbioportal.documentation;

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

// Retrieve the content of an external page
// Make it auto-scannable
@Controller
public class ExternalPageController {

    // service name: getexternalpage.json
    // available via GET method
    // sourceURL is required
    @Transactional
    @RequestMapping(value = "/api/getexternalpage.json", method = {RequestMethod.GET})
    public @ResponseBody Map<String, String> getExternalPage(@RequestParam(required = true) String sourceURL) throws IOException {
        String decodedString, pageText = "";

        // decode the sourceURL and open a connection
        sourceURL = URLDecoder.decode(sourceURL, "UTF-8");
        URL url = new URL(sourceURL);
        URLConnection connection = url.openConnection();

        // create a reader
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

        // read
        while ((decodedString = in.readLine()) != null) {
            pageText += decodedString + "\n";
        }
        in.close();

        // turn the pageText into a singletonMap for json and return
        return Collections.singletonMap("response", pageText);
    }
}