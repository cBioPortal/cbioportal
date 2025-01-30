package org.cbioportal.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Monkifier {
    public static String decodeBase64(String value) {
        return new String(Base64.decodeBase64(value.getBytes()));
    }

    public static String encodeBase64(String value) {
        return value == null ? "": new String(Base64.encodeBase64(value.getBytes()));
    }

    public static String decodeQueryString(HttpServletRequest request) {
        if (request.getQueryString() == null) {
            return null;
        }

        return decodeQueryString(request.getParameterMap());
    }

    public static String decodeQueryString(Map<String, String[]> encodedQueryParams) {
        Map<String, List<String>> decodedQueryParams = encodedQueryParams
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    e -> Monkifier.decodeBase64(e.getKey()),
                    e -> Arrays.stream(e.getValue()).map(Monkifier::decodeBase64).collect(Collectors.toList())
                )
            );

        return UriComponentsBuilder
            .newInstance()
            .queryParams(CollectionUtils.toMultiValueMap(decodedQueryParams))
            .build()
            // encode to avoid potentially invalid query strings
            .encode()
            .toString()
            // remove ? from the beginning of the query string,
            // we only want the parameters (e.g: param1=value1&param2=value2)
            .replaceFirst("\\?", "");
    }
}
