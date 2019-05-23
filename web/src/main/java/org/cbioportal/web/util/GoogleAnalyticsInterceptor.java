package org.cbioportal.web.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class GoogleAnalyticsInterceptor extends HandlerInterceptorAdapter {

    private static final HttpClient CLIENT = HttpClientBuilder.create()
            .setDefaultHeaders(Arrays.asList(new BasicHeader("Accept", "*/*"),
                    new BasicHeader("Accept-Language", "en-US,en;q=0.5"),
                    new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"),
                    new BasicHeader("User-Agent", "Cbioportal API Reporting/1.0 (X11; Linux x86_64)")))
            .build();
    private static ArrayList<BasicNameValuePair> defaultParams;
    private static URIBuilder uriBuilder;

    @Value("${google_analytics_application_client_id:}")
    private String clientId;

    @Value("${google_analytics_tracking_code_api:}")
    private String trackingId;

    public GoogleAnalyticsInterceptor() {
        try {
            uriBuilder = new URIBuilder("https://www.google-analytics.com/collect");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        defaultParams = new ArrayList<>(Arrays.asList(new BasicNameValuePair("v", "1"),
                new BasicNameValuePair("t", "pageview"), new BasicNameValuePair("tid", trackingId),
                new BasicNameValuePair("cid", clientId), new BasicNameValuePair("dh", "cbioportal.org")));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        System.out.println("AfterCompletion fired");
        if (trackingId == null || clientId == null || response.getHeader("referer") != null) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.addAll(defaultParams);
            nvps.addAll(Arrays.asList(new BasicNameValuePair("dp", request.getRequestURI()),
                    new BasicNameValuePair("dt", "")));
            uriBuilder.setParameters(nvps);
            try {
                HttpPost post = new HttpPost(uriBuilder.build());
                CLIENT.execute(post);
                System.out.println("GA request fired");
            } catch (IOException | URISyntaxException e) {
                System.out.println("Error sending hit to Google Analytics: ");
                e.printStackTrace();
            }
        });
    }

}