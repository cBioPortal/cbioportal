package org.cbioportal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.service.CustomDataService;
import org.cbioportal.service.util.CustomDataSession;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.utils.removeme.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CustomDataServiceImpl implements CustomDataService {
    @Autowired
    private SessionServiceRequestHandler sessionServiceRequestHandler;
    
    @Autowired
    private ObjectMapper sessionServiceObjectMapper;

    /**
     * Retrieve CustomDataSession from session service for custom data attributes. 
     * @param customAttributeIds - attribute id/hash of custom data used as session service key.
     * @return Map of custom data attribute id to the CustomDataSession
     */
    @Override
    public Map<String, CustomDataSession> getCustomDataSessions(List<String> customAttributeIds) {
        Map<String, CompletableFuture<CustomDataSession>> postFuturesMap = customAttributeIds.stream()
            .collect(Collectors.toMap(
                attributeId -> attributeId,
                attributeId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        String customDataSessionJson = sessionServiceRequestHandler.getSessionDataJson(
                            Session.SessionType.custom_data,
                            attributeId
                        );
                        return sessionServiceObjectMapper.readValue(customDataSessionJson, CustomDataSession.class);
                    } catch (Exception e) {
                        return null;
                    }
                })
            ));

        CompletableFuture.allOf(postFuturesMap.values().toArray(new CompletableFuture[postFuturesMap.size()])).join();

        Map<String, CustomDataSession> customDataSessions = postFuturesMap.entrySet().stream()
            .filter(entry -> entry.getValue().join() != null)
            .collect(Collectors.toMap(
                entry -> entry.getKey(),
                entry -> entry.getValue().join()
            ));

        return customDataSessions;
    }
}
