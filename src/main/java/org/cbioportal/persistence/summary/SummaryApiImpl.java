package org.cbioportal.persistence.summary;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URIBuilder;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.web.config.CustomObjectMapper;
import org.cbioportal.web.parameter.*;

import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SummaryApiImpl implements SummaryApi {
    
    private final SummaryServer summaryServer;
    private final ObjectMapper jsonMapper;
    
    public SummaryApiImpl(SummaryServer summaryServer) {
        this.summaryServer = summaryServer;
        jsonMapper = new CustomObjectMapper();
    }
    
    @Override
    public String getName() {
        return summaryServer.getName();
    }
    
    @Override
    public String getBaseUrl() {
        return summaryServer.getBaseUrl();
    }
    
    @Override
    public List<String> getStudyIds() {
        return summaryServer.getStudyIds();
    }
    
    @Override
    public List<String> getSupportedEndpoints() {
        return summaryServer.getSupportedEndpoints();
    }

    @Override
    public CompletableFuture<List<ClinicalAttribute>> fetchClinicalAttributes(List<String> studyIds, Projection projection) {
        var params = Map.ofEntries(
            Map.entry("projection", projection.toString())
        );
        return POST(
            "/clinical-attributes/fetch",
            params,
            studyIds,
            new TypeReference<List<ClinicalAttribute>>() {}
        );
    }

    @Override
    public CompletableFuture<List<ClinicalDataCountItem>> fetchClinicalDataCounts(ClinicalDataCountFilter filter) {
        return POST(
            "/clinical-data-counts/fetch",
            Map.of(),
            filter,
            new TypeReference<List<ClinicalDataCountItem>>() {}
        );
    }

    @Override
    public CompletableFuture<List<ClinicalDataBin>> fetchClinicalDataBinCounts(ClinicalDataBinCountFilter filter, DataBinMethod dataBinMethod) {
        var params = Map.ofEntries(
            Map.entry("dataBinMethod", dataBinMethod.toString())
        );
        return POST(
            "/clinical-data-bin-counts/fetch",
            params,
            filter,
            new TypeReference<List<ClinicalDataBin>>() {}
        );
    }
    
    private <T> CompletableFuture<T> POST(
        String endpoint,
        Map<String, String> queryParams,
        Object data,
        TypeReference<T> responseType
    ) {
        try {
            // Only issue the request if this server supports the endpoint
            if (!getSupportedEndpoints().contains(endpoint)) {
                throw new IllegalArgumentException("Unsupported endpoint: " + endpoint);
            }

            // Build request URL
            var uriBuilder = new URIBuilder(getBaseUrl() + endpoint);
            queryParams.forEach(uriBuilder::addParameter);
            URI uri = uriBuilder.build();

            // Serialize request body
            String payload = jsonMapper.writeValueAsString(data);

            // Build the HttpClient/HttpRequest objects
            HttpClient client = HttpClient.newHttpClient(); // TODO dispose after use?
            HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(payload))
                .build();

            // Send the request asynchronously and try to serialize it into the provided response type
            System.out.println("POST " + uri);
            System.out.println("Request body: " + payload);
            return client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(resp -> {
                    try {
                        String body = resp.body();
                        System.out.println("Response body: " + body);
                        return jsonMapper.readValue(body, responseType);
                    } catch (Exception e) {
                        throw new RuntimeException("Could not parse response JSON from " + endpoint, e);
                    }
                });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
