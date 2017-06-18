package org.cbioportal.web;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;



@Controller
@RequestMapping("/trials")
public class ClinicalTrialsController {

    private static final Logger logger = LoggerFactory.getLogger(ClinicalTrialsController.class);

    private String molecularMatchURL;

    @Value("${https://api.molecularmatch.com/v1/search/trials}")
    public void setMolecularMatchURL(String property) {
        this.molecularMatchURL = property;
    }


    @RequestMapping(value = "/molecularmatch", method = RequestMethod.POST)
    @ResponseBody
    public String getMolecularMatchClinicalTrials(@RequestBody JSONArray filters, HttpMethod method) throws URISyntaxException {                                                  

        String apiKey = "539188c9-0516-47c4-b3a6-98b4b5524dc8";
        JSONObject payload = new JSONObject();
        payload.put("apiKey", apiKey);
        payload.put("filters", filters);

        try {
            RestTemplate restTemplate = new RestTemplate();
            URI uri = new URI(molecularMatchURL);
            ResponseEntity<String> responseEntity =
                restTemplate.exchange(uri, method, new HttpEntity<JSONObject>(payload), String.class);
            String resp = responseEntity.getBody();
            return resp;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

