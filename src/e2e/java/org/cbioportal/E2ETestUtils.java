package org.cbioportal;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.legacy.model.AlterationEnrichment;

public final class E2ETestUtils {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private E2ETestUtils() {
        // utility
    }

    public static String loadTestData(String filename) throws Exception {
        // Try classpath first (preferred for test resources)
        InputStream is = E2ETestUtils.class.getResourceAsStream(
            "/org/cbioportal/AlterationEnrichmentControllerE2ETest/" + filename);
        if (is != null) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        // Fallback to filesystem path used previously
        return new String(Files.readAllBytes(
            Paths.get("src/e2e/java/org/cbioportal/AlterationEnrichmentControllerE2ETest/" + filename)),
            StandardCharsets.UTF_8);
    }

    public static AlterationEnrichment findGeneEnrichment(AlterationEnrichment[] enrichments, String geneSymbol) {
        return Arrays.stream(enrichments)
            .filter(enrichment -> geneSymbol.equals(enrichment.getHugoGeneSymbol()))
            .findFirst()
            .orElse(null);
    }

    public static int getTotalProfiledSamples(AlterationEnrichment enrichment) {
        return enrichment.getCounts().stream()
            .mapToInt(count -> count.getProfiledCount())
            .sum();
    }

    public static int getTotalAlteredSamples(AlterationEnrichment enrichment) {
        return enrichment.getCounts().stream()
            .mapToInt(count -> count.getAlteredCount())
            .sum();
    }
}