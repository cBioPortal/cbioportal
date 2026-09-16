package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "GenesetHierarchyInfo", description = "Represents a gene set hierarchy node")
public class GenesetHierarchyInfoDTO {
  private Integer nodeId;
  private String nodeName;
  private Integer parentId;
  private String parentNodeName;
  private List<GenesetDTO> genesets;

  public Integer getNodeId() {
    return nodeId;
  }

  public void setNodeId(Integer nodeId) {
    this.nodeId = nodeId;
  }

  public String getNodeName() {
    return nodeName;
  }

  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  public Integer getParentId() {
    return parentId;
  }

  public void setParentId(Integer parentId) {
    this.parentId = parentId;
  }

  public String getParentNodeName() {
    return parentNodeName;
  }

  public void setParentNodeName(String parentNodeName) {
    this.parentNodeName = parentNodeName;
  }

  public List<GenesetDTO> getGenesets() {
    return genesets;
  }

  public void setGenesets(List<GenesetDTO> genesets) {
    this.genesets = genesets;
  }
}
