/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Class to wrap Reference Genome Gene.
 *
 * @author Kelsey Zhu
 */
public class ReferenceGenomeGene implements Serializable {
  @NotNull private Integer referenceGenomeId;
  @NotNull private Integer entrezGeneId;
  private String hugoGeneSymbol;
  private String chromosome;
  private String cytoband;
  private Long start;
  private Long end;

  public void setReferenceGenomeId(Integer referenceGenomeId) {
    this.referenceGenomeId = referenceGenomeId;
  }

  public Integer getReferenceGenomeId() {
    return referenceGenomeId;
  }

  public Integer getEntrezGeneId() {
    return entrezGeneId;
  }

  public void setEntrezGeneId(Integer entrezGeneId) {
    this.entrezGeneId = entrezGeneId;
  }

  public String getHugoGeneSymbol() {
    return hugoGeneSymbol;
  }

  public void setHugoGeneSymbol(String hugoGeneSymbol) {
    this.hugoGeneSymbol = hugoGeneSymbol;
  }

  public String getChromosome() {
    return chromosome;
  }

  public void setChromosome(String chromosome) {
    this.chromosome = chromosome;
  }

  public String getCytoband() {
    return cytoband;
  }

  public void setCytoband(String cytoband) {
    this.cytoband = cytoband;
  }

  public Long getStart() {
    return this.start;
  }

  public void setStart(Long start) {
    this.start = start;
  }

  public Long getEnd() {
    return this.end;
  }

  public void setEnd(Long end) {
    this.end = end;
  }
}
