package org.cbioportal.legacy.model;

import java.io.Serializable;
import java.util.Set;

public abstract class AlterationCountBase implements Serializable {

  private Integer numberOfAlteredCases;
  private Integer numberOfAlteredCasesOffPanel;
  private Integer totalCount;
  private Integer numberOfProfiledCases;
  private Set<String> matchingGenePanelIds;

  public Integer getNumberOfAlteredCases() {
    return numberOfAlteredCases;
  }

  public void setNumberOfAlteredCases(Integer numberOfAlteredCases) {
    this.numberOfAlteredCases = numberOfAlteredCases;
  }

  public Integer getNumberOfAlteredCasesOffPanel() {
    return numberOfAlteredCasesOffPanel;
  }

  public void setNumberOfAlteredCasesOffPanel(Integer numberOfAlteredCasesOffPanel) {
    this.numberOfAlteredCasesOffPanel = numberOfAlteredCasesOffPanel;
  }

  public Integer getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Integer totalCount) {
    this.totalCount = totalCount;
  }

  public Integer getNumberOfProfiledCases() {
    return numberOfProfiledCases;
  }

  public void setNumberOfProfiledCases(Integer numberOfProfiledCases) {
    this.numberOfProfiledCases = numberOfProfiledCases;
  }

  public Set<String> getMatchingGenePanelIds() {
    return matchingGenePanelIds;
  }

  public void setMatchingGenePanelIds(Set<String> matchingGenePanelIds) {
    this.matchingGenePanelIds = matchingGenePanelIds;
  }

  public abstract String getUniqueEventKey();

  public abstract String[] getHugoGeneSymbols();

  public abstract Integer[] getEntrezGeneIds();
}
