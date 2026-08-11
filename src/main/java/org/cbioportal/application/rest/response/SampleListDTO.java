package org.cbioportal.application.rest.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "SampleList", description = "Represents a sample list")
public class SampleListDTO {
  private String sampleListId;
  private String studyId;
  @JsonIgnore private Integer listId;
  private String category;
  private CancerStudyDTO study;
  private String name;
  private String description;
  private Integer sampleCount;
  private List<String> sampleIds;

  public String getSampleListId() {
    return sampleListId;
  }

  public void setSampleListId(String sampleListId) {
    this.sampleListId = sampleListId;
  }

  public String getStudyId() {
    return studyId;
  }

  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public Integer getListId() {
    return listId;
  }

  public void setListId(Integer listId) {
    this.listId = listId;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public CancerStudyDTO getStudy() {
    return study;
  }

  public void setStudy(CancerStudyDTO study) {
    this.study = study;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getSampleCount() {
    return sampleCount;
  }

  public void setSampleCount(Integer sampleCount) {
    this.sampleCount = sampleCount;
  }

  public List<String> getSampleIds() {
    return sampleIds;
  }

  public void setSampleIds(List<String> sampleIds) {
    this.sampleIds = sampleIds;
  }
}
