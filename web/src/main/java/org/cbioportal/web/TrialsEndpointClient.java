package org.cbioportal.web;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by sathya on 6/21/17.
 */
public class TrialsEndpointClient {


    public static void main(String[] args) throws URISyntaxException, ParseException {

        String apiKey = "539188c9-0516-47c4-b3a6-98b4b5524dc8";
        String fil = "[{\"facet\":\"MUTATION\",\"term\":\"BRAF V600E\"},{\"facet\":\"MUTATION\"," +
            "\"term\":\"MSH3 L503Wfs*5\"}]";
        
        HashMap<String, Integer> trialCount = new HashMap<>(); 
        JSONArray arr = (JSONArray) new JSONParser().parse(fil);

        Map<String, Object> payload = new HashMap<>();
        payload.put("apiKey", apiKey);
        payload.put("filters", new JSONParser().parse(fil));

        try {
            RestTemplate restTemplate = new RestTemplate();
            URI uri = new URI("https://api.molecularmatch.com/v1/search/trials");

            JSONObject resp = restTemplate.postForObject(uri, payload, JSONObject.class);
            ArrayList<LinkedHashMap<String, ArrayList<String>>> trials = (ArrayList<LinkedHashMap<String,ArrayList<String>>>) resp.get("trials");
            
            for(Object obj : arr) {
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
            Iterator<Map.Entry<String, Integer>> it = trialCount.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> entry = it.next();
                System.out.println("Mutation: " + entry.getKey() + "    " + "Trial count: "+ entry.getValue());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //return null;
    }
    }


