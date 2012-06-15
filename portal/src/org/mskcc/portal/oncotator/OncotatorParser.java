package org.mskcc.portal.oncotator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Parses JSON Retrieved from Oncotator.
 */
public class OncotatorParser {

    public static OncotatorRecord parseJSON (String key, String json) throws IOException {
        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readValue(json, JsonNode.class);

        OncotatorRecord oncotator = new OncotatorRecord(key);

        JsonNode genomeChange = rootNode.path("genome_change");
        if (genomeChange != null) {
            oncotator.setGenomeChange(genomeChange.getTextValue());
        }

        JsonNode cosmic = rootNode.path("Cosmic_overlapping_mutations");
        if (cosmic != null) {
            oncotator.setCosmicOverlappingMutations(cosmic.getTextValue());
        }

        JsonNode dbSnpRs = rootNode.path("dbSNP_RS");
        if (dbSnpRs != null) {
            oncotator.setDbSnpRs(dbSnpRs.getTextValue());
        }

        JsonNode bestTranscriptIndexNode = rootNode.path("best_canonical_transcript");

        if (bestTranscriptIndexNode != null) {
            int transcriptIndex = bestTranscriptIndexNode.getIntValue();
            JsonNode transcriptsNode = rootNode.path("transcripts");
            JsonNode bestTranscriptNode = transcriptsNode.get(transcriptIndex);

            String variantClassification = bestTranscriptNode.path("variant_classification").getTextValue();
            String proteinChange = bestTranscriptNode.path("protein_change").getTextValue();
            String geneSymbol = bestTranscriptNode.path("gene").getTextValue();
            int exonAffected = bestTranscriptNode.path("exon_affected").getIntValue();
            oncotator.setVariantClassification(variantClassification);
            oncotator.setProteinChange(proteinChange);
            oncotator.setGene(geneSymbol);
            oncotator.setExonAffected(exonAffected);
        }

        return oncotator;
    }
}
