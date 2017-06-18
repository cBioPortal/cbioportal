package org.cbioportal.web;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by sathya on 6/21/17.
 */
public class TrialsEndpointClient {


    public static void main(String[] args) throws URISyntaxException {

        try {
            URL url = new URL("http://localhost:8080/cbioportal-1.6.1/api/trials/molecularmatch");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            JSONArray filters = new JSONArray();
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("facet", "CONDITION");
            jsonObject1.put("term", "Colorectal cancer");
            filters.add(jsonObject1);

            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("facet", "MUTATION");
            jsonObject2.put("term", "BRAF V600E");
            filters.add(jsonObject2);

            JSONObject jsonObject3 = new JSONObject();
            jsonObject2.put("facet", "STATUS");
            jsonObject2.put("term", "Enrolling");
            filters.add(jsonObject3);

            JSONObject jsonObject4 = new JSONObject();
            jsonObject2.put("facet", "TRIALTYPE");
            jsonObject2.put("term", "Interventional");
            filters.add(jsonObject4);

            JSONObject jsonObject5 = new JSONObject();
            jsonObject2.put("facet", "COUNTRY");
            jsonObject2.put("term", "France");
            filters.add(jsonObject5);

            OutputStream os = conn.getOutputStream();
            os.write(filters.toJSONString().getBytes());
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));
            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }
            conn.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
