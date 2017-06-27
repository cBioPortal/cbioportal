package org.cbioportal.web;

import io.swagger.annotations.Api;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.cbioportal.web.config.annotation.PublicApi;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@PublicApi
@RestController
@Validated
@Api(tags = "Clinical Trails Data", description = " ")
public class ClinicalTrialsController {

    private static final Logger logger = LoggerFactory.getLogger(ClinicalTrialsController.class);

    private String molecularMatchURL;

    @Value("${mm.url:https://api.molecularmatch.com/v1/search/trials}")
    public void setMolecularMatchURL(String property) {
        this.molecularMatchURL = property;
    }


    @RequestMapping(value = "/molecularmatch", method = RequestMethod.POST)
    public HashMap<String, Integer> getMolecularMatchClinicalTrials(@RequestParam String filters, HttpMethod method) throws URISyntaxException {

        String apiKey = "539188c9-0516-47c4-b3a6-98b4b5524dc8";
        Map payload = new HashedMap();
        payload.put("apiKey", apiKey);
        payload.put("filters", filters);

        try {
            RestTemplate restTemplate = new RestTemplate();
            URI uri = new URI(molecularMatchURL);

            JSONObject resp = restTemplate.postForObject(uri, payload, JSONObject.class);

            JSONArray filtersArr = (JSONArray) new JSONParser().parse(filters);
            HashMap<String, Integer> trialCount = new HashMap<>();

            ArrayList<LinkedHashMap<String, ArrayList<String>>> trials = (ArrayList<LinkedHashMap<String, ArrayList<String>>>) resp.get("trials");

            for (Object obj : filtersArr) {
                String facet = (String) ((JSONObject) obj).get("facet");
                if (StringUtils.equals(facet, "MUTATION")) {
                    String searchMutation = (String) ((JSONObject) obj).get("term");

                    for (LinkedHashMap<String, ArrayList<String>> trial : trials) {
                        ArrayList<String> mutations = trial.get("molecularAlterations");
                        if (mutations.contains(searchMutation)) {
                            if (trialCount.containsKey(searchMutation)) {
                                int count = trialCount.get(searchMutation);
                                trialCount.put(searchMutation, count + 1);
                            } else {
                                trialCount.put(searchMutation, 1);
                            }
                        }
                    }
                }
            }
            return trialCount;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

