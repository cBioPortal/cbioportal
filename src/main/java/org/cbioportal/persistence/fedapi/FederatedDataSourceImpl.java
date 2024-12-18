package org.cbioportal.persistence.fedapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URIBuilder;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.web.config.CustomObjectMapper;
import org.cbioportal.web.parameter.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FederatedDataSourceImpl implements FederatedDataSource {
    
    private final FederatedDataSourceInfo federatedDataSourceInfo;
    private final ObjectMapper jsonMapper;
    
    public FederatedDataSourceImpl(FederatedDataSourceInfo federatedDataSourceInfo) {
        this.federatedDataSourceInfo = federatedDataSourceInfo;
        jsonMapper = new CustomObjectMapper();
    }
    
    @Override
    public String getName() {
        return federatedDataSourceInfo.getName();
    }
    
    @Override
    public String getBaseUrl() {
        return federatedDataSourceInfo.getBaseUrl();
    }
    
    @Override
    public CompletableFuture<List<ClinicalAttribute>> fetchClinicalAttributes() {
        // TODO: perhaps this should be a GET
        return POST(
            "/clinical-attributes/fetch",
            Map.of(),
            null,
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
    public CompletableFuture<List<ClinicalDataBin>> fetchClinicalDataBinCounts(ClinicalDataBinCountFilter filter) {
        return POST(
            "/clinical-data-bin-counts/fetch",
            Map.of(),
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
            // Build request URL
            var uriBuilder = new URIBuilder(getBaseUrl() + endpoint);
            queryParams.forEach(uriBuilder::addParameter);
            URI uri = uriBuilder.build();

            // Serialize request body
            String payload = data == null ? "" : jsonMapper.writeValueAsString(data);

            // TODO(TEMP): Disable SSL certificate verification
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCertificates = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };

            // Create an SSL context that uses the above trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());

            // Build the HttpClient/HttpRequest objects
            try (HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build()) {
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    // TODO add back timeout
                    // .timeout(Duration.ofSeconds(10))
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
            }
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
