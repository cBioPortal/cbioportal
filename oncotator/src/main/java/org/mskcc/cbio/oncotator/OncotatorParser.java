package org.mskcc.cbio.oncotator;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Parses JSON Retrieved from Oncotator.
 */
public class OncotatorParser
{

	/**
	 * Parses the JSON returned by the org.mskcc.cbio.oncotator org.mskcc.cbio.web service, and returns
	 * the information as a new OncotateRecord instance.
	 * 
	 * @param key			chr#_start_end_allele1_allele2
	 * @param json			JSON object returned by the org.mskcc.cbio.web service
	 * @return				new OncotatorRecord, or null if JSON has an error
	 * @throws java.io.IOException
	 */
    public static OncotatorRecord parseJSON (String key, String json) throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readValue(json, JsonNode.class);
        
        OncotatorRecord oncotator = new OncotatorRecord(key);
        
        // check if JSON has an ERROR
        
        JsonNode errorNode = rootNode.path("ERROR");
        if (!errorNode.isMissingNode())
        {
        	System.out.println("JSON parse error for " + key + ": " + errorNode.getTextValue());
        	return null;
        }

        // proceed in case of no JSON error
        
        JsonNode genomeChange = rootNode.path("genome_change");
        if (!genomeChange.isMissingNode()) {
            oncotator.setGenomeChange(genomeChange.getTextValue());
        }

        JsonNode cosmic = rootNode.path("Cosmic_overlapping_mutations");
        if (!cosmic.isMissingNode()) {
            oncotator.setCosmicOverlappingMutations(cosmic.getTextValue());
        }

        JsonNode dbSnpRs = rootNode.path("dbSNP_RS");
        if (!dbSnpRs.isMissingNode()) {
            oncotator.setDbSnpRs(dbSnpRs.getTextValue());
        }

        JsonNode bestTranscriptIndexNode = rootNode.path("best_canonical_transcript");

        if (!bestTranscriptIndexNode.isMissingNode()) {
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
