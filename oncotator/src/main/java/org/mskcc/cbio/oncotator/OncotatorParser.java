/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

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
	 * Parses the JSON returned by the oncotator web service, and returns
	 * the information as a new OncotateRecord instance.
	 * 
	 * @param key			chr#_start_end_allele1_allele2
	 * @param json			JSON object returned by the web service
	 * @return				new OncotatorRecord, or null if JSON has an error
	 * @throws java.io.IOException
	 */
    public static OncotatorRecord parseJSON (String key, String json) throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readValue(json, JsonNode.class);
        
        OncotatorRecord oncoRecord = new OncotatorRecord(key);
        oncoRecord.setRawJson(json);

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
            oncoRecord.setGenomeChange(genomeChange.getTextValue());
        }

        JsonNode cosmic = rootNode.path("Cosmic_overlapping_mutations");
        if (!cosmic.isMissingNode()) {
            oncoRecord.setCosmicOverlappingMutations(cosmic.getTextValue());
        }

        JsonNode dbSnpRs = rootNode.path("dbSNP_RS");
        if (!dbSnpRs.isMissingNode()) {
            oncoRecord.setDbSnpRs(dbSnpRs.getTextValue());
        }

        JsonNode bestCanonicalTranscriptIdxNode = rootNode.path("best_canonical_transcript");
	    JsonNode bestEffectTranscriptIdxNode = rootNode.path("best_effect_transcript");
	    JsonNode transcriptsNode = rootNode.path("transcripts");
	    int transcriptIndex;

        if (!bestCanonicalTranscriptIdxNode.isMissingNode())
        {
            transcriptIndex = bestCanonicalTranscriptIdxNode.getIntValue();
	        oncoRecord.setBestCanonicalTranscript(parseTranscriptNode(
			        transcriptsNode, transcriptIndex));
        }

	    if (!bestEffectTranscriptIdxNode.isMissingNode())
	    {
		    transcriptIndex = bestEffectTranscriptIdxNode.getIntValue();
		    oncoRecord.setBestEffectTranscript(parseTranscriptNode(
				    transcriptsNode, transcriptIndex));
	    }

        return oncoRecord;
    }

	public static Transcript parseTranscriptNode(JsonNode transcriptsNode,
			int transcriptIndex)
	{
		JsonNode bestTranscriptNode = transcriptsNode.get(transcriptIndex);

		String variantClassification = bestTranscriptNode.path("variant_classification").getTextValue();
		String proteinChange = bestTranscriptNode.path("protein_change").getTextValue();
		String geneSymbol = bestTranscriptNode.path("gene").getTextValue();
		int exonAffected = bestTranscriptNode.path("exon_affected").getIntValue();

		Transcript transcript = new Transcript();

		transcript.setVariantClassification(variantClassification);
		transcript.setProteinChange(proteinChange);
		transcript.setGene(geneSymbol);
		transcript.setExonAffected(exonAffected);

		return transcript;
	}
}
