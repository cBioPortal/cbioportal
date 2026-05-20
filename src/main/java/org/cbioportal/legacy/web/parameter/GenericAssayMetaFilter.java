package org.cbioportal.legacy.web.parameter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

public class GenericAssayMetaFilter implements Serializable {

  @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
  private List<String> molecularProfileIds;

  @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
  private List<String> genericAssayStableIds;

  private String keyword;

  @Min(PagingConstants.MIN_PAGE_SIZE)
  @Max(PagingConstants.MAX_PAGE_SIZE)
  private Integer limit;

  @Min(PagingConstants.MIN_PAGE_NUMBER)
  private Integer offset;

  private String sortBy;

  private Direction direction;

  public List<String> getMolecularProfileIds() {
    return molecularProfileIds;
  }

  public void setMolecularProfileIds(List<String> molecularProfileIds) {
    this.molecularProfileIds = molecularProfileIds;
  }

  public List<String> getGenericAssayStableIds() {
    return genericAssayStableIds;
  }

  public void setGenericAssayStableIds(List<String> genericAssayStableIds) {
    this.genericAssayStableIds = genericAssayStableIds;
  }

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public String getSortBy() {
    return sortBy;
  }

  public void setSortBy(String sortBy) {
    this.sortBy = sortBy;
  }

  public Direction getDirection() {
    return direction;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }
}
