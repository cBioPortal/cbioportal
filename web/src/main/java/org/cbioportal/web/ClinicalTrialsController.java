package org.cbioportal.web;

import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.cbioportal.web.config.annotation.PublicApi;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

@PublicApi
@RestController
@Validated
@Api(tags = "Clinical Trial Data", description = "integrate MolecularMatch to retrieve clinical trials")
public class ClinicalTrialsController {

    private static final Logger logger = LoggerFactory.getLogger(ClinicalTrialsController.class);

    private String molecularMatchURL;

    @Value("${mm.url:https://api.molecularmatch.com/v1/search/trials}")
    public void setMolecularMatchURL(String property) {

        this.molecularMatchURL = property;
    }


    @RequestMapping(value = "/molecularmatch", method = RequestMethod.POST)
    public String getMolecularMatchClinicalTrials(@RequestBody String filters, HttpMethod method) throws
        URISyntaxException {

        String apiKey = "539188c9-0516-47c4-b3a6-98b4b5524dc8";

        try {
            JSONArray filtersArr = (JSONArray) new JSONParser().parse(filters);
            HashMap<String, Integer> trialCount = new HashMap<>();
            URI uri = new URI(molecularMatchURL);

            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

            HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

            requestFactory.setHttpClient(httpClient);
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

                public boolean verify(String hostname, SSLSession session) {

                    return true;
                }
            });

            TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, UNQUESTIONING_TRUST_MANAGER, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            RestTemplate restTemplate = new RestTemplate(requestFactory);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject map = new JSONObject();
            map.put("apiKey", apiKey);
            map.put("filters", filtersArr);

            HttpEntity<JSONObject> request = new HttpEntity<JSONObject>(map, headers);
            ResponseEntity<JSONObject> respEntity =
                restTemplate.exchange(uri, HttpMethod.POST, request, JSONObject.class);
            JSONObject resp = respEntity.getBody();

            ArrayList<LinkedHashMap<String, ArrayList<String>>> trials =
                (ArrayList<LinkedHashMap<String, ArrayList<String>>>) resp.get("trials");

            JSONArray response = new JSONArray();
            for (Object obj : filtersArr) {
                String facet = (String) ((JSONObject) obj).get("facet");
                if (StringUtils.equals(facet, "MUTATION")) {
                    String searchMutation = ((String) ((JSONObject) obj).get("term")).split(" ")[0];

                    JSONObject object = new JSONObject();
                    JSONArray searchResults = new JSONArray();
                    for (LinkedHashMap<String, ArrayList<String>> trial : trials) {
                        ArrayList<String> mutations = trial.get("molecularAlterations");
                        if (mutations.contains(searchMutation)) {
                            JSONObject topTrial = new JSONObject();
                            if (trialCount.containsKey(searchMutation)) {
                                int count = trialCount.get(searchMutation);
                                trialCount.put(searchMutation, count + 1);
                                //get best 5 trials and their details
                                if (count < 5) {
                                    topTrial.put("id", trial.get("id"));
                                    topTrial.put("briefTitle", trial.get("briefTitle"));
                                    topTrial.put("phase", trial.get("phase"));

                                    searchResults.add(topTrial);
                                }
                            } else {
                                trialCount.put(searchMutation, 1);
                                topTrial.put("id", trial.get("id"));
                                topTrial.put("briefTitle", trial.get("briefTitle"));
                                topTrial.put("phase", trial.get("phase"));
                                searchResults.add(topTrial);
                            }
                        }
                    }
                    object.put("mutation", searchMutation);
                    if (trialCount.get(searchMutation) != null) {
                        object.put("count", trialCount.get(searchMutation));
                    } else {
                        object.put("count", 0);
                    }
                    object.put("trials", searchResults);
                    response.add(object);
                }
            }
            return response.toJSONString();

        } catch (Exception ex) {
            logger.error("Error occurred while retrieving clinical trials", ex);
        }
        return null;
    }
}

