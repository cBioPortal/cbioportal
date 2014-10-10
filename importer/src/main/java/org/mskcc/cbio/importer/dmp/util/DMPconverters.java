/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.importer.dmp.util;

import java.io.IOException;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.node.*;
import org.codehaus.jackson.map.ObjectMapper;

public class DMPconverters {

	/*
	 * tailor a raw DMP sample JSON object 
	 *  - replace the key from numerical number to a fixed phrase "sample"
	 *  - convert the "results" sub-object into an array 
	 *
	 * @param   the raw json object from the web service call
	 * @return  the tailored json object ready for mapping 
	 */

	public static String convertRaw(String _raw_sample_json_str)
		throws IOException {
		   	ObjectMapper mapper = new ObjectMapper();
		   	JsonFactory factory = mapper.getJsonFactory(); 
		   	JsonParser jp = factory.createJsonParser(_raw_sample_json_str);
           	JsonNode root = mapper.readTree(jp); //root contains: disclaimer, results, sample-count
           	
           	ArrayNode result_arr_node = mapper.createArrayNode();
           	JsonNode samples = root.path("results");
			Iterator<JsonNode> ite = samples.getElements();
			while (ite.hasNext()) {
				JsonNode sample = ite.next();
				result_arr_node.add(sample);
			}

			JsonNode final_result = mapper.createObjectNode();
			((ObjectNode)final_result).put("sample-count", root.get("sample-count").asText());
			((ObjectNode)final_result).put("disclaimer", root.get("disclaimer").asText());
			((ObjectNode)final_result).put("results", result_arr_node);

			String indented = mapper.defaultPrettyPrintingWriter().writeValueAsString(final_result);
			return indented;

	}

	/**
	 * THIS IS TEMPORARY
	 * add missing fields for the live dmp sample result, 
	 * therefore enable jsonschema2pojo library to generate the complete template for future use
	 * (This is only for init, will be aborted for production run)
	 * 
	 * @param the incomplete result json string
	 * @return the complete result json string
	 */
	public static String attachMissingValues() {
		return null;

	}
}

