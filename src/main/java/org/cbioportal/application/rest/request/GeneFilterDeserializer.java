package org.cbioportal.application.rest.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cbioportal.legacy.model.CNA;
import org.cbioportal.legacy.model.GeneFilter;
import org.cbioportal.legacy.model.GeneFilterQuery;

public class GeneFilterDeserializer extends JsonDeserializer<GeneFilter> {

  @Override
  public GeneFilter deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    ObjectCodec codec = parser.getCodec();
    JsonNode root = codec.readTree(parser);
    GeneFilter geneFilter = new GeneFilter();

    JsonNode molecularProfileIdsNode = root.get("molecularProfileIds");
    if (molecularProfileIdsNode != null && molecularProfileIdsNode.isArray()) {
      Set<String> molecularProfileIds = new HashSet<>();
      for (JsonNode item : molecularProfileIdsNode) {
        molecularProfileIds.add(item.asText());
      }
      geneFilter.setMolecularProfileIds(molecularProfileIds);
    }

    JsonNode geneQueriesNode = root.get("geneQueries");
    if (geneQueriesNode != null && geneQueriesNode.isArray()) {
      geneFilter.setGeneQueries(parseGeneQueries((ArrayNode) geneQueriesNode, codec, context));
    }

    return geneFilter;
  }

  private List<List<GeneFilterQuery>> parseGeneQueries(
      ArrayNode groupsNode, ObjectCodec codec, DeserializationContext context) throws IOException {
    List<List<GeneFilterQuery>> groups = new ArrayList<>();
    for (JsonNode groupNode : groupsNode) {
      if (!groupNode.isArray()) {
        throw JsonMappingException.from(
            context.getParser(), "Each geneQueries group must be an array");
      }
      List<GeneFilterQuery> group = new ArrayList<>();
      for (JsonNode item : groupNode) {
        if (item.isTextual()) {
          group.add(parseLegacyGeneQuery(item.asText(), context));
        } else if (item.isObject()) {
          group.add(codec.treeToValue(item, GeneFilterQuery.class));
        } else {
          throw JsonMappingException.from(
              context.getParser(), "Gene query entry must be a string or object");
        }
      }
      groups.add(group);
    }
    return groups;
  }

  private GeneFilterQuery parseLegacyGeneQuery(
      String geneQueryString, DeserializationContext context) throws IOException {
    if (geneQueryString == null) {
      throw JsonMappingException.from(context.getParser(), "Gene query string cannot be null");
    }
    String[] parts = geneQueryString.trim().split(":");
    String hugoGeneSymbol = parts[0].trim();
    if (hugoGeneSymbol.isEmpty()) {
      throw JsonMappingException.from(
          context.getParser(), "Cannot parse gene query string: '" + geneQueryString + "'");
    }
    List<CNA> alterations = new ArrayList<>();
    for (int i = 1; i < parts.length; i++) {
      for (String token : parts[i].trim().split("\\s+")) {
        if (!token.isEmpty()) {
          try {
            alterations.add(CNA.valueOf(token.toUpperCase()));
          } catch (IllegalArgumentException e) {
            throw JsonMappingException.from(
                context.getParser(),
                "Invalid CNA token '"
                    + token
                    + "' in gene query string: '"
                    + geneQueryString
                    + "'");
          }
        }
      }
    }
    GeneFilterQuery query = new GeneFilterQuery();
    query.setHugoGeneSymbol(hugoGeneSymbol);
    query.setAlterations(alterations.isEmpty() ? null : alterations);
    return query;
  }
}
