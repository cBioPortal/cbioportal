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

public class JSONconverters {

    /*
     * tailor a raw DMP sample JSON object 
     *  - replace the key from numerical number to a fixed phrase "sample"
     *  - convert the "results" sub-object into an array 
     *
     * @param   the raw json object from the web service call
     * @return  the tailored json object ready for mapping 
     */

    public static String convertRaw(String rawSampleJsonStr)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory(); 
        JsonParser jp = factory.createJsonParser(rawSampleJsonStr);

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
    
    public static String convertSegDataJson(String rawSegDataJsonStr) 
            throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory(); 
        JsonParser jp = factory.createJsonParser(rawSegDataJsonStr);
        
        ArrayNode result_arr_node = mapper.createArrayNode();
        
        JsonNode root = mapper.readTree(jp);
        ArrayNode originalSegDataArr = (ArrayNode)root.get("seg-data");
        String sampleId = root.get("sampleId").asText();
        
        String[] fieldNames = 
                originalSegDataArr.get(0).toString().replaceAll("[\\[\\]]", "").replaceAll("\"", "").split(",");
        for (int _index = 1; _index < originalSegDataArr.size(); _index++) {
            String[] tokens = 
                    originalSegDataArr.get(_index).toString().replaceAll("[\\[\\]]", "").replaceAll("\"", "").split(",");
            JsonNode singleSegJson = mapper.createObjectNode();
            int _fieldIndex = 0;
            for(String token : tokens) {
                ((ObjectNode)singleSegJson).put(fieldNames[_fieldIndex], token);
                _fieldIndex += 1;
            }
            result_arr_node.add(singleSegJson);
        }
        
        JsonNode final_result = mapper.createObjectNode();
        ((ObjectNode)final_result).put("sampleId", sampleId);
        ((ObjectNode)final_result).put("segment-data", result_arr_node);
        
        String indented = mapper.defaultPrettyPrintingWriter().writeValueAsString(final_result);
        return indented;
        
    }
    
    public static String mergeSampleSegmentData(String sampleDataJsonStr, String segDataJsonStr) 
            throws IOException {
        
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory(); 
        JsonParser jp_sampleData = factory.createJsonParser(sampleDataJsonStr);
        JsonParser jp_segmentData = factory.createJsonParser(segDataJsonStr);
        JsonNode root_sampleData = mapper.readTree(jp_sampleData);
        JsonNode root_segmentData = mapper.readTree(jp_segmentData);
        
        //Find the target sample result json by using "sampleId" (in segment data) / "alys2sample_id" in sample meta data
        ArrayNode newResultsJson = mapper.createArrayNode();
        Iterator<JsonNode> sampleDataResultsArrItr = ((ArrayNode)root_sampleData.get("results")).getElements();
        while(sampleDataResultsArrItr.hasNext()) {
            JsonNode sampleDataResultJson = sampleDataResultsArrItr.next();
            if (sampleDataResultJson.get("meta-data").get("alys2sample_id").asText().equals(root_segmentData.get("sampleId").asText())) {
                JsonNode convertedResultJson = mapper.createObjectNode();
                ((ObjectNode)convertedResultJson).putAll((ObjectNode)sampleDataResultJson);
                ((ObjectNode)convertedResultJson).put("segment-data", root_segmentData.get("segment-data"));
                newResultsJson.add(convertedResultJson);
            } else {
                newResultsJson.add(sampleDataResultJson);
            }
        }
        
        //Get the rest of the information (sample-count, disclaimer, etc.)
        JsonNode final_result = mapper.createObjectNode();
        ((ObjectNode)final_result).put("sample-count", root_sampleData.get("sample-count").asText());
        ((ObjectNode)final_result).put("disclaimer", root_sampleData.get("disclaimer").asText());
        ((ObjectNode)final_result).put("results", newResultsJson);
        
        String indented = mapper.defaultPrettyPrintingWriter().writeValueAsString(final_result);
        return indented;
        
    }

}

